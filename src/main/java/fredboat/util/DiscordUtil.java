package fredboat.util;

import fredboat.MusicFredBoat;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;

public class DiscordUtil {

    public static boolean isOtherBotPresent(Guild guild) {
        JDA jda = guild.getJDA();
        User other = jda.getUserById(MusicFredBoat.OTHER_BOT_ID);
        return guild.isMember(other);
    }

}
