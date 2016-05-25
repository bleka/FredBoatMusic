package fredboat.command.maintenance;

import fredboat.FredBoat;
import fredboat.commandmeta.Command;
import fredboat.commandmeta.ICommandOwnerRestricted;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import fredboat.util.TextUtils;

/**
 *
 * @author frederik
 */
public class ExitCommand extends Command implements ICommandOwnerRestricted {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        if (invoker.getId().equals(FredBoat.OWNER_ID)) {
            channel.sendMessage(TextUtils.prefaceWithMention(invoker, " goodbye!!"));
            System.exit(0);
        } else {
            channel.sendMessage(TextUtils.prefaceWithMention(invoker, " you are not allowed to use that command!"));
        }
    }

}
