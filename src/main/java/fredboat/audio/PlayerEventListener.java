package fredboat.audio;

import net.dv8tion.jda.player.hooks.PlayerListenerAdapter;
import net.dv8tion.jda.player.hooks.events.PauseEvent;

public class PlayerEventListener extends PlayerListenerAdapter {

    public final GuildPlayer player;

    public PlayerEventListener(GuildPlayer player) {
        this.player = player;
    }
    
    @Override
    public void onPause(PauseEvent event) {
        player.lastTimePaused = System.currentTimeMillis();
    }
    
}
