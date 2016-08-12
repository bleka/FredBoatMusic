package fredboat.command.music;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;


public class ShuffleCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        GuildPlayer player = PlayerRegistry.get(guild);
        player.setShuffle(!player.isShuffle());
        
        if(player.isShuffle()){
            channel.sendMessage("The player is now shuffled.");
        } else {
            channel.sendMessage("The player is no longer shuffled.");
        }
    }
    
}
