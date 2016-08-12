package fredboat.command.music;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class PauseCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        GuildPlayer player = PlayerRegistry.get(guild.getId());
        player.currentTC = channel;
        if(player.getCurrentAudioSource() == null && player.getAudioQueue().isEmpty()){
            channel.sendMessage("The player is not currently playing anything.");
        } else if(player.isPaused() || player.isStopped()){
            channel.sendMessage("The player is already paused.");
        } else {
            player.pause();
            channel.sendMessage("The player is now paused. You can unpause it with `;;unpause`.");
        }
    }
    
}
