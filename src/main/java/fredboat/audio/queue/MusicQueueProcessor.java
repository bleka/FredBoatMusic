package fredboat.audio.queue;

import fredboat.audio.GuildPlayer;
import static fredboat.audio.GuildPlayer.MAX_PLAYLIST_ENTRIES;
import fredboat.commandmeta.MessagingException;
import fredboat.util.TextUtils;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;

public class MusicQueueProcessor extends Thread {

    public static LinkedBlockingQueue<QueueItem> queue = new LinkedBlockingQueue<>();

    public MusicQueueProcessor() {
        setDaemon(true);
    }

    @Override
    public void run() {
        super.run(); //To change body of generated methods, choose Tools | Templates.

        String lastPlaylistId = "";
        int successfullyAdded = 0;

        while (true) {
            try {
                QueueItem item = queue.take();
                TextChannel channel = item.getTextChannel();

                try {
                    AudioManager manager = item.getGuild().getAudioManager();
                    AudioSource source = item.getSource();
                    AudioInfo info = source.getInfo();
                    GuildPlayer player = item.getPlayer();

                    if (!item.isPlaylistItem()) {
                        //Just a single item

                        if (info.getError() != null) {
                            manager.closeAudioConnection();
                            throw new MessagingException("Could not load URL: " + info.getError());
                        }
                        if (info.isLive()) {
                            throw new MessagingException("The provided source is currently live, but I cannot handle live sources.");
                        }
                        player.getAudioQueue().add(source);
                        if (player.isPlaying()) {
                            channel.sendMessage("**" + source.getInfo().getTitle() + "** has been added to the queue.");
                        } else {
                            channel.sendMessage("**" + source.getInfo().getTitle() + "** will now play.");
                            player.play();
                        }
                    } else {
                        if (!item.getPlaylistId().equals(lastPlaylistId)) {
                            lastPlaylistId = "";
                            successfullyAdded = 0;
                        }

                        if (info.getError() != null) {
                            channel.sendMessage("Failed to queue #" + item.getPlaylistIndex() + ": " + info.getError());
                        } else if (info.isLive()) {
                            throw new MessagingException("The provided source is currently live, but I cannot handle live sources.");
                        } else {
                            successfullyAdded++;
                            player.getAudioQueue().add(source);
                        }

                        //Begin to play if we are not already and we have at least one source
                        if (player.isPlaying() == false && player.getAudioQueue().isEmpty() == false) {
                            player.play();
                        }

                        if (item.isLastPlaylistItem()) {
                            channel.sendMessage("Successfully added **" + successfullyAdded + " tracks to the queue.");
                        }
                    }
                } catch (MessagingException ex) {
                    channel.sendMessage(ex.getMessage());
                } catch (Exception ex) {
                    TextUtils.handleException(ex, channel);
                }

            } catch (Exception ex) {
                Logger.getLogger(MusicQueueProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void add(QueueItem item) {
        queue.add(item);
    }

}
