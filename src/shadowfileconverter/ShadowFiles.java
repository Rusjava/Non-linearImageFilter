/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shadowfileconverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.util.Formatter;
import java.util.Scanner;
import java.util.*;

/**
 *
 * @author Ruslan Feshchenko
 * @version
 */
public class ShadowFiles {
    
    private final boolean write;
    private final boolean binary;
    private File file=null;
    private Object stream=null;
    private int ncol;
    private int nrays;
    //private Counter rayCounter;

    /**
     * Main constructor
     * @param write false - open for reading, true - open for writing
     * @param binary false - open for text I/O, true - open for binary I/O
     * @param ncol number of columns
     * @param nrays number of rays
     * @throws java.io.FileNotFoundException
     */
    public ShadowFiles(boolean write, boolean binary, int ncol, int nrays) throws FileNotFoundException, IOException {
        this.write=write;
        this.binary=binary;
        this.ncol=ncol;
        this.nrays=nrays;
        if (write) {
            if (binary) {
                if (openWrite("Choose a binary file to save a ray set in")) {
                    stream=new DataOutputStream(new FileOutputStream(file, false));
                    ((DataOutputStream)stream).write(new byte [] {12,0,0,0});
                    ((DataOutputStream)stream).writeInt(Integer.reverseBytes(ncol));
                    ((DataOutputStream)stream).writeInt(Integer.reverseBytes(nrays));
                    ((DataOutputStream)stream).writeInt(0);
                    ((DataOutputStream)stream).write(new byte [] {12,0,0,0});
                }      
            } else {
                 if (openWrite("Choose a text file to save a ray set in")) {
                     stream=new PrintWriter(new FileWriter(file, false));
                     Formatter fm=new Formatter();
                     fm.format("%d %d", ncol, nrays);
                     ((PrintWriter)stream).println(fm);
                 }     
            }
 
        } else {
            if (binary) {
                int tmp;
                if (openRead("Choose a binary file with ray data")) {
                    stream=new DataInputStream(new FileInputStream(file));
                    tmp=((DataInputStream)stream).readInt();
                    this.ncol=Math.min(Integer.reverseBytes(((DataInputStream)stream).readInt()), ncol);
                    this.nrays=Math.min(Integer.reverseBytes(((DataInputStream)stream).readInt()), nrays);
                    tmp=((DataInputStream)stream).readInt();
                    tmp=((DataInputStream)stream).readInt();
                }    
            } else {
                if (openRead("Choose a text file with ray data")) {
                    Scanner header;
                    stream=new BufferedReader(new FileReader(file));
                    header=new Scanner(((BufferedReader)stream).readLine());
                    this.ncol=Math.min(header.nextInt(), ncol);
                    this.nrays=Math.min(header.nextInt(), nrays);
                }
            }
        }        
    }

    /**
     * Closes I/O stream
     * @throws IOException
     */
    public void close() throws IOException {
        if (write) {
            if (binary) {
                ((DataOutputStream)stream).close();
            } else {
                ((PrintWriter)stream).close();
            }
        } else {
            if (binary) {
                ((DataInputStream)stream).close();
            } else {
                ((BufferedReader)stream).close();
            }
        }          
    }
    
    /**
     * Writes binary data for one ray or the file heading
     * @param rayData double array of 18 numbers representing 18 columns of ray data
     * @throws java.io.IOException
     */
    public void write(double [] rayData) throws IOException {
        int nread=Math.min(rayData.length, ncol);
        if (binary) {
            ((DataOutputStream)stream).write(new byte [] {12,0,0,0});
            for (int i=0; i<nread; i++) {
                ((DataOutputStream)stream).
                        writeLong(Long.reverseBytes(Double.doubleToLongBits(rayData[i])));
            }
            ((DataOutputStream)stream).write(new byte [] {12,0,0,0});
        } else {
            Formatter fm=new Formatter();
            for (int i=0; i<nread; i++) {
                fm.format("%f ", rayData[i]);
            }
            ((PrintWriter)stream).println(fm);
        }
    }
    
    /**
     * Reads binary data of one ray or of the file heading
     * @param rayData double array of 18 numbers representing 18 columns of ray data
     * @throws java.io.IOException
     * @throws shadowfileconverter.ShadowFiles.EndOfFileException thrown when end of file is reached
     * @throws shadowfileconverter.ShadowFiles.EndOfLineException thrown when end of line is reached
     */
    public void read(double [] rayData) throws IOException, EndOfFileException, EndOfLineException {
        int tmp, nread=Math.min(rayData.length, ncol);
        if (binary) {
            tmp=((DataInputStream)stream).readInt();
            if (tmp==0) {
                throw new EndOfFileException(0);
            }
            for (int i=0; i<nread; i++) {
                rayData[i]=Double.longBitsToDouble(Long.reverseBytes(((DataInputStream)stream).readLong()));
            }
            tmp=((DataInputStream)stream).readInt();
        } else {
            Scanner header;
            String line=((BufferedReader)stream).readLine();
            if (line==null) {
                throw new EndOfFileException (0);
            }
            header=new Scanner(line);
            for (int i=0; i<nread; i++) {
                if (!header.hasNext()) {
                    throw new EndOfLineException(0);
                }
                rayData[i]=header.nextDouble();
            }
        }
    }
    
    public int getNcol() {
        return ncol;
    }
    
    public int getNrays() {
        return nrays;
    }
    
    private boolean openWrite(String title) {
        JFileChooser fo=new JFileChooser ();
        fo.setDialogTitle(title);
        int ans=fo.showOpenDialog(null);   
        if (ans==JFileChooser.APPROVE_OPTION) {
            file=fo.getSelectedFile();
            if (file.exists()) {
                int n=JOptionPane.showConfirmDialog(null, "The file already exists. Overwrite?", "Warning",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (n==JOptionPane.NO_OPTION) {
                    file=null;
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean openRead(String title) {
        JFileChooser fo=new JFileChooser ();
        fo.setDialogTitle(title);
        int ans=fo.showOpenDialog(null);   
        if (ans==JFileChooser.APPROVE_OPTION) {
            file=fo.getSelectedFile();
            return true;
        }
        return false;
    }
    
    /**
     * Class for exception when the number of rays is less than specified
     */
    public static class EndOfFileException extends Exception {
        int finalRayNumber;
        public EndOfFileException (int finalRayNumber) {
            this.finalRayNumber=finalRayNumber;
        }
    }
    
    /**
     * Class for exception when the number of columns is less than specified
     */
    public static class EndOfLineException extends Exception {
        int finalColNumber;
        public EndOfLineException (int finalColNumber) {
            this.finalColNumber=finalColNumber;
        }
    }
}
