package fredboat.agent;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import java.util.HashMap;
import net.dv8tion.jda.JDA;

public class MusicGC extends Thread {

    private final JDA jda;

    public MusicGC(JDA jda) {
        super();
        this.jda = jda;
    }

    @Override
    public void run() {
        while(true){
            synchronized(this){
                try {
                    this.sleep(60000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                
                HashMap<String, GuildPlayer> players = PlayerRegistry.getRegistry();
                
                for(GuildPlayer player : players.values()){
                    if(player.isPaused()
                            && player.isStopped() == false
                            && player.getMillisSincePause() > 60000){
                        player.stop();
                        System.out.println("Stopped player: " + player);
                    }
                }
            }
        }
    }
    
}
