package fredboat.command.maintenance;

import fredboat.MusicFredBoat;
import fredboat.commandmeta.Command;
import fredboat.commandmeta.ICommandOwnerRestricted;
import fredboat.commons.util.ExitCodes;
import fredboat.commons.util.TextUtils;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class RestartCommand extends Command implements ICommandOwnerRestricted {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        channel.sendMessage(TextUtils.prefaceWithMention(invoker, " Restarting.."));
        
        MusicFredBoat.shutdown(ExitCodes.EXIT_CODE_RESTART);
    }

}
