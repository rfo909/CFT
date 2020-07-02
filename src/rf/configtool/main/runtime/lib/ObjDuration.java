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

import java.text.SimpleDateFormat;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.*;

/**
 * Duration can only be created from inside Date (by subtracting two dates etc)
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
            return days + " days " + f(hours,2) + ":" + f(minutes,2) + ":" + f(seconds,2) + "." + f(millis,3);
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

}
