package fredboat.audio.queue;

import fredboat.audio.GuildPlayer;
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
        String lastPlaylistId = "";
        int successfullyAdded = 0;

        System.out.println("Started audio queue processor");
        
        while (true) {
            try {
                QueueItem item = queue.take();
                System.out.println("Took item from queue: " + item);
                TextChannel channel = item.getTextChannel();

                try {
                    AudioManager manager = item.getGuild().getAudioManager();
                    AudioSource source = item.getSource();
                    AudioInfo info = source.getInfo();
                    GuildPlayer player = item.getPlayer();
                    
                    if (!item.isPlaylistItem()) {
                        //Just a single item

                        if (info.getError() != null) {
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
                            lastPlaylistId = item.getPlaylistId();
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
                            switch (successfullyAdded) {
                                case 0:
                                    channel.sendMessage("Failed to queue any new songs.");
                                    break;
                                case 1:
                                    channel.sendMessage("A song has been added to the queue.");
                                    break;
                                default:
                                    channel.sendMessage("**" + successfullyAdded + " songs** have been successfully added.");
                                    break;
                            }
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

    public static void add(QueueItem item) {
        System.out.println("Added item to queue: " + item);
        queue.add(item);
    }

}
