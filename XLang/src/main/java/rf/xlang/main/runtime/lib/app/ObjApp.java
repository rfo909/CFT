/*
CFT - an interactive programmable shell for automation
Copyright (C) 2020-2025 Roar Foshaug

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

package rf.xlang.main.runtime.lib.app;

import java.util.HashMap;

import rf.xlang.main.Ctx;
import rf.xlang.main.ObjGlobal;
import rf.xlang.main.runtime.*;
import rf.xlang.parsetree.TupleType;
import java.util.*;

public class ObjApp extends Obj {

    public ObjApp(ObjGlobal objGlobal) {
    }

    @Override
    public String getTypeName() {
        return "App";
    }

    @Override
    public boolean eq(Obj v) {
        return false;
    }

    public boolean getValAsBoolean() {
        return true;
    }

}
