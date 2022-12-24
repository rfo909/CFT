/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.util;

import java.util.*;
import java.util.zip.*;
import java.io.*;

/**
* Utility class keeping all relevant information about a file, and taking care of
* details around compression and character set. Delivers both input/output streams
* and PrintWriter / BufferedReader for processing text files. Great for copying files
* between compression types and/or character sets. Any file ending with ".zip" is
* automatically written as zip-compressed, or read as zip-compressed, etc.
*
* 2005-09 RFO
*
* 2006-04-03 RFO: made copyFrom and streamCopy more robust, so that open streams are closed when exception, for example when trying to write
* to a server that does not exist, or there are insufficient permissions.
*/
public class FileInfo {
    public static final int UNCOMPRESSED = 1;
    public static final int ZIP = 2;
    public static final int GZIP = 3;
    
    public static final String CHARSET_BINARY = "BINARY";

    private File f;
    private String completeName;
    private String charSet;
    private String directory;
    private String fileName;
    private String fileStem;
    private int compression;
    private String zipEntryType;

    public FileInfo (String completeName) {
        this(completeName, null);
    }

    /**
    * Valid values for charSet are ISO-8859-1, UTF-8, UTF-16, UTF-16BE and UTF-16LE, as well as FileInfo.CHARSET_BINARY for no coversion
    */
    public FileInfo (String completeName, String charSet) {
        this.completeName=completeName;
        if (charSet != null) {
            this.charSet=charSet;
        } else {
            this.charSet=FileInfo.CHARSET_BINARY;
        }

        int pos=completeName.lastIndexOf(File.separator);
        if (pos >= 0) {
            directory=completeName.substring(0,pos);
            fileName=completeName.substring(pos+1, completeName.length());
        } else {
            directory="";
            fileName=completeName;
        }

        // identify file name minus file type -> fileStem
        pos = fileName.lastIndexOf('.');
        if (pos >= 0) {
            fileStem=fileName.substring(0,pos);
        } else {
            fileStem=fileName; // no type
        }
        // create File object
        f=new File(completeName);

        // identify compressed files
        if (fileName.toLowerCase().endsWith(".gz")) {
            compression=GZIP;
        } else if (fileName.toLowerCase().endsWith(".z")) {
            compression=GZIP;
        } else if (fileName.toLowerCase().endsWith(".zip")) {
            compression=ZIP;
        } else {
            compression=UNCOMPRESSED;
        }
    }

    public String getCompleteName() {
        return completeName;
    }

    public String getCharSet() {
        return charSet;
    }

    public String getDirectory() {
        return directory;
    }
    public String getFileName() {
        return fileName;
    }
    public String getFileStem () {
        return fileStem;
    }
    public boolean fileExists() {
        return f.exists();
    }
    public Date getFileDate() {
        if (!f.exists()) return null;
        return new Date(f.lastModified());
    }
    public int getCompression() {
        return compression;
    }
    public void delete() throws Exception {
        if (f.exists()) f.delete();
    }

    public String toString() {
        return completeName;
    }

    /**
    * Inner class used by getInputStream() to return an InputStream that on close not just closes
    * the zipEntry input stream but also the ZipFile itself
    */
    class MyInputStream extends InputStream {
        private ZipFile zf;
        private InputStream is;
        MyInputStream (ZipFile zf, InputStream zipEntryStream) {
            this.zf=zf;
            this.is=zipEntryStream;
        }
        public void close() throws IOException {
            // the point of MyInputStream is to close both zipEntry-input stream and zipFile on close()
            is.close();
            zf.close();
        }
        // all other methods are pass-through to the "is" input stream
        public int available() throws IOException { return is.available(); }
        public void mark (int readLimit) { is.mark(readLimit); }
        public boolean markSupported() { return is.markSupported(); }
        public int read() throws IOException { return is.read(); }
        public int read (byte[] b) throws IOException { return is.read(b); }
        public int read (byte[] b, int off, int len) throws IOException { return is.read(b,off,len); }
        public void reset() throws IOException { is.reset(); }
        public long skip (long n) throws IOException { return is.skip(n); }
    }



    /**
    * Create input stream for reading file, possibly compressed. For ZIP-files, we require
    * there to be a single non-directory entry, which is the file content.
    */
    public InputStream getInputStream() throws Exception {
        if (compression==GZIP) {
            return new GZIPInputStream(new FileInputStream(f));
        }
        if (compression==ZIP) {
            ZipFile zipFile = new ZipFile(f);
            Enumeration zipEntries = zipFile.entries();
            if (zipEntries != null) {
                ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
                if (zipEntry != null && !zipEntry.isDirectory()) {
                    return new MyInputStream(zipFile, zipFile.getInputStream(zipEntry));
                }
            }
            throw new Exception("Unsupported directory structure within Zip-file - expected single file entry and no internal directory structure: " + f.getPath());
        }
        // default: uncompressed
        return new FileInputStream(f);
    }

    public BufferedReader getBufferedReader () throws Exception {
        if (charSet != null) {
            return new BufferedReader(new InputStreamReader(getInputStream(), charSet));
        } else {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }
    }

    /**
    * Before writing or copying data into file represented by this FileInfo,
    * one may set the zipEntryType, which is the type extension to use together
    * with fileStem when creating the zipEntry inside the zip-file. This is
    * only used with zip-files, as other files have no internal structure. If not
    * set, or set to null, then fileStem is used without a type extension. For example,
    * if FileInfo is created for file "a.zip" and we write content to it, then the
    * content is stored in zipEntry with name "a". Calling setZipEntryType("txt") before
    * writing to the file, makes the zipEntryName change to "a.txt".
    *
    * The "type" value should NOT contain dots.
    */
    public void setZipEntryType (String type) {
        this.zipEntryType=type;
    }


    /**
    * Create outputstream for writing compressed or uncompressed file, depending
    * on name. If the method setZipEntryType() has been called to set a zipEntryType,
    * then this is used to extend the fileStem to use as name for the zipEntry, if the
    * compression format is ZIP (file ends with .zip).
    */
    public OutputStream getOutputStream () throws Exception {
        if (compression == ZIP) {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(completeName));
            String zEntryName=fileStem;
            if (zipEntryType != null) zEntryName += "." + zipEntryType;
            ZipEntry zipEntry = new ZipEntry(zEntryName);
            out.putNextEntry(zipEntry);
            return out;
        } else if (compression == GZIP) {
            return new GZIPOutputStream(new FileOutputStream(completeName));
        } else {
            return new FileOutputStream(completeName);
        }
    }

    public PrintWriter getPrintWriter() throws Exception {
        if (charSet != null) {
            return new PrintWriter(new OutputStreamWriter(getOutputStream(), charSet));
        } else {
            return new PrintWriter(new OutputStreamWriter(getOutputStream()));
        }
    }

    /**
    * Read data from file indicated by argument, write them to this file. Both
    * source file and target file (this) can be compressed or not, depending on
    * file name. If compression types are the same, and (if zip) the filestem is
    * the same (file copied between folders with same name), then the process is
    * short-circuited, and the file is copied raw, not decompressed and recompressed.
    */
    public void copyFrom (FileInfo f) throws Exception {
        boolean isBinary=false;
        if (f.getCharSet().equals(CHARSET_BINARY) || this.getCharSet().equals(CHARSET_BINARY)) {
            // error if not both binary
            if (!f.getCharSet().equals(this.getCharSet())) { 
                throw new Exception("Can not copy from charset " + f.getCharSet() + " to " + this.getCharSet());
            }
            isBinary=true;
        }
        if (!isBinary && (f.getCharSet() != this.getCharSet())) {
            BufferedReader reader=null;
            PrintWriter pw=null;
            try {
                // must copy via BufferedReader / PrintWriter level to get charSet conversion right
                reader=f.getBufferedReader();
                pw=this.getPrintWriter();

                for (;;) {
                    String s=reader.readLine();
                    if (s==null) break;
                    pw.println(s);
                }
            } finally {
                if (pw != null) try {pw.close();} catch (Exception ex) {}
                if (reader != null) try {reader.close();} catch (Exception ex) {}
            }
        } else {
            // mode isBinary, or charsets are the same, copy is performed at stream level
            boolean rawCopyOk=false;
            if (f.getCompression() == this.getCompression()) {
                if (this.getCompression() != ZIP) {
                    // if uncompressed or gzip, then it is safe to raw copy the file, as it has
                    // no internal structure that must be consistent with file name
                    rawCopyOk=true;
                } else {
                    // for zip files, the filestem must be the same, as this is also the
                    // name of the internal zipEntry
                    if (f.getFileStem().equals(this.getFileStem())) {
                        rawCopyOk=true;
                    }
                }
            }

            InputStream is=null;
            OutputStream os=null;

            try {
                if (rawCopyOk) {
                    is=new FileInputStream(f.getCompleteName());
                    os=new FileOutputStream(this.getCompleteName());
                } else {
                    // use proper streams for possible decompress and/or compress
                    is=f.getInputStream();
                    os=this.getOutputStream();
                }
            } catch (Exception ex) {
                // let exception continue, but close streams first
                if (is != null) try {is.close();} catch (Exception ex2) {}
                if (os != null) try {os.close();} catch (Exception ex2) {}
                throw ex;
            }
            streamCopy(is,os);
        }
    }

    private void streamCopy (InputStream is, OutputStream os) throws Exception {
        try {
            byte[] buf = new byte[1024*64];
            int count;

            while((count = is.read(buf)) > 0) {
                os.write(buf,0,count);
            }
        } finally {
            os.close();
            is.close();
        }
    }

//    public static void main (String args[]) {
//        try {
//            FileInfo a=new FileInfo("c:\\xxx.zip", "UTF-8");
//            a.setZipEntryType("txt");
//            PrintWriter pw=a.getPrintWriter();
//            for (int i=0; i<100; i++) {
//                pw.println(i + ": dette er en");
//            }
//            pw.close();
//
//            try {
//                FileInfo b=new FileInfo("\\\\someServer\\yyy.zip");
//                b.setZipEntryType("txt");
//                b.copyFrom(a);
//            } catch (Exception ex) {
//                System.out.println("Failed to copy to invalid server");
//            }
//
//            FileInfo c=new FileInfo("c:\\zzz.zip");
//            c.setZipEntryType("txt");
//            c.copyFrom(a);
//            System.out.println("c-copy ok");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

}


