/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

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

package rf.configtool.main.runtime.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

/**
 * Sorting lines starting with date/time is typical for processing log files.
 * Even though doing so is also possible via the Int() wrapper, because this
 * implementation first is older, and second is much easier to use for this
 * particular case, it should not be removed. Speed over elegance?
 *
 */
public class ObjDateSort extends Obj {
    
    private Obj theObj() {
        return this;
    }
    
    private String dateFmt="yyyy-MM-dd HH:mm:ss,SSS";
    private int dateStringOffset=0; 
    private long searchMaxLines=10000;
    
    
    
    public ObjDateSort() {
        add(new FunctionSetDateFormat());
        add(new FunctionAsc());
        add(new FunctionGetDateFormat());
        add(new FunctionSearch());
        add(new FunctionSearchMaxLines());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }

    private Obj self() {
    	return this;
    }

    
    public String getTypeName() {
        return "DateSort";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular("DateSort");
    }

    class FunctionSetDateFormat extends Function {
        public String getName() {
            return "setDateFormat";
        }
        public String getShortDesc() {
            return "setDateFormat(str) - set date/time format according to SimpleDateFormat - return self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter: date/time format string");
            if (!(params.get(0) instanceof ValueString)) throw new Exception("Expected one parameter: date/time format string"); 
            dateFmt=((ValueString)params.get(0)).getVal();
            return new ValueObj(theObj());
        }
    }
    
    class FunctionGetDateFormat extends Function {
        public String getName() {
            return "getDateFormat";
        }
        public String getShortDesc() {
            return "getDateFormat() - get current date/time format";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(dateFmt);
        }
    }
    
    class FunctionAsc extends Function {
        public String getName() {
            return "asc";
        }
        public String getShortDesc() {
            return "asc(list) - return sorted list of lines";
        }
        
        class X {
            long time;
            Value v;
            X (long time, Value v) {
                this.time=time;
                this.v=v;
            }
        }
        
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter: list of strings");
            if (!(params.get(0) instanceof ValueList)) throw new Exception("Expected one parameter: list of strings");
            
            List<Value> lines=((ValueList) params.get(0)).getVal();
            SimpleDateFormat sdf=new SimpleDateFormat(dateFmt);
            
            List<X> list=new ArrayList<X>();
            
            for (Value line:lines) {
                String s=line.getValAsString();
                if (s.length()<dateStringOffset+dateFmt.length()) continue;
                long prevTime=0L;
                
                long time;
                try {
                    time=sdf.parse(s.substring(dateStringOffset, dateStringOffset+dateFmt.length())).getTime();
                } catch (Exception ex) {
                    time=prevTime;
                }
                list.add(new X(time, line));
                prevTime=time;
            }
            list.sort(new Comparator<X> (){
                public int compare (X a, X b) {
                    if (a.time<b.time) return -1;
                    if (a.time>b.time) return 1;
                    return 0;
                }
            });
            
            List<Value> result=new ArrayList<Value>();
            for (X x:list) {
                result.add(x.v);
            }
            
            return new ValueList(result);
            
        }
    }
    

    /**
     * 2020-08 RFO
     * Identify lines in file between fromDate and toDate. Handles huge files
     * by using random access and binary search on each file. Looks for lines starting
     * with date on current date format. Returns list of lines
     * @author roar
     *
     */
    class FunctionSearch extends Function {
        public String getName() {
            return "search";
        }
        public String getShortDesc() {
            return "search(file, fromTimeMillis, toTimeMillis, Grep?) - get lines between time values (millis), empty list if none";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() < 3 || params.size() > 4) throw new Exception("Expected parameters file, fromTime, toTime, Grep?");
            Obj fileObj=getObj("file", params, 0);
            long rangeFrom=getInt("fromTime", params, 1);
            long rangeTo=getInt("toTime", params, 2);
            
            Obj grepObj=null;
            if (params.size()==4) {
            	grepObj=getObj("grep",params,3);
            } 
            
            if (!(fileObj instanceof ObjFile)) {
            	throw new Exception("Expected parameters file, fromTime, toTime, Grep?");
            }
            if (grepObj != null && !(grepObj instanceof ObjGrep)) {
               	throw new Exception("Expected parameters file, fromTime, toTime, Grep?");
            }
            
            ObjFile f = (ObjFile) fileObj;
            ObjGrep grep=(ObjGrep) grepObj;
           
            File file=f.getFile();
            if (!file.exists()) throw new Exception("File '" + file.getAbsolutePath() + "' does not exist");
            if (rangeTo <= rangeFrom) throw new Exception("toDate must be after fromDate");
            
            List<Value> hits = search(f, rangeFrom, rangeTo, grep);
            if (hits==null) {
            	hits=new ArrayList<Value>(); // empty list
            }
            
            
            return new ValueList(hits);
        }

        
        private List<Value> search (ObjFile f, long rangeFrom, long rangeTo, ObjGrep grep) throws Exception {
        	final int BlockSize=8192;
        	final long fileLength=f.getFile().length();
        	
        	if (fileLength < 200*1024) {
        		return readLines(f,0,rangeFrom,rangeTo, grep);
        	}

        	//System.out.println("Seeking to " + seekPos);
        	long firstDate = identifyDate(f, 0, BlockSize, false);
        	// if first date after end of range, then no hits 
        	if (firstDate>rangeTo) return null;
        	
        	long lastDate = identifyDate(f, fileLength-BlockSize, BlockSize, true);
        	// if last date before start of range, then no hits
        	if (lastDate<rangeFrom) return null;
        	
        	
        	long firstPos=0L;
        	long lastPos=fileLength;
        	
        	
        	// complete or partial overlap?
        	final boolean startInside = (firstDate >= rangeFrom);
        	if (startInside) {
        		// completely inside
        		return readLines(f, firstPos, rangeFrom, rangeTo, grep);
        	}
        	
        	// search for start position
        	long midPos=firstPos+(lastPos-firstPos)/2;
        	
        	while (midPos-firstPos > 4*BlockSize) {
        		//System.out.println("firstPos=" + firstPos + " lastPos=" + lastPos);
        		long midDate = identifyDate(f, midPos, BlockSize, false);
        		if (midDate >= rangeFrom) {
        			// mid is inside (or after) range, move lastPos to midPos, and calculate new midPos
        			lastPos=midPos;
        			midPos=firstPos+(lastPos-firstPos)/2;
        		} else {
        			// mid is outside (before) range, move firstPos to midPos, and calculate new midPos
        			firstPos=midPos;
        			midPos=firstPos+(lastPos-firstPos)/2;
        		}
        	}

        	return readLines(f, firstPos, rangeFrom, rangeTo, grep);
        }
        
        private List<Value> readLines (ObjFile objFile, long seekPos, long rangeFrom, long rangeTo, ObjGrep grep) throws Exception {
           	final File f=objFile.getFile();
            RandomAccessFile raf=null;
            try {
            	raf=new RandomAccessFile(f,"r");

            	final String encoding = objFile.getEncoding();
            	
                BufferedReader br=null;

                raf.seek(seekPos);
            	br = new BufferedReader(
         			   new InputStreamReader(
         	                      new FileInputStream(raf.getFD()), encoding));
            	

            	List<Value> result=new ArrayList<Value>();
            	SimpleDateFormat sdf=new SimpleDateFormat(dateFmt);
            	boolean copyLines=false;
            	
                for (;;) {
                    String line=br.readLine();
                    if (line==null) break;
                    
                    
                    if (line.length() >= dateStringOffset + dateFmt.length()) {
	                    long time=0L;
	                    try {
	                        time=sdf.parse(line.substring(dateStringOffset, dateStringOffset+dateFmt.length())).getTime();
	                    } catch (Exception ex) {
	                        time=0L;
	                    }
	                    if (time > 0) {
		                    if (time >= rangeFrom) {
		                    	copyLines=true;
		                    }
		                    if (time > rangeTo) {
		                    	// done
		                    	break;
		                    }
	                    }
                    }
                    if (copyLines) {
                    	if (grep != null) {
                    		if (!grep.keepLine(line)) continue;
                    	}
                    	result.add(new ValueObjFileLine(line,-1L,objFile));
                    }
                    if (result.size() > searchMaxLines) {
                    	throw new Exception("Exceeded searchMaxLines=" + searchMaxLines);
                    }
                }
                return result;
            } finally {
            	try {raf.close();} catch (Exception ex) {};
            }

        }


        private long identifyDate (ObjFile objFile, long seekPos, int bufSize, boolean lastDate) throws Exception {

        	final File f=objFile.getFile();
            RandomAccessFile raf=null;
            try {
            	raf=new RandomAccessFile(f,"r");

            	final String encoding = objFile.getEncoding();

            	raf.seek(seekPos);
                
                byte[] buf=new byte[bufSize];
                FileInputStream in=new FileInputStream(raf.getFD());
                int count = in.read(buf);
                
                String longString=new String(buf,0,count,encoding);
                long foundDate=0L;
                
                SimpleDateFormat sdf=new SimpleDateFormat(dateFmt);
                StringTokenizer st=new StringTokenizer(longString,"\n",false);
                while (st.hasMoreTokens()) {
                	String s=st.nextToken();
                    if (s.length()<dateFmt.length()) continue;
                    long time=0L;
                    try {
                        time=sdf.parse(s.substring(0, dateFmt.length())).getTime();
                    } catch (Exception ex) {
                        time=0L;
                    }
                    if (time>0L) foundDate=time;
                    if (!lastDate && foundDate>0) {
                    	//System.out.println("Found date " + (new Date(foundDate)));
                    	// return first valid date
                    	return foundDate;
                    }
                }
                if (foundDate == 0L) throw new Exception("No dates found");
            	//System.out.println("Found end-date " + (new Date(foundDate)));
                return foundDate;
                
            } finally {
            	if (raf != null) try {raf.close();} catch (Exception ex) {};
            }
        }
        
    } // class FunctionSearch
 
    
    class FunctionSearchMaxLines extends Function {
        public String getName() {
            return "searchMaxLines";
        }
        public String getShortDesc() {
            return "searchMaxLines(count?) - get or set search max lines";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
        	if (params.size()==0) {
        		return new ValueInt(searchMaxLines);
        	}
            if (params.size() != 1) throw new Exception("Expected optional parameter count");
            long count=getInt("count",params,0);
            searchMaxLines=count;
            return new ValueObj(self());
        }
    }

}
    
