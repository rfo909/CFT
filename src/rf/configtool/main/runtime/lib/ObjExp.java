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

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.CtxCloseHook;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueBlock;

/**
 * Experimental code
 */
public class ObjExp extends Obj {
    
    public ObjExp() {
        add(new FunctionOnCtxClose());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    @Override
    public String getTypeName() {
        return "Exp";
    }
    

    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Exp";
    }
    
    private Obj self () {
        return this;
    }
    
     
	class FunctionOnCtxClose extends Function {
		public String getName() {
			return "onCtxClose";
		}

		public String getShortDesc() {
			return "onCtxClose(macro) - add macro to run on context close";
		}
		
		class Hook implements CtxCloseHook {
			private ValueBlock m;
			public Hook (ValueBlock m) {
				this.m=m;
			}
			public void ctxClosing(Ctx ctx) throws Exception {
				m.callLambda(ctx,null);
			}

		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			if (params.size() != 1)
				throw new Exception("Expected macro parameter");
			Value v=params.get(0);
			if (!(v instanceof ValueBlock)) throw new Exception("Expected macro parameter");
			
			ctx.addCtxCloseHook(new Hook( (ValueBlock) v));
			return new ValueBoolean(true);
		}
	}


}


