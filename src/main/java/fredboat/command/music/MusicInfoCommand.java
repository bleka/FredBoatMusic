package fredboat.command.music;

import com.mashape.unirest.http.exceptions.UnirestException;
import fredboat.commandmeta.Command;
import fredboat.util.TextUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.RemoteSource;

public class MusicInfoCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        String url = args[1];
        RemoteSource rs = new RemoteSource(url);
        
        AudioInfo rsinfo = rs.getInfo();
        if(rsinfo.getError() != null){
            channel.sendMessage(rsinfo.getError());
        } else {
            try {
                channel.sendMessage(TextUtils.postToHastebin(rsinfo.getJsonInfo().toString(), true));
            } catch (UnirestException ex) {
                throw new RuntimeException();
            }
        }
    }
    
}
