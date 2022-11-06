package rf.configtool.util;

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
        if (hours > 0) return f(hours,2) + "h" + f(minutes,2)+"m";
        if (minutes > 0) return f(minutes,2)+"m"+f(seconds,2)+"s";
        return seconds+"."+f(millis,3) + "s";
    }

}
