package fredboat.command.music;

import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.player.MusicPlayer;

public class NowplayingCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        MusicPlayer player = PlayerRegistry.get(guild.getId());
        if(player.isPlaying()){
            channel.sendMessage("Now playing " + player.getCurrentAudioSource().getInfo().getTitle());
        } else {
            channel.sendMessage("Not currently playing anything");
        }
    }
    
}
