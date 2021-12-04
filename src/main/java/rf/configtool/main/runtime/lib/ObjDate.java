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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueNull;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;


public class ObjDate extends Obj {
    
    private long timeValue;
    private String dateFmt="yyyy-MM-dd HH:mm:ss";

    
    public ObjDate() {
        this(System.currentTimeMillis());
    }
    
    public ObjDate (long timeValue) {
        this.timeValue=timeValue;
        
		Function[] arr={
				new FunctionSetFormat(),
				new FunctionGet(),
				new FunctionSet(),
				new FunctionParse(),
				new FunctionFmt(),
				new FunctionYear(),
				new FunctionMonth(),
				new FunctionDayOfMonth(),
				new FunctionDayOfYear(),
				new FunctionHour(),
				new FunctionMinute(),
				new FunctionSecond(),
				new FunctionMillisecond(),
				new FunctionDuration(),
				new FunctionAdd(),
				new FunctionSub(),
				new FunctionDiff(),
				new FunctionAfter(),
				new FunctionBefore(),
				new FunctionGetFormat(),
		};
		setFunctions(arr);
        
    }
    
    private ObjDate theObj() {
        return this;
    }
    
    private String getFormattedDate() {
        SimpleDateFormat sdf=new SimpleDateFormat(dateFmt);
        return sdf.format(new Date(timeValue));
    }
    
    public long getTimeValue() {
        return timeValue;
    }
    

    @Override
    public boolean eq(Obj x) {
        if (x==this) return true;
        if (x instanceof ObjDate && ((ObjDate) x).timeValue==timeValue) return true;
        return false;
    }

    @Override
    public String getTypeName() {
        return "Date";
    }
    
    @Override
    public ColList getContentDescription() {
        String s="<undefined>";
        if (timeValue != 0) {
            s=getFormattedDate();
        }
        return ColList.list().regular(s);
    }
        
    @Override
    public String synthesize() throws Exception {
        return "Date(" + timeValue + ")";
    }

    
    
    class FunctionSetFormat extends Function {
        public String getName() {
            return "setFormat";
        }
        public String getShortDesc() {
            return "setFormat(str) - set date/time format according to SimpleDateFormat - return self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected one parameter: date/time format string");
            if (!(params.get(0) instanceof ValueString)) throw new Exception("Expected one parameter: date/time format string"); 
            dateFmt=((ValueString)params.get(0)).getVal();
            return new ValueObj(theObj());
        }
    }
    

    

    class FunctionGet extends Function {
        public String getName() {
            return "get";
        }
        public String getShortDesc() {
            return "get() - get int value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(timeValue);
        }
    }


    class FunctionSet extends Function {
        public String getName() {
            return "set";
        }
        public String getShortDesc() {
            return "set(value) - set int value (milliseconds) - returns this";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected int parameter");
            timeValue=getInt("value", params, 0);
            return new ValueObj(theObj());
        }
    }


    class FunctionParse extends Function {
        public String getName() {
            return "parse";
        }
        public String getShortDesc() {
            return "parse(value) or parse(value,default) - parse string according to current format - returns this or default if failing";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1 && params.size() != 2) throw new Exception("Expected parameters value and optional default");
            String s=getString("value", params, 0);
            Value defaultValue=new ValueNull();
            if (params.size()==2) defaultValue=params.get(1);
            
            SimpleDateFormat sdf=new SimpleDateFormat(dateFmt);
            try {
                long val=sdf.parse(s).getTime();
                timeValue=val;
                return new ValueObj(theObj());
            } catch (Exception ex) {
                return defaultValue;
            }
        }
    }


    class FunctionFmt extends Function {
        public String getName() {
            return "fmt";
        }
        public String getShortDesc() {
            return "fmt() - returns formatted date string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            SimpleDateFormat sdf=new SimpleDateFormat(dateFmt);
            return new ValueString(sdf.format(new Date(timeValue)));
        }
    }


    private int getCalendarValue (int what) {
        Calendar.getInstance();
        Calendar cal=Calendar.getInstance();
        cal.setTime(new Date(timeValue));
        return cal.get(what);
    }
    
    class FunctionYear extends Function {
        public String getName() {
            return "year";
        }
        public String getShortDesc() {
            return "year() - return year";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(getCalendarValue(Calendar.YEAR));
        }
    }

    class FunctionMonth extends Function {
        public String getName() {
            return "month";
        }
        public String getShortDesc() {
            return "month() - return month (1-based)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(getCalendarValue(Calendar.MONTH)+1);
        }
    }

    class FunctionDayOfMonth extends Function {
        public String getName() {
            return "dayOfMonth";
        }
        public String getShortDesc() {
            return "dayOfMonth() - return day of month";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(getCalendarValue(Calendar.DAY_OF_MONTH));
        }
    }

    class FunctionDayOfYear extends Function {
        public String getName() {
            return "dayOfYear";
        }
        public String getShortDesc() {
            return "dayOfYear() - return day of month";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(getCalendarValue(Calendar.DAY_OF_YEAR));
        }
    }

    class FunctionHour extends Function {
        public String getName() {
            return "hour";
        }
        public String getShortDesc() {
            return "hour() - return hour of day (24-based)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(getCalendarValue(Calendar.HOUR_OF_DAY));
        }
    }

    class FunctionMinute extends Function {
        public String getName() {
            return "minute";
        }
        public String getShortDesc() {
            return "minute() - return minute of hour";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(getCalendarValue(Calendar.MINUTE));
        }
    }

    class FunctionSecond extends Function {
        public String getName() {
            return "second";
        }
        public String getShortDesc() {
            return "second() - return second of minute";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(getCalendarValue(Calendar.SECOND));
        }
    }

    class FunctionMillisecond extends Function {
        public String getName() {
            return "millisecond";
        }
        public String getShortDesc() {
            return "millisecond() - return millisecond";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(getCalendarValue(Calendar.MILLISECOND));
        }
    }

    class FunctionDuration extends Function {
        public String getName() {
            return "Duration";
        }
        public String getShortDesc() {
            return "Duration(int?) - create duration object, used in date arithmethic";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() == 0) {
                return new ValueObj(new ObjDuration());
            } else if (params.size()==1) {
                long val=getInt("int",params,0);
                return new ValueObj(new ObjDuration(val));
            } else {
                throw new Exception("Expected zero parameters or single int value");
            }
        }
    }

    class FunctionAdd extends Function {
        public String getName() {
            return "add";
        }
        public String getShortDesc() {
            return "add(duration) - add duration to this Date, return new Date";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected duration parameter");
            Obj obj=getObj("duration",params,0);
            if (!(obj instanceof ObjDuration)) throw new Exception("Expected duration parameter");
            long t=timeValue+((ObjDuration) obj).getTimeValue();
            return new ValueObj(new ObjDate(t));
        }
    }


    class FunctionSub extends Function {
        public String getName() {
            return "sub";
        }
        public String getShortDesc() {
            return "sub(duration) - subtract duration from this Date, return new Date";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected duration parameter");
            Obj obj=getObj("duration",params,0);
            if (!(obj instanceof ObjDuration)) throw new Exception("Expected duration parameter");
            long t=timeValue-((ObjDuration) obj).getTimeValue();
            return new ValueObj(new ObjDate(t));
        }
    }


    class FunctionDiff extends Function {
        public String getName() {
            return "diff";
        }
        public String getShortDesc() {
            return "diff(date) - create duration corresponding to distance between this date and given date (absolute)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected date parameter");
            Obj obj=getObj("date",params,0);
            if (!(obj instanceof ObjDate)) throw new Exception("Expected date parameter");
            long t=((ObjDate) obj).timeValue-timeValue;
            if (t<0) t=-t;
            return new ValueObj(new ObjDuration(t));
        }
    }


    class FunctionAfter extends Function {
        public String getName() {
            return "after";
        }
        public String getShortDesc() {
            return "after(date) - return true or false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected date parameter");
            Obj obj=getObj("date",params,0);
            if (!(obj instanceof ObjDate)) throw new Exception("Expected date parameter");
            boolean b=timeValue>((ObjDate) obj).timeValue;
            return new ValueBoolean(b);
        }
    }


    class FunctionBefore extends Function {
        public String getName() {
            return "before";
        }
        public String getShortDesc() {
            return "before(date) - return true or false";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected date parameter");
            Obj obj=getObj("date",params,0);
            if (!(obj instanceof ObjDate)) throw new Exception("Expected date parameter");
            boolean b=timeValue<((ObjDate) obj).timeValue;
            return new ValueBoolean(b);
        }
    }

    class FunctionGetFormat extends Function {
        public String getName() {
            return "getFormat";
        }
        public String getShortDesc() {
            return "getFormat() - returns format string";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueString(dateFmt);
        }
    }


}
