package fredboat.command.music;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.player.source.AudioSource;

public class SkipCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        GuildPlayer player = PlayerRegistry.get(guild.getId());
        player.currentTC = channel;
        if (player.getCurrentAudioSource() == null) {
            channel.sendMessage("The queue is empty!");
        } else {
            AudioSource src = player.getCurrentAudioSource();
            player.skipToNext();
            channel.sendMessage("Skipped " + src.getInfo().getTitle());
        }

    }

}
