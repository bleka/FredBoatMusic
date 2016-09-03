package fredboat.db;

import java.util.HashMap;
import net.dv8tion.jda.entities.Guild;
import redis.clients.jedis.Jedis;

public class RedisCache {

    public static Jedis jedis = null;
    public static HashMap<String, RedisGuildCache> guildCaches = new HashMap<>();

    public static void init(String host, String password) {
        jedis = new Jedis(host);
        jedis.auth(password);
        System.out.println("Connected to Redis at " + host);
    }

    public static RedisGuildCache getGuild(Guild guild) {
        return getGuild(guild.getId());
    }

    public static RedisGuildCache getGuild(String guildId) {
        if (guildCaches.containsKey(guildId)) {
            return guildCaches.get(guildId);
        } else {
            RedisGuildCache rgc = new RedisGuildCache(guildId);
            
            return guildCaches.put(guildId, rgc);
        }
    }

}
