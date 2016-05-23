package fredboat.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class TextUtils {

    public static Message prefaceWithMention(User user, String msg) {
        MessageBuilder builder = new MessageBuilder().appendMention(user).appendString(msg);
        return builder.build();
    }

    public static Message replyWithMention(TextChannel channel, User user, String msg) {
        MessageBuilder builder = new MessageBuilder().appendMention(user).appendString(msg);
        Message mes = builder.build();
        channel.sendMessage(mes);
        return mes;
    }

    public static void handleException(Exception e, MessageChannel channel, User invoker) {
        MessageBuilder builder = new MessageBuilder();

        builder.appendMention(invoker);
        builder.appendString(" an error occured :anger: ```java\n" + e.toString() + "\n");

        //builder.appendString("```java\n");
        for (StackTraceElement ste : e.getStackTrace()) {
            builder.appendString("\t" + ste.toString() + "\n");
            if ("prefixCalled".equals(ste.getMethodName())) {
                break;
            }
        }
        builder.appendString("\t...```");

        try {
            channel.sendMessage(builder.build());
        } catch (UnsupportedOperationException tooLongEx) {
            channel.sendMessage("An error occured :anger: Error was too long to display.");
            e.printStackTrace();
        }
    }
    
    public static String postToHastebin(String body) throws UnirestException {
        return Unirest.post("http://hastebin.com/documents").body(body).asJson().getBody().getObject().getString("key");
    }
    
    public static String postToHastebin(String body, boolean asURL) throws UnirestException {
        if(asURL){
            return "http://hastebin.com/" + postToHastebin(body);
        } else {
            return postToHastebin(body);
        }
    }
}
