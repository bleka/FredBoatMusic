package fredboat.audio;

import fredboat.commandmeta.MessagingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.player.source.RemoteSource;
import net.dv8tion.jda.utils.PermissionUtil;

public class GuildPlayer extends MusicPlayer {

    public static final int MAX_PLAYLIST_ENTRIES = 20;

    public final JDA jda;
    public final String guildId;
    public final HashMap<String, VideoSelection> selections = new HashMap<>();
    public TextChannel currentTC;
    public long lastTimePaused = System.currentTimeMillis();
    public final PlayerEventListener eventListener;
    public String lastYoutubeVideoId = null;

    private long playlistTimeoutEnds = 0L;

    public GuildPlayer(JDA jda, Guild guild) {
        this.jda = jda;
        this.guildId = guild.getId();
        this.eventListener = new PlayerEventListener(this);
        addEventListener(eventListener);

        AudioManager manager = guild.getAudioManager();
        manager.setSendingHandler(this);
    }

    public void joinChannel(User usr) throws MessagingException {
        VoiceChannel targetChannel = getUserCurrentVoiceChannel(usr);
        joinChannel(targetChannel);
    }

    public void joinChannel(VoiceChannel targetChannel) throws MessagingException {
        if (targetChannel == null) {
            throw new MessagingException("You must join a voice channel first.");
        }

        /*if (guild.getVoiceStatusOfUser(self).inVoiceChannel()) {
            throw new MessagingException("I need to leave my current channel first.");
        }*/
        if (PermissionUtil.checkPermission(jda.getSelfInfo(), Permission.VOICE_CONNECT, targetChannel) == false) {
            throw new MessagingException("I am not permitted to connect to that voice channel.");
        }

        if (PermissionUtil.checkPermission(jda.getSelfInfo(), Permission.VOICE_SPEAK, targetChannel) == false) {
            throw new MessagingException("I am not permitted to play music in that voice channel.");
        }

        AudioManager manager = getGuild().getAudioManager();
        if (manager.getConnectedChannel() != null) {
            manager.moveAudioConnection(targetChannel);
        } else {
            manager.openAudioConnection(targetChannel);
        }

        /*while (manager.isAttemptingToConnect() == true && manager.isConnected() == false) {
             System.out.println(manager.isAttemptingToConnect() + " : " + manager.isConnected());
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                }
            }
        }*/
        System.out.println("Connected to voice channel " + targetChannel);
    }

    public void leaveVoiceChannelRequest(TextChannel channel, boolean silent) {
        AudioManager manager = getGuild().getAudioManager();
        if (!silent) {
            if (manager.getConnectedChannel() == null) {
                channel.sendMessage("Not currently in a channel.");
            } else {
                channel.sendMessage("Left channel " + getChannel().getName() + ".");
            }
        }
        manager.closeAudioConnection();
    }

    public VoiceChannel getUserCurrentVoiceChannel(User usr) {
        for (VoiceChannel chn : getGuild().getVoiceChannels()) {
            for (User userInChannel : chn.getUsers()) {
                if (usr.getId().equals(userInChannel.getId())) {
                    return chn;
                }
            }
        }
        return null;
    }

    public void playOrQueueSong(String url, TextChannel channel) {
        playOrQueueSong(url, channel, null);
    }

    public void playOrQueueSong(String url, TextChannel channel, User invoker) {
        //Check that we are in the same voice channel
        if (invoker != null && getUserCurrentVoiceChannel(invoker) != getChannel()) {
            joinChannel(invoker);
        }

        //Now we will either have thrown an exception or be in the same channel
        AudioManager manager = getGuild().getAudioManager();
        manager.setSendingHandler(this);

        Playlist playlist;
        try {
            playlist = Playlist.getPlaylist(url);
        } catch (NullPointerException ex) {
            RemoteSource rs = new RemoteSource(url);

            AudioInfo rsinfo = rs.getInfo();
            if (rsinfo.getError() != null) {
                channel.sendMessage("Was unable to queue song:" + rsinfo.getError());
            } else {
                throw new RuntimeException("Caught exception but unable to determine yt-dl error", ex);
            }
            return;
        }
        if (playlist.getSources().isEmpty()) {
            if (this.getAudioQueue().isEmpty()) {
                manager.closeAudioConnection();
                throw new MessagingException("The playlist is currently empty.");
            }
        } else if (playlist.getSources().size() == 1) {
            AudioSource source = playlist.getSources().get(0);
            AudioInfo info = source.getInfo();
            if (info.getError() != null) {
                manager.closeAudioConnection();
                throw new MessagingException("Could not load URL: " + info.getError());
            }
            if (info.isLive()) {
                throw new MessagingException("The provided source is currently live, but I cannot handle live sources.");
            }
            this.getAudioQueue().add(source);
            if (this.isPlaying()) {
                channel.sendMessage("**" + source.getInfo().getTitle() + "** has been added to the queue.");
            } else {
                channel.sendMessage("**" + source.getInfo().getTitle() + "** will now play.");
                this.play();
            }
        } else {
            //We have multiple sources in the playlist
            channel.sendMessage("Found a playlist with " + playlist.getSources().size() + " entries");
            int successfullyAdded = 0;
            int i = 0;

            //Check if the player is under cooldown
            if (playlistTimeoutEnds > System.currentTimeMillis()) {
                int secsToWait = (int) ((playlistTimeoutEnds - System.currentTimeMillis()) / 1000);
                throw new MessagingException("You are adding playlists too fast! Please wait " + secsToWait + " seconds before adding a new playlist. Longer playlists results in longer cooldowns.");
            }

            if (playlist.getSources().size() > MAX_PLAYLIST_ENTRIES) {
                channel.sendMessage("This playlist contains too many entries. Adding the first " + MAX_PLAYLIST_ENTRIES + " instead...");
            }

            for (AudioSource source : playlist.getSources()) {
                i++;
                AudioInfo info = source.getInfo();
                if (info.getError() != null) {
                    channel.sendMessage("Failed to queue #" + i + ": " + info.getError());
                } else if (info.isLive()) {
                    throw new MessagingException("The provided source is currently live, but I cannot handle live sources.");
                } else {
                    successfullyAdded++;
                    this.getAudioQueue().add(source);
                }

                //Begin to play if we are not already and we have at least one source
                if (this.isPlaying() == false && this.getAudioQueue().isEmpty() == false) {
                    this.play();
                }

                if (successfullyAdded == MAX_PLAYLIST_ENTRIES) {
                    break;
                }
            }

            playlistTimeoutEnds = System.currentTimeMillis() + 20000 * successfullyAdded;

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

            if (!this.isPlaying()) {
                this.play();
            }
        }
    }

    public VoiceChannel getChannel() {
        return getUserCurrentVoiceChannel(jda.getSelfInfo());
    }

    public TextChannel getActiveTextChannel() {
        if (currentTC != null) {
            return currentTC;
        } else {
            System.err.println("No currentTC in " + getGuild() + "! Returning public channel...");
            return getGuild().getPublicChannel();
        }

    }

    /**
     * Returns users who are not bots
     */
    public ArrayList<User> getUsersInVC() {
        VoiceChannel vc = getChannel();
        if (vc == null) {
            return new ArrayList<>();
        }

        List<User> allUsers = vc.getUsers();
        ArrayList<User> nonBots = new ArrayList<>();
        for (User usr : allUsers) {
            if (!usr.isBot()) {
                nonBots.add(usr);
            }
        }
        return nonBots;
    }

    public long getMillisSincePause() {
        return System.currentTimeMillis() - lastTimePaused;
    }

    @Override
    public String toString() {
        return "[GP:" + getGuild().getId() + "]";
    }

    public Guild getGuild() {
        return jda.getGuildById(guildId);
    }

}
