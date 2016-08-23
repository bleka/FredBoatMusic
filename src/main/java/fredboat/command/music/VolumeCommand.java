package fredboat.command.music;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import fredboat.commandmeta.MessagingException;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class VolumeCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        
        GuildPlayer player = PlayerRegistry.get(guild);
        try {
            float volume = Float.parseFloat(args[1])/100;
            volume = Math.max(0, Math.min(1, volume));
            
            channel.sendMessage("Changed volume from **" + (int) Math.floor(player.getVolume() * 100) + "%** to **" + (int) Math.floor(volume * 100) + "%**.");
            
            player.setVolume(volume);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new MessagingException("Use `;;volume <0-100>`. 35% is the default.\nThe player is currently at **" + (int) Math.floor(player.getVolume() * 100) + "%**.");
        }
    }
    
}
