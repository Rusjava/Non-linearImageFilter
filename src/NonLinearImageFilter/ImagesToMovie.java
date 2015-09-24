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

import javax.media.ControllerListener;
import javax.media.Processor;
import javax.media.Format;
import javax.media.DataSink;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.ControllerEvent;
import javax.media.ConfigureCompleteEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.PrefetchCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.EndOfMediaEvent;
import javax.media.Time;
import javax.media.Buffer;
import javax.media.control.TrackControl;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.DataSource;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.NoDataSinkException;
import javax.media.format.RGBFormat;

/**
 *
 * @author Ruslan Feshchenko
 * @version 1.0
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
     * @param videoFormat
     * @return
     * @throws java.io.IOException
     */
    public boolean doIt(int width, int height, int frameRate, List inFiles, MediaLocator outML, int videoFormat) throws IOException {

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
        switch (videoFormat) {
            case 0:
                p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.MSVIDEO));
            case 1:
                p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));
        }

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
     * Creates the DataSink.
     *
     * @param p
     * @param outML
     * @return
     * @throws java.io.IOException
     */
    protected DataSink createDataSink(Processor p, MediaLocator outML) throws IOException {

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
        } catch (NoDataSinkException e) {
            return null;
        }

        return dsink;
    }

    private final Object waitSync = new Object();
    private boolean stateTransitionOK = true;

    /**
     * Block until the processor has transitioned to the given state. Return
     * false if the transition failed.
     *
     * @param p
     * @param state
     * @return
     */
    protected boolean waitForState(Processor p, int state) {
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
     *
     * @return
     */
    protected boolean waitForFileDone() {
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
    public class ImageDataSource extends PullBufferDataSource {

        private final PullBufferStream streams[];

        /**
         * The constructor
         *
         * @param width
         * @param height
         * @param frameRate
         * @param images
         */
        public ImageDataSource(int width, int height, int frameRate, List images) {
            this.streams = new ImageSourceStream[1];
            this.streams[0] = new ImageSourceStream(width, height, frameRate, images);
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
         *
         * @return
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
         *
         * @return
         */
        @Override
        public PullBufferStream[] getStreams() {
            return streams;
        }

        /**
         * Returning the movie duration
         *
         * @return
         */
        @Override
        public Time getDuration() {
            return DURATION_UNKNOWN;
        }

        /**
         * Returning controls
         *
         * @return
         */
        @Override
        public Object[] getControls() {
            return new Object[0];
        }

        /**
         * Returning control object of given type
         *
         * @param type
         * @return
         */
        @Override
        public Object getControl(String type) {
            return null;
        }
    }

    /**
     * The source stream to go along with ImageDataSource.
     */
    public class ImageSourceStream implements PullBufferStream {

        List images;
        int width, height;
        RGBFormat format;
        long duration;
        int nextImage = 0;  // index of the next image to be read.
        boolean ended = false;

        /**
         * The constructor
         *
         * @param width
         * @param height
         * @param frameRate
         * @param images
         */
        public ImageSourceStream(int width, int height, int frameRate, List images) {
            this.width = width;
            this.height = height;
            this.images = images;
            this.duration = 1000000000L / frameRate;
            this.format = new RGBFormat(new Dimension(width, height),
                    Format.NOT_SPECIFIED,
                    Format.byteArray, (float) frameRate, 24, 1, 2, 3
            );
        }

        /**
         * We should never need to block assuming data are read from
         * BufferedImages.
         *
         * @return
         */
        @Override
        public boolean willReadBlock() {
            return false;
        }

        /**
         * This is called from the Processor to read a frame worth of video
         * data.
         *
         * @param buf
         * @throws java.io.IOException
         */
        @Override
        public void read(Buffer buf) throws IOException {

            // Check if we've finished all the frames.
            if (nextImage >= images.size()) {
                // We are done.  Set EndOfMedia.    
                buf.setEOM(true);
                buf.setOffset(0);
                buf.setTimeStamp(duration * nextImage);
                buf.setLength(0);
                ended = true;
                return;
            }

            short[] dataShort = ((DataBufferUShort) ((ImageComponent) images.get(nextImage))
                    .getImage().getData().getDataBuffer()).getData();
            nextImage++;

            byte[] data = new byte[3 * dataShort.length];
            double dr = Math.pow(2, 8);
            for (int i = 0; i < dataShort.length; i++) {
                byte value = (byte) Math.round(dataShort[i] / dr);
                data[3 * i] = value;
                data[3 * i + 1] = value;
                data[3 * i + 2] = value;
            }

            buf.setData(data);
            buf.setOffset(0);
            buf.setLength(data.length);
            buf.setFormat(format);
            buf.setSequenceNumber(nextImage - 1);
            buf.setDuration(duration);
            buf.setTimeStamp(duration * (nextImage - 1));
            buf.setFlags(buf.getFlags() | Buffer.FLAG_KEY_FRAME);
        }

        /**
         * Return the format of each video frame. That will be JPEG.
         *
         * @return
         */
        @Override
        public Format getFormat() {
            return format;
        }

        /**
         * Returns content descriptor
         *
         * @return
         */
        @Override
        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor(ContentDescriptor.RAW);
        }

        /**
         * Returns buffer size in bytes
         *
         * @return
         */
        @Override
        public long getContentLength() {
            return 3L * width * height;
        }

        /**
         * Returns the end of stream flag
         *
         * @return
         */
        @Override
        public boolean endOfStream() {
            return ended;
        }

        /**
         * returns controls
         *
         * @return
         */
        @Override
        public Object[] getControls() {
            return new Object[0];
        }

        /**
         * Return control object of given type
         *
         * @param type
         * @return
         */
        @Override
        public Object getControl(String type) {
            return null;
        }
    }
}
