package fredboat.command.music;

import fredboat.commandmeta.Command;
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
        channel.sendMessage(rsinfo.getError());
        channel.sendMessage(rsinfo.getJsonInfo().toString());
    }
    
}
