package fredboat.audio;

import java.util.HashMap;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;

public class PlayerRegistry {
    
    private static HashMap<String, GuildPlayer> registry = new HashMap<>();
    public static final float DEFAULT_VOLUME = 0.35f;
    public static JDA jda;
    
    public static void init(JDA api){
        jda = api;
    }
    
    public static void put(String k, GuildPlayer v){
        registry.put(k, v);
    }
    
    public static GuildPlayer get(Guild guild){
        return get(guild.getId());
    }
    
    public static GuildPlayer get(String k){
        GuildPlayer player = registry.get(k);
        if (player == null){
            player = new GuildPlayer(jda, jda.getGuildById(k));
            player.setVolume(DEFAULT_VOLUME);
            registry.put(k, player);
        }
        return player;
    }
    
    public static GuildPlayer remove(String k){
        return registry.remove(k);
    }

    public static HashMap<String, GuildPlayer> getRegistry() {
        return registry;
    }
    
}
