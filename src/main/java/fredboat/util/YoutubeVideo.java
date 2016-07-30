package fredboat.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeVideo {
    
    public String id = null;
    public String name = null;
    public String duration = null;//Youtube has strange duration strings suchs as PT2H3M33S

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDuration() {
        return duration;
    }
    
    public int getDurationHours() {
        Pattern pat = Pattern.compile("(\\d+)H");
        Matcher matcher = pat.matcher(duration);
        
        if(matcher.find()){
            return Integer.valueOf(matcher.group(1));
        } else {
            return 0;
        }
    }
    
    public int getDurationMinutes() {
        Pattern pat = Pattern.compile("(\\d+)M");
        Matcher matcher = pat.matcher(duration);
        
        if(matcher.find()){
            return Integer.valueOf(matcher.group(1));
        } else {
            return 0;
        }
    }
    
    public int getDurationSeconds() {
        Pattern pat = Pattern.compile("(\\d+)S");
        Matcher matcher = pat.matcher(duration);
        
        if(matcher.find()){
            return Integer.valueOf(matcher.group(1));
        } else {
            return 0;
        }
    }
    
    public String getDurationFormatted(){
        if(getDurationHours() == 0){
            return forceTwoDigits(getDurationMinutes()) + ":" + forceTwoDigits(getDurationSeconds());
        } else {
            return forceTwoDigits(getDurationHours()) + ":" + forceTwoDigits(getDurationMinutes()) + ":" + forceTwoDigits(getDurationSeconds());
        }
    }
    
    private String forceTwoDigits(int i){
        if(i < 10){
            return "0" + i;
        } else {
            return String.valueOf(i);
        }
    }

    @Override
    public String toString() {
        return "[YoutubeVideo:"+id+"]";
    }
    
}
