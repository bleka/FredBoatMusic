package fredboat.audio;

import java.util.HashMap;
import net.dv8tion.jda.player.MusicPlayer;

public class PlayerRegistry {
    
    private static HashMap<String, MusicPlayer> registry = new HashMap<>();
    public static final float DEFAULT_VOLUME = 0.35f;
    
    public static void put(String k, MusicPlayer v){
        registry.put(k, v);
    }
    
    public static MusicPlayer get(String k){
        MusicPlayer player = registry.get(k);
        if (player == null){
            player = new MusicPlayer();
            player.setVolume(DEFAULT_VOLUME);
            registry.put(k, player);
        }
        return player;
    }
    
    public static MusicPlayer remove(String k){
        return registry.remove(k);
    }
    
}
