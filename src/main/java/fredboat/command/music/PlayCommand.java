package fredboat.command.music;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class PlayCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        if (args.length < 2) {
            channel.sendMessage("Proper syntax: ;;play <url>");
            return;
        }

        SelfInfo self = guild.getJDA().getSelfInfo();
        GuildPlayer player = PlayerRegistry.get(guild.getId());

        player.playOrQueueSong(args[1], channel, invoker);
        
        try {
            message.deleteMessage();
        } catch (Exception ex) {

        }
    }

}
