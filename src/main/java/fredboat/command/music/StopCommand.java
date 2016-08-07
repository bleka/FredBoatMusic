package fredboat.command.music;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.PermissionUtil;

public class StopCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        if(PermissionUtil.checkPermission(invoker, Permission.MESSAGE_MANAGE, guild)){
            GuildPlayer player = PlayerRegistry.get(guild.getId());
            int count = player.getAudioQueue().size();
            
            if(player.getCurrentAudioSource() != null){
                count++;
            }
            
            player.stop();
            switch (count) {
                case 0:
                    channel.sendMessage("The queue was already empty.");
                    break;
                case 1:
                    channel.sendMessage("The queue has been emptied, `1` song have been removed.");
                    break;
                default:
                    channel.sendMessage("The queue has been emptied, `"+ count +"` songs have been removed.");
                    break;
            }
            player.leaveVoiceChannelRequest(channel, true);
        } else {
            channel.sendMessage("In order to prevent abuse, this command is only available to those who can manage messages.");
        }
    }
    
}
