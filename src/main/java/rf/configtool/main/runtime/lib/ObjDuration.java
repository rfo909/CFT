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
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;

/**
 * Duration is an absolute value, no negatives allowed, for addition or subtraction from Date values.
 */
public class ObjDuration extends Obj {
    
    private long timeValue;
    private String dateFmt="yyyy-MM-dd HH:mm:ss";

    class Details {
        long millis, seconds, minutes, hours, days;
        public Details(long timeValue) {
            long x=timeValue;
            millis=x % 1000;  x/=1000; // x is now seconds
            seconds=x % 60;   x/=60;   // x is now minutes
            minutes=x % 60;   x/=60;   // x is now hours
            hours=x%24;       x/=24;   // x is now days
            days=x;
        }
        private String f(long x, int n) {
            String s=""+x;
            while (s.length() < n) s="0"+s;
            return s;
        }
        
        public String fmt() {
        	if (days > 185) return (days/30) + "mo"; 
        	if (days > 2) return days+"d";
        	if (days > 0) return days+"d " + hours + "h";
        	if (hours > 0) return f(hours,2) + ":" + f(minutes,2);
        	if (minutes > 0) return f(hours,2)+":" + f(minutes,2)+":"+f(seconds,2);
        	return seconds+"."+f(millis,3) + "s";
        }
    }
    
    
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
    public String synthesize() throws Exception {
        return "Date.Duration(" + timeValue + ")";
    }


    @Override
    public String getTypeName() {
        return "Duration";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular("Duration: " + (new Details(timeValue)).fmt());
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
            
            return new ValueString(""+(new Details(timeValue)).fmt());
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


    
}
