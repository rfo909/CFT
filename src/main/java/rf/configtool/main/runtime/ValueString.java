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

package rf.configtool.main.runtime;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.util.Hex;

public class ValueString extends Value implements IsSynthesizable {
    
    private String val;
    
    public ValueString (String val) {
        this.val=val;
        Function[] arr={
                new FunctionSub(),
                new FunctionLength(),
                new FunctionTrim(),
                new FunctionSplit(),
                new FunctionEndsWith(),
                new FunctionStartsWith(),
                new FunctionContains(),
                new FunctionToUpper(),
                new FunctionToLower(),
                new FunctionParseInt(),
                new FunctionParseFloat(),
                new FunctionReplaceChars(),
                new FunctionReplace(),
                new FunctionMerge(),
                new FunctionIndexOf(),
                new FunctionBetween(),
                new FunctionBefore(),
                new FunctionAfter(),
                new FunctionBeforeLast(),
                new FunctionAfterLast(),
                new FunctionChars(),
                new FunctionEsc(),
                new FunctionUnEsc(),
                new FunctionToHexString(),
                new FunctionFromHexString(),
                new FunctionHash(),
                new FunctionGetBytes(),
                new FunctionLast(),
                new FunctionFirst(),
                new FunctionPrintable(),
                new FunctionMergeExpr(),
                new FunctionTimes(),
            };
            setFunctions(arr);
        
    }
    
    public String getVal() {
        return val;
    }
    
    @Override
    public String getTypeName() {
        return "String";
    }

    @Override
    public String getValAsString() {
        return val;
    }
    
    @Override
    public String createCode() throws Exception {
        
        if (val==null) return "''";
        
        // if string contains non-printable characters, use hex encoding
        boolean useHex=false;
        for (int i=0; i<val.length(); i++) {
            char ch=val.charAt(i);
            if (ch < 32 || ch >= 127) {
                useHex=true;
                break;
            }
        }
        if (useHex) {
            return '"' + toHexString(val) + '"' + ".fromHexString";
        }
        
        // decide if using .esc / .unEsc
        boolean force=val.contains("\r") || val.contains("\n") || val.contains("\t");
        boolean d=val.contains("\"");
        boolean s=val.contains("'");
        if (force || (d && s)) return '"' + escString(val) + '"' + ".unEsc";
        if (d) return "'" + val + "'";
        return "\"" + val + "\"";
    }

    
    
    
    @Override
    public boolean eq(Obj v) {
        return (v instanceof ValueString) && ((ValueString) v).getVal().equals(val);
    }

    @Override
    public boolean getValAsBoolean() {
        return true;
    }


    
    
    class FunctionSub extends Function {
        public String getName() {
            return "sub";
        }
        public String getShortDesc() {
            return "sub(start,end) or sub(start) - returns substring";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==2) {
                if (!isInt(params,0) || !isInt(params,1)) {
                    throw new Exception("Expected parameters: startPos, endPos");
                }
                int a=(int) getInt("startPos", params, 0);
                int b=(int) getInt("endPos", params, 1);
                
                if (a < 0) a=0;
                if (b > val.length()) b=val.length();
                if (a > b || a > val.length()) return new ValueString(""); 
                
                return new ValueString(val.substring(a,b));
                
            } else if (params.size()==1) {
                if (!isInt(params,0)) {
                    throw new Exception("Expected parameter: startPos");
                }
                int a=(int) getInt("startPos", params, 0);
                return new ValueString(val.substring(a));
                
            } else {
                throw new Exception("Expected parameters: (startPos,endPos) or (startPos)");
            }
        }

    }
    
    class FunctionLength extends Function {
        public String getName() {
            return "length";
        }
        public String getShortDesc() {
            return "length() - returns string length";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            return new ValueInt(val.length());
        }

    }
    
    class FunctionSplit extends Function {
        public String getName() {
            return "split";
        }
        public String getShortDesc() {
            return "split() or split(delimitersString) - returns list of strings";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            String delimiters;
            if (params.size()==0) {
                delimiters=" ";
            } else if (params.size()==1) {
                if (!isString(params,0)) {
                    throw new Exception("Expected parameter: delimeters");
                }
                delimiters=getString("delimiters", params, 0);
            } else {
                throw new Exception("Expected optional parameter delimiters (string)");
            }
            
            StringTokenizer st=new StringTokenizer(val, delimiters, false);
            List<Value> parts=new ArrayList<Value>();
            while (st.hasMoreTokens()) parts.add(new ValueString(st.nextToken()));
            
            return new ValueList(parts);
        }

    }
    
    class FunctionEndsWith extends Function {
        public String getName() {
            return "endsWith";
        }
        public String getShortDesc() {
            return "endsWith(str) - returns true or false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==1) {
                return new ValueBoolean(val.endsWith(params.get(0).getValAsString()));
            } else {
                throw new Exception("Expected one string parameter");
            }
        }
    }
    

    class FunctionStartsWith extends Function {
        public String getName() {
            return "startsWith";
        }
        public String getShortDesc() {
            return "startsWith(str) - returns true or false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==1) {
                return new ValueBoolean(val.startsWith(params.get(0).getValAsString()));
            } else {
                throw new Exception("Expected one string parameter");
            }
        }
    }
    

    class FunctionContains extends Function {
        public String getName() {
            return "contains";
        }
        public String getShortDesc() {
            return "contains(str) - returns true or false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()==1) {
                return new ValueBoolean(val.contains(params.get(0).getValAsString()));
            } else {
                throw new Exception("Expected one string parameter");
            }
        }
    }

    class FunctionToUpper extends Function {
        public String getName() {
            return "toUpper";
        }
        public String getShortDesc() {
            return "toUpper() - return string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(val.toUpperCase());
        }
    }
    

    class FunctionToLower extends Function {
        public String getName() {
            return "toLower";
        }
        public String getShortDesc() {
            return "toLower() - return string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(val.toLowerCase());
        }
    }
    

    class FunctionParseInt extends Function {
        public String getName() {
            return "parseInt";
        }
        public String getShortDesc() {
            return "parseInt(radix?) - returns int";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            int radix;
            if (params.size()==1) {
                if (!(params.get(0) instanceof ValueInt)) throw new Exception("Expected optional int parameter (radix)");
                radix=(int) ((ValueInt) params.get(0)).getVal();
            } else {
                radix=10;
            }
            return new ValueInt(Long.parseLong(val, radix));
        }
    }

    class FunctionParseFloat extends Function {
        public String getName() {
            return "parseFloat";
        }
        public String getShortDesc() {
            return "parseFloat() - returns float";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=0) throw new Exception("Expected no parameters");
            return new ValueFloat(Double.parseDouble(val));
        }
    }

    class FunctionTrim extends Function {
        public String getName() {
            return "trim";
        }
        public String getShortDesc() {
            return "trim() - return string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(val.trim());
        }
    }
        
    
    class FunctionReplaceChars extends Function {
        public String getName() {
            return "replaceChars";
        }
        public String getShortDesc() {
            return "replaceChars(a,b) - replace characters in a with b - returns string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) {
                throw new Exception("Expected parameters pre, post");
            }
            String a=params.get(0).getValAsString();
            String b=params.get(1).getValAsString();
            if (a.length()==0 || b.length()==0) throw new Exception("Invalid parameters, can not be empty strings");
            
            String x=val;
            for (int pos=0; pos<a.length(); pos++) {
                char ca=a.charAt(pos);
                char cb=b.charAt(pos % b.length());
                x=x.replace(ca,cb);
            }
            return new ValueString(x);
        }
    }
    

    class FunctionReplace extends Function {
        public String getName() {
            return "replace";
        }
        public String getShortDesc() {
            return "replace(a,b) - replace a with b - returns new string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) {
                throw new Exception("Expected parameters pre, post");
            }
            String a=params.get(0).getValAsString();
            String b=params.get(1).getValAsString();
            return new ValueString(val.replace(a, b));
        }
    }
    
    class FunctionMerge extends Function {
        public String getName() {
            return "merge";
        }
        public String getShortDesc() {
            return "merge(dict) - replace key with value from dictionary object - returns new string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) {
                throw new Exception("Expected parameter dict");
            }
            if (!(params.get(0) instanceof ValueObj)) throw new Exception("Expected parameter dict");
            Obj obj=((ValueObj) params.get(0)).getVal();
            if (!(obj instanceof ObjDict)) throw new Exception("Expected parameter dict");
            
            ObjDict dict=(ObjDict) obj;
            Iterator<String> keys=dict.getKeys();
            String s=val;
            while (keys.hasNext()) {
                String key=keys.next();
                Value v=dict.getValue(key);
                String mergeString;
                if (v instanceof ValueNull) {
                    mergeString="";
                } else {
                    mergeString=v.getValAsString();
                }
                s=s.replace(key, mergeString);
            }
            
            return new ValueString(s);
        }
    }
    
    
    class FunctionIndexOf extends Function {
        public String getName() {
            return "indexOf";
        }
        public String getShortDesc() {
            return "indexOf(str) - returns pos or -1 if no match";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=1) throw new Exception("Expected parameter 'str'");
            String str=getString("str", params, 0);
            int pos=val.indexOf(str);
            return new ValueInt(pos);
        }

    }
    
    class FunctionBetween extends Function {
        public String getName() {
            return "between";
        }
        public String getShortDesc() {
            return "between(pre,post) - return string between two given strings";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2) {
                throw new Exception("Expected parameters pre, post");
            }
            String a=params.get(0).getValAsString();
            String b=params.get(1).getValAsString();
            
            int pos=val.indexOf(a);
            int startPos;
            if (pos < 0) {
                startPos=0;
            } else {
                startPos=pos+a.length();
            }
            int endPos=val.indexOf(b,startPos);
            if (endPos < 0) endPos=val.length();
            
            return new ValueString(val.substring(startPos, endPos));
        }
    }


    class FunctionBefore extends Function {
        public String getName() {
            return "before";
        }
        public String getShortDesc() {
            return "before(str) - return string up to given string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) {
                throw new Exception("Expected parameter str");
            }
            String a=params.get(0).getValAsString();
            
            int pos=val.indexOf(a);
            if (pos < 0) return new ValueString(val);
            return new ValueString(val.substring(0,pos));
        }
    }

    class FunctionAfter extends Function {
        public String getName() {
            return "after";
        }
        public String getShortDesc() {
            return "after(str) - return string following given string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) {
                throw new Exception("Expected parameter str");
            }
            String a=params.get(0).getValAsString();
            
            int pos=val.indexOf(a);
            if (pos < 0) return new ValueString("");
            return new ValueString(val.substring(pos+a.length()));
        }
    }


    class FunctionBeforeLast extends Function {
        public String getName() {
            return "beforeLast";
        }
        public String getShortDesc() {
            return "beforeLast(str) - return string up to last position of given string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) {
                throw new Exception("Expected parameter str");
            }
            String a=params.get(0).getValAsString();
            
            int pos=val.lastIndexOf(a);
            if (pos < 0) return new ValueString(val);
            return new ValueString(val.substring(0,pos));
        }
    }

    class FunctionAfterLast extends Function {
        public String getName() {
            return "afterLast";
        }
        public String getShortDesc() {
            return "afterLast(str) - return string following last position of given string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) {
                throw new Exception("Expected parameter str");
            }
            String a=params.get(0).getValAsString();
            
            int pos=val.lastIndexOf(a);
            if (pos < 0) return new ValueString("");
            return new ValueString(val.substring(pos+a.length()));
        }
    }

    public static String escString(String s) {
        StringBuffer sb=new StringBuffer();
        for (int pos=0; pos<s.length(); pos++) {
            char c=s.charAt(pos);
            if (c=='"') sb.append("^q");
            else if (c=='\'') sb.append("^a");
            else if (c=='\\') sb.append("^b");
            else if(c=='\n') sb.append("^n");
            else if (c=='\r') sb.append("^r");
            else if (c=='\t') sb.append("^t");
            else if (c==' ') sb.append("^s");
            else if (c=='^') sb.append("^^");
            else sb.append(c);
        }
        return sb.toString();
    }
    
    public static String unEscString(String s) {
        boolean esc=false;
        StringBuffer sb=new StringBuffer();
        for (int pos=0; pos<s.length(); pos++) {
            char c=s.charAt(pos);
            if (esc) {
                if (c=='q') sb.append('"');
                else if (c=='a') sb.append('\'');
                else if (c=='b') sb.append('\\');
                else if (c=='n') sb.append('\n');
                else if (c=='r') sb.append('\r');
                else if (c=='t') sb.append('\t');
                else if (c=='s') sb.append(' ');
                else sb.append(c);
                esc=false;
            } else {
                if (c=='^') esc=true;
                else sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toHexString (String s) throws Exception {
        byte[] bytes=s.getBytes("UTF-8");
        return Hex.toHex(bytes);
    }
    
    public static String fromHexString (String s) throws Exception {
        byte[] bytes=Hex.fromHex(s);
        return new String(bytes,"UTF-8");
    }
    


    class FunctionEsc extends Function {
        public String getName() {
            return "esc";
        }
        public String getShortDesc() {
            return "esc() - convert to safe format, escaping quotes and special characters";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            return new ValueString(escString(val));
        }
    }

    class FunctionUnEsc extends Function {
        public String getName() {
            return "unEsc";
        }
        public String getShortDesc() {
            return "unEsc() - convert escaped string back to original content";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            return new ValueString(unEscString(val));
        }
    }



    class FunctionToHexString extends Function {
        public String getName() {
            return "toHexString";
        }
        public String getShortDesc() {
            return "toHexString() - convert to hex format";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            return new ValueString(toHexString(val));
        }
    }

    class FunctionFromHexString extends Function {
        public String getName() {
            return "fromHexString";
        }
        public String getShortDesc() {
            return "fromHexString() - convert hex string back to original content";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            return new ValueString(fromHexString(val));
        }
    }




    
    class FunctionHash extends Function {
        public String getName() {
            return "hash";
        }
        public String getShortDesc() {
            return "hash() - create hash string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");  // 32 bytes hash
            digest.update(val.getBytes("UTF-8"));
            byte[] hash=digest.digest();
    
            String digits="0123456789abcdef";
            StringBuffer sb=new StringBuffer();
            for (int i=0; i<hash.length; i++) {
                byte b=hash[i];
                sb.append(digits.charAt( (b>>4) & 0x0F ));
                sb.append(digits.charAt( b & 0x0F ));
            }
            
        
            return new ValueString(sb.toString());
        }
    }
    
    
    class FunctionChars extends Function {
        public String getName() {
            return "chars";
        }
        public String getShortDesc() {
            return "chars() returns list of characters";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            String delimiters;
            if (params.size()!=0) throw new Exception("Expected no parameters");
            List<Value> parts=new ArrayList<Value>();
            for (int i=0; i<val.length(); i++) {
                parts.add(new ValueString(""+val.charAt(i)));
            }
            return new ValueList(parts);
        }

    }
    
    class FunctionGetBytes extends Function {
        public String getName() {
            return "getBytes";
        }
        public String getShortDesc() {
            return "getBytes(charSet) - returns Binary value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=1) throw new Exception("Expected parameter charset");
            String charSet=getString("charset",params,0);
            
            byte[] bytes = val.getBytes(charSet);
            return new ValueBinary(bytes);
        }

    }
    
    class FunctionLast extends Function {
        public String getName() {
            return "last";
        }
        public String getShortDesc() {
            return "last(count) - returns 'count' last characters if possible";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=1) throw new Exception("Expected parameter count");
            int count=(int) getInt("count",params,0);
            if (count >= val.length()) {
                return new ValueString(val);
            } 
            return new ValueString(val.substring(val.length()-count));
        }
    }

    class FunctionFirst extends Function {
        public String getName() {
            return "first";
        }
        public String getShortDesc() {
            return "first(count) - returns 'count' first characters if possible";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size()!=1) throw new Exception("Expected parameter count");
            int count=(int) getInt("count",params,0);
            if (count >= val.length()) {
                return new ValueString(val);
            } 
            return new ValueString(val.substring(0,count));
        }
    }


    class FunctionPrintable extends Function {
        public String getName() {
            return "printable";
        }
        public String getShortDesc() {
            return "printable() - returns string with printable characters";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            StringBuffer sb=new StringBuffer();
            for (int i=0; i<val.length(); i++) {
                char c=val.charAt(i);
                int x=(int) c;
                boolean keep=
                        // TAB=9, CR=13, LF=10
                        (x>31 || x==9 || x==13 || x==10)
                        &&
                        (x != 127)
                        &&
                        (x<128 || x>159)
                        ;

                if (keep) {
                    sb.append(c);
                }
            }
            return new ValueString(sb.toString());
        }
    }
    
    
    
    class FunctionMergeExpr extends Function {
        public String getName() {
            return "mergeExpr";
        }
        public String getShortDesc() {
            return "mergeExpr([before,after]) - replace <<x>> with result from eval(x) - returns list of strings";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            String before="<<";
            String after=">>";
            if (params.size() == 0) {
                // ok
            } else if (params.size() == 2) {
                before=getString("before", params, 0);
                after=getString("after", params, 1);
            } else {
                throw new Exception("Expected either 0 or 2 parameters (before- and after-markers for code segments inside string)");
            }
            
            
            List<String> resultList = ValueString.mergeExpr(val, before, after, ctx);
            
            List<Value> x=new ArrayList<Value>();
            for (String s:resultList) x.add(new ValueString(s));
            return new ValueList(x);
            
        }
        
    }
    
    public static List<String> mergeExpr (String val, String before, String after, Ctx ctx) throws Exception {
        List<String> resultList=new ArrayList<String>();
        
        StringBuffer sb=new StringBuffer();
        int currPos=0;
        while (currPos < val.length()) {
            int pos=val.indexOf(before, currPos);
            if (pos < 0) {
                sb.append(val.substring(currPos));
                break;
            }
            int pos2=val.indexOf(after, currPos+before.length());
            if (pos2 < 0) {
                sb.append(val.substring(currPos));
                break;
            }
            // found something
            sb.append(val.substring(currPos,pos));
            currPos=pos2+after.length();
            String expr=val.substring(pos+before.length(),pos2);
            Value v=ctx.subNewData(true).resolveCodeSpaceString(expr);
            
            if (v instanceof ValueList) {
                List<String> renderResult=new ArrayList<String>();
                render(renderResult, (ValueList) v);
                
                if (renderResult.size()==0) {
                    // ignore
                } else if (renderResult.size()==1) { 
                    // special treatment, to insert value into string, without newlines
                    sb.append(renderResult.get(0));
                } else {
                    sb.append(renderResult.get(0));
                    for (int i=1; i<renderResult.size(); i++) {
                        String line=renderResult.get(i);
                        resultList.add(sb.toString());
                        sb=new StringBuffer();
                        sb.append(line);
                    }
                }
            } else if (v instanceof ValueNull) {
                // ignore
            } else {
                // all others
                sb.append(v.getValAsString());
            }
        }
        resultList.add(sb.toString());
        return resultList;

    }

    
    class FunctionTimes extends Function {
        public String getName() {
            return "times";
        }
        public String getShortDesc() {
            return "times(Count) - returns string repeated Count times";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            long count=getInt("Count", params, 0);
            StringBuffer sb=new StringBuffer();
            for (int i=0; i<count; i++) {
                sb.append(val);
            }
            return new ValueString(sb.toString());
        }
    }
    
    

    
    /**
     * Process list, possibly containing inner lists, into single
     * list of values (recursive)
     */
    private static void render (List<String> result, ValueList list) throws Exception {
        for (Value v:list.getVal()) {
            if (v instanceof ValueList) {
                render(result,(ValueList) v);
            } else if (v instanceof ValueNull) {
                // ignore
            } else {
                result.add(v.getValAsString());
            }
        } 
    }


}
