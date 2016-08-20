package fredboat.command.util;

import fredboat.MusicFredBoat;
import fredboat.commandmeta.Command;
import fredboat.commandmeta.IBackupCommand;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class HelpCommand extends Command implements IBackupCommand {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        invoker.getPrivateChannel().sendMessage(MusicFredBoat.helpMsg);
        channel.sendMessage(invoker.getUsername() + ": Documentation has been sent to your direct messages!");
    }
    
}
