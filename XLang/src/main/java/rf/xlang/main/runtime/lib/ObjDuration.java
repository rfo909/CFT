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

package rf.xlang.main.runtime.lib;

import java.util.List;

import rf.xlang.main.Ctx;
import rf.xlang.main.runtime.Function;
import rf.xlang.main.runtime.Obj;
import rf.xlang.main.runtime.Value;
import rf.xlang.main.runtime.ValueInt;
import rf.xlang.main.runtime.ValueObj;
import rf.xlang.main.runtime.ValueString;

import rf.xlang.util.DateTimeDurationFormatter;

/**
 * Duration is an absolute value, no negatives allowed, for addition or subtraction from Date values.
 */
public class ObjDuration extends Obj {
    
    private long timeValue;
    private String dateFmt="yyyy-MM-dd HH:mm:ss";

    
    public ObjDuration () {
        this(0L);
    }
    

    public ObjDuration (long timeValue) {
        this.timeValue=timeValue;
        preventNegative();
    
        add(new FunctionGet());
        add(new FunctionDays());
        add(new FunctionHours());
        add(new FunctionMinutes());
        add(new FunctionSeconds());
        add(new FunctionMillis());
        add(new FunctionFmt());
        add(new FunctionAsDays());
        add(new FunctionAsHours());
        add(new FunctionAsMinutes());
        add(new FunctionAsSeconds());
        
    }
    
    private void preventNegative() {
        if (timeValue < 0) timeValue=0;
    }
    
    public long getTimeValue() {
        return timeValue;
    }
    
    private Obj theObj() {
        return this;
    }

    @Override
    public boolean eq(Obj x) {
        if (x==this) return true;
        if (x instanceof ObjDuration && ((ObjDuration) x).timeValue==timeValue) return true;
        return false;
    }


    @Override
    public String getTypeName() {
        return "Duration";
    }

    

    class FunctionGet extends Function {
        public String getName() {
            return "get";
        }
        public String getShortDesc() {
            return "get() - get int value (milliseconds)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(timeValue);
        }
    }

    class FunctionDays extends Function {
        public String getName() {
            return "days";
        }
        public String getShortDesc() {
            return "days(val) - add days - returns this";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected int parameter");
            timeValue += getInt("value", params, 0)*24*60*60*1000;
            preventNegative();
            return new ValueObj(theObj());
        }
    }

    class FunctionHours extends Function {
        public String getName() {
            return "hours";
        }
        public String getShortDesc() {
            return "hours(val) - add hours - returns this";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected int parameter");
            timeValue += getInt("value", params, 0)*60*60*1000;
            preventNegative();
            return new ValueObj(theObj());
        }
    }

    class FunctionMinutes extends Function {
        public String getName() {
            return "minutes";
        }
        public String getShortDesc() {
            return "minutes(val) - add minutes - returns this";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected int parameter");
            timeValue += getInt("value", params, 0)*60*1000;
            preventNegative();
            return new ValueObj(theObj());
        }
    }

    class FunctionSeconds extends Function {
        public String getName() {
            return "seconds";
        }
        public String getShortDesc() {
            return "seconds(val) - add seconds - returns this";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected int parameter");
            timeValue += getInt("value", params, 0)*1000;
            preventNegative();
            return new ValueObj(theObj());
        }
    }

    class FunctionMillis extends Function {
        public String getName() {
            return "millis";
        }
        public String getShortDesc() {
            return "millis(val) - add millis - returns this";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected int parameter");
            timeValue += getInt("value", params, 0);
            preventNegative();
            return new ValueObj(theObj());
        }
    }

    class FunctionFmt extends Function {
        public String getName() {
            return "fmt";
        }
        public String getShortDesc() {
            return "fmt() - returns formatted string presentation";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
            return new ValueString(""+(new DateTimeDurationFormatter(timeValue)).fmt());
        }
    }

    
    class FunctionAsDays extends Function {
        public String getName() {
            return "asDays";
        }
        public String getShortDesc() {
            return "asDays() - return duration as number of days";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            long days=timeValue/(86400*1000);
            return new ValueInt(days);
        }
    }

    class FunctionAsHours extends Function {
        public String getName() {
            return "asHours";
        }
        public String getShortDesc() {
            return "asHours() - return duration as number of hours";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            long hours=timeValue/(3600*1000);
            return new ValueInt(hours);
        }
    }

    class FunctionAsMinutes extends Function {
        public String getName() {
            return "asMinutes";
        }
        public String getShortDesc() {
            return "asMinutes() - return duration as number of minutes";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            long minutes=timeValue/(60*1000);
            return new ValueInt(minutes);
        }
    }


    class FunctionAsSeconds extends Function {
        public String getName() {
            return "asSeconds";
        }
        public String getShortDesc() {
            return "asSeconds() - return duration as number of seconds";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            long seconds=timeValue/1000;
            return new ValueInt(seconds);
        }
    }


    
}
