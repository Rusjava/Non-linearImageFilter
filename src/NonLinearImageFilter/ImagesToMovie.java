/*
 * Copyright (C) 2015 вапрол
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package NonLinearImageFilter;

import java.io.IOException;
import java.util.List;
import java.awt.Dimension;
import java.awt.image.DataBufferUShort;

import javax.media.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.datasink.*;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
/**
 * This program takes a list of BufferedImages and converts them into an AVI
 * movie.
 */
public class ImagesToMovie implements ControllerListener, DataSinkListener {

    /**
     * Do actual video file generation
     *
     * @param width
     * @param height
     * @param frameRate
     * @param inFiles
     * @param outML
     * @return
     */
    public boolean doIt(int width, int height, int frameRate, List inFiles, MediaLocator outML) {

        PullBufferDataSource ids = new ImageDataSource(width, height, frameRate, inFiles);

        Processor p;
        TrackControl tcs[];
        Format f[];
        DataSink dsink;

        /*
         * Creating processor for the image datasource
         */
        try {
            p = Manager.createProcessor(ids);
        } catch (Exception e) {
            return false;
        }

        p.addControllerListener(this);

        // Put the Processor into configured state so we can set
        // some processing options on the processor.
        p.configure();
        if (!waitForState(p, Processor.Configured)) {
            return false;
        }

        // Set the output content descriptor to QuickTime. 
        p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.MSVIDEO));

        // Query for the processor for supported formats.
        // Then set it on the processor.
        tcs = p.getTrackControls();
        f = tcs[0].getSupportedFormats();

        if (f == null || f.length <= 0) {
            return false;
        }
        /*
         * Setting track format
         */
        tcs[0].setFormat(f[0]);

        // We are done with programming the processor.  Let's just
        // realize it.
        p.realize();
        if (!waitForState(p, Processor.Realized)) {
            return false;
        }
        
        // Now, we'll need to create a DataSink.
        if ((dsink = createDataSink(p, outML)) == null) {
            return false;
        }
        dsink.addDataSinkListener(this);

        fileDone = false;
       
        // OK, we can now start the actual transcoding.
        try {
            p.start();
            dsink.start();
        } catch (IOException e) {
            return false;
        }

        // Wait for EndOfStream event.
        waitForFileDone();

        // Cleanup.
        try {
            dsink.close();
        } catch (Exception e) {

        }
        p.removeControllerListener(this);

        return true;
    }

    /**
     * Create the DataSink.
     */
    DataSink createDataSink(Processor p, MediaLocator outML) {

        DataSource ds;
        DataSink dsink;

        /*
         * Checking if processor have getDataOutput
         */
        if ((ds = p.getDataOutput()) == null) {
            return null;
        }
        /*
         * creating datasink
         */
        try {
            dsink = Manager.createDataSink(ds, outML);
            dsink.open();
        } catch (Exception e) {
            return null;
        }

        return dsink;
    }

    private final Object waitSync = new Object();
    private boolean stateTransitionOK = true;

    /**
     * Block until the processor has transitioned to the given state. Return
     * false if the transition failed.
     */
    private boolean waitForState(Processor p, int state) {
        synchronized (waitSync) {
            try {
                while (p.getState() < state && stateTransitionOK) {
                    waitSync.wait();
                }
            } catch (Exception e) {
            }
        }
        return stateTransitionOK;
    }

    /**
     * Controller Listener.
     *
     * @param evt
     */
    @Override
    public void controllerUpdate(ControllerEvent evt) {
        if (evt instanceof ConfigureCompleteEvent
                || evt instanceof RealizeCompleteEvent
                || evt instanceof PrefetchCompleteEvent) {
            synchronized (waitSync) {
                stateTransitionOK = true;
                waitSync.notifyAll();
            }
        } else if (evt instanceof ResourceUnavailableEvent) {
            synchronized (waitSync) {
                stateTransitionOK = false;
                waitSync.notifyAll();
            }
        } else if (evt instanceof EndOfMediaEvent) {
            evt.getSourceController().stop();
            evt.getSourceController().close();
        }
    }

    private final Object waitFileSync = new Object();
    private boolean fileDone = false;
    private boolean fileSuccess = true;

    /**
     * Block until file writing is done.
     */
    boolean waitForFileDone() {
        synchronized (waitFileSync) {
            try {
                while (!fileDone) {
                    waitFileSync.wait();
                }
            } catch (Exception e) {
            }
        }
        return fileSuccess;
    }

    /**
     * Event handler for the file writer.
     *
     * @param evt
     */
    @Override
    public void dataSinkUpdate(DataSinkEvent evt) {
        if (evt instanceof EndOfStreamEvent) {
            synchronized (waitFileSync) {
                fileDone = true;
                waitFileSync.notifyAll();
            }
        } else if (evt instanceof DataSinkErrorEvent) {
            synchronized (waitFileSync) {
                fileDone = true;
                fileSuccess = false;
                waitFileSync.notifyAll();
            }
        }
    }

    ///////////////////////////////////////////////
    //
    // Inner classes.
    ///////////////////////////////////////////////
    /**
     * A DataSource to read from a list of BufferedImage files and turn that
     * into a stream of JMF buffers. The DataSource is not seekable or
     * positionable.
     */
    class ImageDataSource extends PullBufferDataSource {

        private final PullBufferStream streams[];
        private final int frameRate;
        private final int frameNumber;

        ImageDataSource(int width, int height, int frameRate, List images) {
            this.streams = new ImageSourceStream[1];
            this.streams[0] = new ImageSourceStream(width, height, frameRate, images);
            this.frameRate = frameRate;
            this.frameNumber = images.size();
        }

        @Override
        public void setLocator(MediaLocator source) {
        }

        @Override
        public MediaLocator getLocator() {
            return null;
        }

        /**
         * Content type is of RAW since we are sending buffers of video frames
         * without a container format.
         */
        @Override
        public String getContentType() {
            return ContentDescriptor.RAW;
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        /**
         * Return the ImageSourceStreams.
         */
        @Override
        public PullBufferStream[] getStreams() {
            return streams;
        }

        /**
         * Returning the movie duration
         */
        @Override
        public Time getDuration() {
            return DURATION_UNKNOWN;
        }

        @Override
        public Object[] getControls() {
            return new Object[0];
        }

        @Override
        public Object getControl(String type) {
            return null;
        }
    }

    /**
     * The source stream to go along with ImageDataSource.
     */
    class ImageSourceStream implements PullBufferStream {

        List images;
        int width, height;
        RGBFormat format;

        int nextImage = 0;  // index of the next image to be read.
        boolean ended = false;

        public ImageSourceStream(int width, int height, int frameRate, List images) {
            this.width = width;
            this.height = height;
            this.images = images;

            /*this.format = new VideoFormat(VideoFormat.RGB,
                    new Dimension(width, height),
                    Format.NOT_SPECIFIED,
                    Format.byteArray,
                    (float) frameRate);*/
            
            this.format = new RGBFormat (new Dimension(width, height),
                    Format.NOT_SPECIFIED,
                    Format.byteArray, (float) frameRate, 24, 1, 2, 3);
            
        }

        /**
         * We should never need to block assuming data are read from files.
         */
        @Override
        public boolean willReadBlock() {
            return false;
        }

        /**
         * This is called from the Processor to read a frame worth of video
         * data.
         */
        @Override
        public void read(Buffer buf) throws IOException {

            // Check if we've finished all the frames.
            if (nextImage >= images.size()) {
                // We are done.  Set EndOfMedia.    
                buf.setEOM(true);
                buf.setOffset(0);
                buf.setTimeStamp(100000000L * nextImage);
                buf.setLength(0);
                ended = true;
                return;
            }

            short[] dataShort = ((DataBufferUShort) ((ImageComponent) images.get(nextImage)).getImage().getData().getDataBuffer()).getData();
            nextImage++;
            
            byte[] data = new byte[3 * dataShort.length];
            for (int i = 0; i < dataShort.length; i++) {
                data[3 * i] = (byte) (dataShort[i] >>> 8);
                data[3 * i + 1] = (byte) (dataShort[i] >>> 8);
                data[3 * i + 2] = (byte) (dataShort[i] >>> 8);
            }

            buf.setData(data);
            buf.setOffset(0);
            buf.setLength(data.length);
            buf.setFormat(format);
            //buf.setSequenceNumber(nextImage - 1);
            //buf.setDuration(100000000L);
            buf.setTimeStamp(100000000L * (nextImage - 1));
            //buf.setFlags(buf.getFlags() | Buffer.FLAG_KEY_FRAME);
        }

        /**
         * Return the format of each video frame. That will be JPEG.
         */
        @Override
        public Format getFormat() {
            return format;
        }

        @Override
        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor(ContentDescriptor.RAW);
        }

        @Override
        public long getContentLength() {
            return LENGTH_UNKNOWN;
        }

        @Override
        public boolean endOfStream() {
            return ended;
        }

        @Override
        public Object[] getControls() {
            return new Object[0];
        }

        @Override
        public Object getControl(String type) {
            return null;
        }
    }
}
