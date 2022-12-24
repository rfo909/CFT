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

package rf.configtool.main.runtime.lib;

import java.util.List;

import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueObj;

public class ObjClosure extends Obj {
    
    private ObjDict dict;
    private ValueBlock lambda;
    
    public ObjClosure(ObjDict dict, ValueBlock lambda) {
        this.dict=dict;
        this.lambda=lambda;
        this.add(new FunctionCall());
        this.add(new FunctionDict());
        this.add(new FunctionLambda());
    }
    
    public ValueBlock getLambda() {
        return lambda;
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "Callable";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Callable";
    }
    
    private ObjClosure theObj () {
        return this;
    }
    

    class FunctionCall extends Function {
        public String getName() {
            return "call";
        }
        public String getShortDesc() {
            return "call(...) - call closure with parameters";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            CFTCallStackFrame caller=new CFTCallStackFrame("ObjClosure.FunctionCall()","Calling closure");

            return callClosure(ctx,caller,params);
        }
    }
    
    class FunctionLambda extends Function {
        public String getName() {
            return "lambda";
        }
        public String getShortDesc() {
            return "lambda() - extract the lambda from the closure";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return lambda;
        }
    }

    class FunctionDict extends Function {
        public String getName() {
            return "dict";
        }
        public String getShortDesc() {
            return "dict() - extract the dictionary from the closure";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            return new ValueObj(dict);
        }
    }

    public Value callClosure (Ctx ctx, CFTCallStackFrame caller, List<Value> params) throws Exception {
        return lambda.callLambda(ctx, caller, dict, params);
    }

}
