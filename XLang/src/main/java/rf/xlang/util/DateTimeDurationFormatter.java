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

package rf.xlang.util;

public class DateTimeDurationFormatter {
    long millis, seconds, minutes, hours, days;
    public DateTimeDurationFormatter(long timeValue) {
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
        if (days > 740) return (days/365) + "y";
        if (days > 185) return (days/30) + "mo"; 
        if (days > 5) return days+"d";
        if (days > 0) return days+"d" + hours + "h";
        if (hours > 0) return hours + "h" + f(minutes,2)+"m";
        if (minutes > 0) return minutes+"m"+f(seconds,2)+"s";
        if (seconds >= 10) return seconds+"s";
        return seconds+"."+f(millis,3) + "s";
    }

}
