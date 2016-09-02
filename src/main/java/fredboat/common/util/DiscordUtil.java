package fredboat.common.util;

import fredboat.MusicFredBoat;
import java.util.List;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;

public class DiscordUtil {

    public static boolean isOtherBotPresent(Guild guild) {
        JDA jda = guild.getJDA();
        User other = jda.getUserById(MusicFredBoat.OTHER_BOT_ID);
        return guild.isMember(other);
    }
    
    public static boolean isUserBotCommander(Guild guild, User user){
        List<Role> roles = guild.getRolesForUser(user);
        
        for(Role r : roles){
            if(r.getName().equals("Bot Commander")){
                return true;
            }
        }
        
        return false;
    }

}
