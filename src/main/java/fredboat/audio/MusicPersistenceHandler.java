package fredboat.audio;

import fredboat.MusicFredBoat;
import fredboat.util.ExitCodes;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.player.source.RemoteSource;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class MusicPersistenceHandler {

    private static boolean isFirst = true;//Used for loading songs
    
    public static void handlePreShutdown(int code) {
        JDA jda = MusicFredBoat.jdaBot;

        File dir = new File("music_persistence");
        if (!dir.exists()) {
            dir.mkdir();
        }
        HashMap<String, GuildPlayer> reg = PlayerRegistry.getRegistry();

        boolean isUpdate = code == ExitCodes.EXIT_CODE_UPDATE;
        boolean isRestart = code == ExitCodes.EXIT_CODE_RESTART;

        String msg = null;

        if (isUpdate) {
            msg = "FredBoat♪♪ is updating. This should only take a minute and will reload the current playlist.";
        } else if (isRestart) {
            msg = "FredBoat♪♪ is restarting. This should only take a minute and will reload the current playlist.";
        } else {
            msg = "FredBoat♪♪ is shutting down. Once the bot comes back the current playlist will reload.";
        }

        for (String gId : reg.keySet()) {
            try {
                GuildPlayer player = reg.get(gId);
                Guild guild = jda.getGuildById(gId);
                
                if (player.getCurrentAudioSource() == null) {
                    continue;//Nothing to see here
                }
                
                player.getActiveTextChannel().sendMessage(msg);
                
                JSONObject data = new JSONObject();
                data.put("vc", player.getUserCurrentVoiceChannel(jda.getSelfInfo()).getId());
                data.put("tc", player.getActiveTextChannel().getId());
                data.put("isPaused", player.isPaused());
                
                ArrayList<String> srcs = new ArrayList<>();
                srcs.add(player.getCurrentAudioSource().getSource());
                
                for (AudioSource src : player.getAudioQueue()) {
                    srcs.add(src.getSource());
                }
                
                data.put("sources", srcs);
                
                try {
                    FileUtils.writeStringToFile(new File(dir, gId), data.toString());
                } catch (IOException ex) {
                    player.getActiveTextChannel().sendMessage("Error occured when saving persistence file: " + ex.getMessage());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void reloadPlaylists() {
        JDA jda = MusicFredBoat.jdaBot;

        File dir = new File("music_persistence");
        if (!dir.exists()) {
            return;
        }
        System.out.println("Found persistence data: "+Arrays.toString(dir.listFiles()));

        for (File file : dir.listFiles()) {
            InputStream is = null;
            try {
                String gId = file.getName();
                is = new FileInputStream(file);
                Scanner scanner = new Scanner(is);
                JSONObject data = new JSONObject(scanner.useDelimiter("\\A").next());
                scanner.close();

                GuildPlayer player = PlayerRegistry.get(gId);
                
                boolean isPaused = data.getBoolean("isPaused");
                JSONArray sources = data.getJSONArray("sources");
                VoiceChannel vc = jda.getVoiceChannelById(data.getString("vc"));
                TextChannel tc = jda.getTextChannelById(data.getString("tc"));
                
                player.joinChannel(vc);
                player.currentTC = tc;
                
                
                sources.forEach((Object t) -> {
                    String src = (String) t;
                    AudioSource aud = new RemoteSource(src);
                    
                    //System.out.println(player.getAudioQueue().toString()+ " : " + (player.isPlaying() == false));
                    player.getAudioQueue().add(aud);
                    
                    if(isFirst){
                        if(isPaused){
                            tc.sendMessage("Reloading playlist. The player is still paused.");
                            player.skipToNext();
                            player.pause();
                        } else {
                            tc.sendMessage("Reloading playlist. The first song will now play.");
                            player.play();
                        }
                    }
                    isFirst = false;
                });
                isFirst = true;
                tc.sendMessage("Finished reloading playlist.:ok_hand::skin-tone-3:");
            } catch (Exception ex) {
                Logger.getLogger(MusicPersistenceHandler.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    is.close();
                } catch (Exception ex) {
                    Logger.getLogger(MusicPersistenceHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        for(File f : dir.listFiles()){
            boolean deleted = f.delete();
            System.out.println(deleted ? "Deleted persistence file: " + f : "Failed to delete persistence file: " + f);
        }
        
        dir.delete();
    }

}
