/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

package rf.configtool.parsetree;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.Protection;

/**
 * Return directory object for current directory
 */
public class ExprPwd extends ExprCommon {

    public ExprPwd (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("pwd","expected 'pwd'");
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        String currDir=ctx.getObjGlobal().getCurrDir();
        ctx.getObjGlobal().addSystemMessage(currDir);
        return new ValueObj(new ObjDir(currDir,Protection.NoProtection));
    }
}
