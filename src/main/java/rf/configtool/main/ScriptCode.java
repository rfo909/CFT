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

package rf.configtool.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.util.TabUtil;



public class ScriptCode {
    
     
    private PropsFile props;
    private File savefile;
    private Map<String, FunctionBody> namedFunctions=new HashMap<String,FunctionBody>();
    private List<String> namesInSequence=new ArrayList<String>();
    private ObjTerm term;
    
    private String currLine;
    
    public ScriptCode (PropsFile props, ObjTerm cfg) {
        this.props=props;
        this.term=cfg;
    }
    
    public void setCurrLine (String line) {
        if (line.trim().length()==0) return; 
        currLine=line;
    }
    
    public String getCurrLine() {
        return currLine;
    }
    
    public boolean assignPublicName(String name, boolean force) throws Exception {
        return assignFunctionName(name, false, force);
    }
    
    public boolean assignPrivateName (String name, boolean force) throws Exception {
        return assignFunctionName(name, true, force);
    }
    
    
    private boolean assignFunctionName(String name, boolean isPrivate, boolean force) throws Exception {
        if (currLine==null) throw new Exception("No current code line");
        if (!Lexer.stringIsIdentifier(name)) throw new Exception("Invalid function name: " + name);
        if (namedFunctions.get(name) != null && !force) return false;
        
        if (!namesInSequence.contains(name)) namesInSequence.add(name);
        FunctionBody body=namedFunctions.get(name);
        SourceLocation loc=new SourceLocation("<func> " + name, 0, 0);
        if (body==null) {
            body=new FunctionBody(currLine, isPrivate, loc);
            namedFunctions.put(name, body);
        } else {
            body.redefineFunctionInteractively(currLine, loc);
        }
        return true;
    }

    public FunctionBody getFunctionBody (String name) {
        return namedFunctions.get(name); // may be null
    }
    

    public void reportAll(Stdio stdio, boolean publicOnly) {
        report(stdio, null, publicOnly);
    }
    
    public List<String> getNames() {
        List<String> list=new ArrayList<String>();
        for (String name:namesInSequence) list.add(name);
        return list;
    }
    
    public void report(Stdio stdio, String symbolSubStr, boolean publicOnly) {
        final int available=term.getScreenWidth();
        
        String hr = "+-----------------------------------------------------";
        
        stdio.println(hr);
        int nameMaxLength=3;
        if (namesInSequence.size()==0 && currLine==null) {
            stdio.println("No symbols defined, use /ident to define symbol for last program line");
        } else {
            for (String name:namesInSequence) {
                if (name.length() > nameMaxLength) nameMaxLength=name.length();
            }

            List<String> matchingNames=new ArrayList<String>();
            
            if (symbolSubStr != null) {
                
                // Check for exact match first
                for (int i=0; i<namesInSequence.size(); i++) {
                    String name=namesInSequence.get(i);
                    if (name.equals(symbolSubStr)) {
                        matchingNames.add(name);
                    }
                }
                if (matchingNames.isEmpty()) {
                    for (int i=0; i<namesInSequence.size(); i++) {
                        String name=namesInSequence.get(i);
                        if (name.startsWith(symbolSubStr)) {
                            matchingNames.add(name);
                        }
                    }
                } 
            } else {
                // include all
                for (int i=0; i<namesInSequence.size(); i++) {
                    String name=namesInSequence.get(i);
                    matchingNames.add(namesInSequence.get(i));
                }
            }
                
            boolean showFull = (matchingNames.size()==1 && symbolSubStr != null);
            for (String name:matchingNames) {
                
                FunctionBody body=getFunctionBody(name);
                
                if ( ((body.isPrivate() || body.isClass()) && publicOnly && !showFull) ) continue;
                
                
                
                String label=name;
                while(label.length()<nameMaxLength) label=label+" ";
                if (showFull) {
                    FunctionBody codeLines = getFunctionBody(name);
                    List<String> text = codeLines.getSaveFormat();
                    boolean foundContent=false;
                    stdio.println();
                    for (String line:text) {
                        if (!foundContent && line.trim().length()==0) continue;
                        foundContent=true;
                        if (line.length() > available) line=line.substring(0,available-1)+"+";
                        stdio.println(line);
                    }
                    if (body.isPrivate()) {
                        stdio.println("//"+name);
                    } else if (body.isClass()) {
                        stdio.println(body.getClassDetails().createClassDefString());
                    } else {
                        stdio.println("/"+name);
                    }
                    stdio.println();
                } else {
                    String namedLine=getFunctionBody(name).getFirstNonBlankLine();
                    
                    String pre=" ";
                    if (body.isClass()) {
                        pre="&";
                    } else if (body.isPrivate()) {
                        pre="/";
                    }
                    
                    String s="| " + pre + label + ": " + TabUtil.substituteTabs(namedLine,1);
    
                    if (s.length() > available) s=s.substring(0,available-1)+"+";
    
                    stdio.println(s);
                } 
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
    
    private String createSavefileName (String scriptName) {
        return "savefile" + scriptName + ".txt";
    }
    
    /**
     * Return File for existing file as looked up along the Props path. Used 
     * by global function savefile()
     */
    public File getSaveFile (String saveName, File currentDir) throws Exception {
        if (savefile==null) {
            savefile=props.getScriptSavefile(createSavefileName(saveName),currentDir);
        }
        return savefile;
    }
    
    
    public void save(String scriptName, File currentDir) throws Exception {
        String sfn=createSavefileName(scriptName);
        
        if (this.savefile != null) {
            if (!this.savefile.getName().equals(sfn)) {
                // saving with new name, using current dir
                this.savefile=null;
            }
        }
        if (this.savefile==null) {
            this.savefile=new File(currentDir.getCanonicalPath()+File.separator+sfn);
        } 
        
        PrintStream ps=new PrintStream(new FileOutputStream(this.savefile));
        
        for (String functionName:namesInSequence) {
            FunctionBody body=namedFunctions.get(functionName);
            List<String> saveLines=body.getSaveFormat();
            for (String x:saveLines) {
                ps.println(x);
            }

            if (body.isClass()) {
                ps.println(body.getClassDetails().createClassDefString());
            } else if (body.isPrivate()) {
                ps.println("//"+functionName);
            } else {
                ps.println("/"+functionName);
            }
        }
        ps.close();
        return;
    }
    
    private String getInlineIdentifier(String s, SourceLocation loc, String err) throws Exception {
        int pos=s.indexOf(" ");
        if (pos < 0) throw new SourceException(loc, err);
        return s.substring(pos).trim();
    }
    
    public void load(String scriptName, File currentDir) throws Exception {
        namedFunctions.clear();
        namesInSequence.clear();
        
        
        if (savefile==null) {
            String sfn=createSavefileName(scriptName);
            savefile=props.getScriptSavefile(sfn, currentDir);
        }
        BufferedReader reader=new BufferedReader(new FileReader(savefile));
        
        List<ScriptSourceLine> lines=new ArrayList<ScriptSourceLine>();
        CodeInlineDocument inlineDoc=null;
       
        for(int lineNumber=1; true; lineNumber++) {
            String s=reader.readLine();
            if (s==null) break;
            
            SourceLocation loc=new SourceLocation("<script> " + scriptName, lineNumber, 0);

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
                if (trimmed.startsWith(">>>") && inlineDoc.matchesEofMark(getInlineIdentifier(trimmed, loc, "expected identifier following sequence of >>> and space"))) {
                    lines.add(new ScriptSourceLine(loc,s,ScriptSourceLine.TYPE_LINE_ORIGINAL));
                    // add generated code line
                    lines.add(new ScriptSourceLine(loc,inlineDoc.createCodeLine(),ScriptSourceLine.TYPE_LINE_GENERATED));
                    inlineDoc=null;
                    continue;
                }
                // add raw text line
                inlineDoc.addLine(s);
                lines.add(new ScriptSourceLine(loc,s,ScriptSourceLine.TYPE_LINE_ORIGINAL));
                continue;
            }
            if (trimmed.startsWith("<<<")) {
                String inlineEofMark=getInlineIdentifier(trimmed, loc, "expected identifier following sequence of <<< and space");
                inlineDoc=new CodeInlineDocument(inlineEofMark, loc);
                lines.add(new ScriptSourceLine(loc, s, ScriptSourceLine.TYPE_LINE_ORIGINAL));
                continue;
            }


            if (trimmed.startsWith("/")) {
                boolean isPrivate=false;
                
                ClassDetails cd=null;
                
                String functionName;
                
                if (trimmed.startsWith("/class") || trimmed.startsWith("//class")) { 
                    Lexer lexer=new Lexer();
                    lexer.processLine(new ScriptSourceLine(loc, trimmed));
                    TokenStream ts=lexer.getTokenStream();

                    cd=new ClassDetails(ts); // parse the /class ident ... string
                    functionName=cd.getName();
                } else if (trimmed.startsWith("//")) {
                    isPrivate=true;
                    functionName=trimmed.substring(2);
                    if (!Lexer.stringIsIdentifier(functionName)) throw new SourceException(loc,"Expected function name to be identifier: '" + functionName + "'");
                } else {
                    functionName=trimmed.substring(1);
                    if (!Lexer.stringIsIdentifier(functionName)) throw new SourceException(loc,"Expected function name to be identifier: '" + functionName + "'");
                }
                
                
                Lexer lexer=new Lexer();
                lexer.processLine(new ScriptSourceLine(new SourceLocation(), functionName));
                

                if (namesInSequence.contains(functionName)) namesInSequence.remove(functionName);
                namesInSequence.add(functionName);
                
                FunctionBody body=new FunctionBody(lines,isPrivate,cd);
                
                namedFunctions.put(functionName, body);
                
                lines=new ArrayList<ScriptSourceLine>();
                continue;
            } 
                
            lines.add(new ScriptSourceLine(loc,s));
        }
        
        reader.close();
        
    }
    
    
    public void clear (String name) throws Exception {
        if (namedFunctions.containsKey(name)) namedFunctions.remove(name);
        if (namesInSequence.contains(name)) namesInSequence.remove(name);
    }
    
    public void copy (String fromName, String toName) throws Exception {
        if (!namedFunctions.containsKey(fromName)) throw new Exception("No such named line: " + fromName);
        if (namedFunctions.containsKey(toName)) throw new Exception("Target name '" + toName + "'exists - clear it first");
        
        namedFunctions.put(toName, namedFunctions.get(fromName));
        namesInSequence.add(toName);
    }
}
