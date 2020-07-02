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

import java.util.List;

import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueString;

public class ObjCfg extends Obj {
    
    private int h=24;
    private int w=130;
    private boolean wrap=false;
    
    public ObjCfg() {
        this.add(new FunctionW());
        this.add(new FunctionH());
        this.add(new FunctionWrap());
        this.add(new FunctionShow());
    }
    
    public int getScreenWidth() {
        if (!wrap) return w;
        return 999999;
    }

    public int getScreenHeight() {
        return h;
    }
    
    public boolean changeWrap() {
        wrap=!wrap;
        return wrap;
    }

    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "Cfg";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(h+"x"+w).regular(":wrap="+wrap);
    }

    
    private String getDesc() {
        return "Cfg";
    }
    
    private Obj theObj () {
        return this;
    }
    

    class FunctionW extends Function {
        public String getName() {
            return "w";
        }
        public String getShortDesc() {
            return "w(val?) - get or set width of screen in columns";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) {
                // doing direct output, to avoid problems with formatting
                StringBuffer sb=new StringBuffer();
                for (int i=1; i<30; i++) {
                    String s="...................."+(i*10);
                    s=s.substring(s.length()-10);
                    sb.append(s);
                }
                ctx.getStdio().println(sb.toString());
                ctx.getStdio().println("w="+w);
                
            } else if (params.size()==1) {
                w=(int) getInt("val",params,0);
            } else {
                throw new Exception("Expected optional integer value");
            }
            return new ValueInt(w);
        }
    }

    class FunctionH extends Function {
        public String getName() {
            return "h";
        }
        public String getShortDesc() {
            return "h(val?) - get or set height of screen in rows";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) {
                for (int i=100; i>=5; i--) {
                    ctx.getObjGlobal().getStdio().println(""+i);
                }
                // doing direct output, to avoid problems with formatting
                ctx.getObjGlobal().getStdio().println("h="+h);
            } else if (params.size()==1) {
                h=(int) getInt("val",params,0);
            } else {
                throw new Exception("Expected optional integer value");
            }
            return new ValueInt(h);
        }
    }

    class FunctionWrap extends Function {
        public String getName() {
            return "wrap";
        }
        public String getShortDesc() {
            return "wrap(boolean) - set line wrap mode (default false)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected boolean parameter");
            wrap=getBoolean("value", params, 0);
            return new ValueBoolean(wrap);
        }
    }

    class FunctionShow extends Function {
        public String getName() {
            return "show";
        }
        public String getShortDesc() {
            return "show() - show active area";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            Stdio stdio=ctx.getStdio();
            
            String s="";
            for (int i=0; i<w; i++) s+="#";
            for (int i=0; i<h; i++) {
                stdio.println(s);
            }
            
            return new ValueString(h+"x"+w);
        }
    }


}
    

