package fredboat.db;

import fredboat.util.CommonConstants;
import java.util.Map;

public class RedisGuildCache {

    public final String guild;
    private Map<String, String> data = null;

    private long lastUpdateMs = 0;
    protected static final long CACHE_TIMEOUT = 5 * 60 * 1000;

    protected RedisGuildCache(String guild) {
        this.guild = guild;
    }

    public Map<String, String> getData() {
        ensureRecentData();
        return data;
    }
    
    public String getPrefix(){
        Map<String, String> d = getData();
        return d.getOrDefault("prefix", CommonConstants.DEFAULT_BOT_PREFIX);
    }

    private void ensureRecentData() {
        if (System.currentTimeMillis() - lastUpdateMs > CACHE_TIMEOUT) {
            query();
        }
    }

    private void query() {
        data = RedisCache.jedis.hgetAll("guild:" + guild);
    }

}
