package fredboat.command.music;

import fredboat.commandmeta.Command;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.managers.AudioManager;

public class LeaveCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        AudioManager manager = guild.getAudioManager();
        if(manager.getConnectedChannel() == null){
            channel.sendMessage("Not currently in a channel");
        }
        manager.closeAudioConnection();
    }
    
}
