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

package rf.configtool.main;

import java.io.*;
import java.util.*;

import rf.configtool.data.Stmt;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.parser.Parser;
import rf.configtool.parser.SourceLocation;
import rf.configtool.parser.Token;
import rf.configtool.util.TabUtil;

public class CodeHistory {
    
     
    private Stdio stdio;
    private PropsFile props;
    private Map<String, CodeLines> namedLines=new HashMap<String,CodeLines>();
    private List<String> namesInSequence=new ArrayList<String>();
    private ObjCfg cfg;
    
    private String currLine;
    
    public CodeHistory (Stdio stdio, PropsFile props, ObjCfg cfg) {
        this.stdio=stdio;
        this.props=props;
        this.cfg=cfg;
    }
    
    public void setCurrLine (String line) {
        if (line.trim().length()==0) return; 
        currLine=line;
    }
    
    public String getCurrLine() {
        return currLine;
    }
    
    public boolean assignName(String name, boolean force) throws Exception {
        if (currLine==null) throw new Exception("No current code line");
        if (namedLines.get(name) != null && !force) return false;
        
        if (!namesInSequence.contains(name)) namesInSequence.add(name);
        CodeLines c=namedLines.get(name);
        SourceLocation loc=new SourceLocation("<func> " + name, 0, 0);
        if (c==null) {
            c=new CodeLines(currLine, loc);
            namedLines.put(name, c);
        } else {
            c.update(currLine, loc);
        }
        return true;
    }

    public CodeLines getNamedLine (String name) {
        return namedLines.get(name); // may be null
    }
    

    public void reportAll() {
        report(null);
    }
    
    public List<String> getNames() {
        List<String> list=new ArrayList<String>();
        for (String name:namesInSequence) list.add(name);
        return list;
    }
    
    public void report(String symbolSubStr) {
        final int available=cfg.getScreenWidth();
        
        String hr = "+-----------------------------------------------------";
        
        stdio.println(hr);
        int nameMaxLength=3;
        if (namesInSequence.size()==0 && currLine==null) {
            stdio.println("No symbols defined, use /ident to define symbol for last program line");
        } else {
            for (String name:namesInSequence) {
                if (name.length() > nameMaxLength) nameMaxLength=name.length();
            }

            for (int i=0; i<namesInSequence.size(); i++) {
                String name=namesInSequence.get(i);
                
                if (symbolSubStr != null && !name.contains(symbolSubStr)) continue; 
                
                String label=name;
                while(label.length()<nameMaxLength) label=label+" ";
                String namedLine=getNamedLine(name).getFirstNonBlankLine();
                
                String s="| " + label + ": " + TabUtil.substituteTabs(namedLine,1);

                if (s.length() > available) s=s.substring(0,available-1)+"+";

                stdio.println(s);
            }

        }
        if (currLine != null) {
            stdio.println(hr);
            String label=".";
            while (label.length()<nameMaxLength) label=label+" ";
            String s="| " + label + ": " + TabUtil.substituteTabs(currLine,1);
            
            if (s.length() > available) s=s.substring(0,available-1)+"+";
            stdio.println(s);
        }
        stdio.println(hr);

        
    }
    
    private String createSavefileName (String name) {
        return "savefile" + name + ".txt";
    }
    
    /**
     * Return File for existing file as looked up along the Props path. Used 
     * by global function savefile()
     */
    public File getSaveFile (String saveName) throws Exception {
    	return props.getCodeLoadFile(createSavefileName(saveName));
    }
    
    
    public void save(String saveName) throws Exception {
    	File file=props.getCodeSaveFile(createSavefileName(saveName));
    	stdio.println("(save) " + file.getCanonicalPath());
        PrintStream ps=new PrintStream(new FileOutputStream(file));
        
        for (String s:namesInSequence) {
            CodeLines c=namedLines.get(s);
            List<String> saveLines=c.getSaveFormat();
            for (String x:saveLines) {
                ps.println(x);
            }
            ps.println("/" + s);
        }
        ps.close();
        return;
    }
    
    public void load(String saveName) throws Exception {
        namedLines.clear();
        namesInSequence.clear();
        
    	File file=props.getCodeLoadFile(createSavefileName(saveName));
    	stdio.println("(load) " + file.getCanonicalPath());
        BufferedReader reader=new BufferedReader(new FileReader(file));
        
        List<CodeLine> lines=new ArrayList<CodeLine>();
        CodeInlineDocument inlineDoc=null;
       
        for(int lineNumber=1; true; lineNumber++) {
            String s=reader.readLine();
            if (s==null) break;
            
            SourceLocation loc=new SourceLocation("<script> " + saveName, lineNumber, 0);

            // Process inline document format, but add lines as-is to the
            // lines list, as this the external representation. It gets parsed
            // again in CodeLine() constructor. Here we are only concerned with
            // termination, to know when "/xxx" actually means creating
            // a named function.
            //
            // <<< mark
            // line 1
            // line 2
            // >>> mark
            //
            final String trimmed=s.trim();

            if (inlineDoc != null) {
                // check for end marker
                if (trimmed.startsWith(">>>") && inlineDoc.matchesEofMark(trimmed.substring(3).trim())) {
                	lines.add(new CodeLine(loc,s,CodeLine.TYPE_LINE_ORIGINAL));
                    // add generated code line
                    lines.add(new CodeLine(loc,inlineDoc.createCodeLine(),CodeLine.TYPE_LINE_GENERATED));
                    inlineDoc=null;
                    continue;
                }
                // add raw text line
                inlineDoc.addLine(s);
                lines.add(new CodeLine(loc,s,CodeLine.TYPE_LINE_ORIGINAL));
                continue;
            }
            if (trimmed.startsWith("<<<")) {
                String inlineEofMark=trimmed.substring(3).trim();
                inlineDoc=new CodeInlineDocument(inlineEofMark, loc);
                lines.add(new CodeLine(loc, s, CodeLine.TYPE_LINE_ORIGINAL));
                continue;
            }


            if (trimmed.startsWith("/")) {
                String name=s.trim().substring(1);
                if (namesInSequence.contains(name)) namesInSequence.remove(name);
                namesInSequence.add(name);
                CodeLines c=new CodeLines(lines);
                namedLines.put(name, c);
                lines=new ArrayList<CodeLine>();
                continue;
            } 
                
            lines.add(new CodeLine(loc,s));
        }
        
        reader.close();
        
    }
    
    
    public void clear (String name) throws Exception {
        if (namedLines.containsKey(name)) namedLines.remove(name);
        if (namesInSequence.contains(name)) namesInSequence.remove(name);
    }
    public void copy (String fromName, String toName) throws Exception {
        if (!namedLines.containsKey(fromName)) throw new Exception("No such named line: " + fromName);
        if (namedLines.containsKey(toName)) throw new Exception("Target name '" + toName + "'exists - clear it first");
        
        namedLines.put(toName, namedLines.get(fromName));
        namesInSequence.add(toName);
    }
}
