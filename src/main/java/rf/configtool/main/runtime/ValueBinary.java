package rf.configtool.main.runtime;

import java.security.MessageDigest;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.util.Hex;

public class ValueBinary extends Value {
    
    private byte[] val;
    private boolean secure;

    public ValueBinary (byte[] val) {
        this(val,false);
    }

    public ValueBinary (byte[] val, boolean secure) {
        this.val=val;
        this.secure=secure;
        if (!secure) {
            add (new FunctionHex());
            add (new FunctionLength());
            add (new FunctionToString());
            add (new FunctionPrintableChars());
            add (new FunctionHash());
        }
    }
    
    public void validateNonSecure (String msg) throws Exception {
        if (secure) throw new Exception("ValueBinary is flagged as secure, no access: " + msg);
    }
    
    public byte[] getVal() {
        return val;
    }
    
    @Override
    public String getTypeName() {
        return "Binary";
    }

    @Override
    public String getValAsString() {
        if (secure) {
            return "0x???";
        } else {
            return "0x...";
        }
    }
    

    // No synthesis - but IF added, make sure it fails when secure flag is set!
    
    
    
    @Override
    public boolean eq(Obj v) {
        if (v==this) return true;
        if (v instanceof ValueBinary) {
            byte[] data=((ValueBinary)v).val;
            if (data.length != val.length) return false;
            for (int i=0; i<val.length; i++) {
                if (data[i] != val[i]) return false;
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean getValAsBoolean() {
        return true;
    }


    
    class FunctionHex extends Function {
        public String getName() {
            return "hex";
        }
        public String getShortDesc() {
            return "hex() - Returns hex string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(Hex.toHex(val));
        }

    }
    
    class FunctionLength extends Function {
        public String getName() {
            return "length";
        }
        public String getShortDesc() {
            return "length() - returns number of bytes";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(val.length);
        }

    }
    
    class FunctionToString extends Function {
        public String getName() {
            return "toString";
        }
        public String getShortDesc() {
            return "toString(encoding) - returns String";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected encoding parameter");
            String encoding=getString("encoding",params,0);
            return new ValueString(new String(val,encoding));
        }

    }
    
    class FunctionPrintableChars extends Function {
        public String getName() {
            return "printableChars";
        }
        public String getShortDesc() {
            return "printableChars() - returns String with printable (7-bits) characters";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            StringBuffer sb=new StringBuffer();
            for (int i=0; i<val.length; i++) {
                int x=(int) val[i];
                String ch=getChar(x);
                if (ch != null) sb.append(ch);
            }
            return new ValueString(sb.toString());
        }
        
        private String getChar(int i) {
            if (i==9) {
                return "\t";
            }
            if (i==13) {
                return "\r";
            }
            if (i==10) {
                return "\n";
            }

            if (i>=32 && i <= 126) {
                byte[] b = { (byte) i };
                try {
                    String s=new String(b,"ISO-8859-1");
                    if (s.length() != 1) return null;
                    return s;
                } catch (Exception ex) {
                    return null;
                }
            } else return null;
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
            digest.update(val);
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
    
    
}
