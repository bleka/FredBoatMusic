package fredboat.command.maintenance;

import fredboat.FredBoat;
import fredboat.commandmeta.Command;
import fredboat.commandmeta.ICommandOwnerRestricted;
import fredboat.util.ExitCodes;
import fredboat.util.TextUtils;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class RestartCommand extends Command implements ICommandOwnerRestricted {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        channel.sendMessage(TextUtils.prefaceWithMention(invoker, " Restarting.."));
        
        FredBoat.shutdown(ExitCodes.EXIT_CODE_RESTART);
    }

}
