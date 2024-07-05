/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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
import java.io.InputStreamReader;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.CtxCloseHook;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;

public class ObjLineReader extends ObjPersistent implements CtxCloseHook {
    
    private ObjFile file;
    private BufferedReader br;
    private long lineNumber;
    
    public ObjLineReader(ObjFile file) {
        this.file=file;
        
        this.add(new FunctionStart());
        this.add(new FunctionRead());
    }

    @Override
    public void ctxClosing(Ctx ctx) throws Exception {
        // When the context where start() was called, terminates, the
        // file is closed
        try {
            br.close();
            br=null;
        } catch (Exception ex) {
            ctx.addSystemMessage("LineReader closing file " + file.getName() + " fails with exception");
            throw ex;
        }
    }
    
    public Obj self() {
        return this;
    }
    
    
    private void init(Ctx ctx) {
        try {
            lineNumber=1;
            
            if (br != null) {
                try {
                    br.close();
                } catch (Exception ex) {
                    // ignore
                }
            }
            br=file.getBufferedReader();
            ctx.addCtxCloseHook(this);
        } catch (Exception ex) {
            br=null;
        }
    }
    
    @Override 
    public String getPersistenceId() {
        return "ObjLineReader: " + file.getPath();
    }
 

    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "LineReader";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "LineReader: " + file.getPath();
    }
    
    
     
    @Override
    public void cleanupOnExit() {
        try {
            if (br != null) br.close();
        } catch (Exception ex) {
            // 
        }
    }
    
    
    /**
     * API for FilterReader and others
     */
    public synchronized Value readLine() throws Exception {
        if (br==null) throw new Exception("File not open - call start() to open");
        String line=br.readLine();
        if (line==null) return new ValueNull(); // EOF
        return new ValueObjFileLine(line, lineNumber++, file);  
    }
 
    /**
     * API for FilterReader and others
     */
    public synchronized Value readLine(ObjGrep grep) throws Exception {
        if (br==null) throw new Exception("File not open - call start() to open");
        for(;;) {
            String line=br.readLine();
            if (line==null) break;
            if (grep.keepLine(line)) {
                return new ValueObjFileLine(line, lineNumber++, file);  
            }
        }
        return new ValueNull(); // EOF
    }

    
    class FunctionStart extends Function {
        public String getName() {
            return "start";
        }
        public String getShortDesc() {
            return "start() - open file for reading - returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            init(ctx);
            return new ValueObj(self());
        }
    }
 
    
    class FunctionRead extends Function {
        public String getName() {
            return "read";
        }
        public String getShortDesc() {
            return "read() - read one line";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return readLine();
         }
    }
 
    
    
    
}
