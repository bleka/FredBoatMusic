package fredboat.command.maintenance;

import com.mashape.unirest.http.exceptions.UnirestException;
import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import fredboat.commandmeta.ICommandOwnerRestricted;
import fredboat.util.TextUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

public class PlayerDebugCommand extends Command implements ICommandOwnerRestricted {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        JSONArray a = new JSONArray();
        
        for(GuildPlayer gp : PlayerRegistry.getRegistry().values()){
            JSONObject data = new JSONObject();
            data.put("name", gp.getGuild().getName());
            data.put("id", gp.getGuild().getId());
            data.put("users", gp.getChannel().getUsers().toString());
            data.put("isPlaying", gp.isPlaying());
            data.put("isPaused", gp.isPaused());
            data.put("isStopped", gp.isStopped());
            data.put("songCount", gp.getSongCount());
            
            a.put(data);
        }
        
        try {
            channel.sendMessage(TextUtils.postToHastebin(a.toString(), true));
        } catch (UnirestException ex) {
            Logger.getLogger(PlayerDebugCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
