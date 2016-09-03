package fredboat.command.maintenance;

import fredboat.MusicFredBoat;
import fredboat.commons.commandmeta.Command;
import fredboat.commons.commandmeta.ICommandOwnerRestricted;
import fredboat.commons.util.CommonConstants;
import fredboat.commons.util.ExitCodes;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import fredboat.commons.util.TextUtils;

/**
 *
 * @author frederik
 */
public class ExitCommand extends Command implements ICommandOwnerRestricted {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        if (invoker.getId().equals(CommonConstants.OWNER_ID)) {
            channel.sendMessage(TextUtils.prefaceWithMention(invoker, " goodbye!!"));
            MusicFredBoat.shutdown(ExitCodes.EXIT_CODE_NORMAL);
        } else {
            channel.sendMessage(TextUtils.prefaceWithMention(invoker, " you are not allowed to use that command!"));
        }
    }

}
