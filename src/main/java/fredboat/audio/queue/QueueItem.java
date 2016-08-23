package fredboat.audio.queue;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.player.source.AudioSource;

public class QueueItem {

    public final JDA jda;
    public final String invoker;
    public final String guild;
    public final String tc;
    public final AudioSource source;
    public String playlistId = null;
    public boolean lastPlaylistItem = false;
    public int playlistIndex = 0;

    public QueueItem(User invoker, TextChannel tc, AudioSource source) {
        this.invoker = invoker.getId();
        this.jda = invoker.getJDA();
        this.guild = tc.getGuild().getId();
        this.tc = tc.getId();
        this.source = source;
    }

    public QueueItem(User invoker, TextChannel tc, AudioSource source, String playlistId, int playlistIndex, boolean isLastPlaylistItem) {
        this.invoker = invoker.getId();
        this.jda = invoker.getJDA();
        this.guild = tc.getGuild().getId();
        this.tc = tc.getId();
        this.source = source;

        this.playlistId = playlistId;
        this.playlistIndex = playlistIndex;
        this.lastPlaylistItem = isLastPlaylistItem;
    }

    public JDA getJda() {
        return jda;
    }

    public User getInvoker() {
        return jda.getUserById(invoker);
    }

    public Guild getGuild() {
        return jda.getGuildById(guild);
    }

    public GuildPlayer getPlayer() {
        return PlayerRegistry.get(guild);
    }

    public TextChannel getTextChannel() {
        return jda.getTextChannelById(tc);
    }

    public AudioSource getSource() {
        return source;
    }

    public boolean isLastPlaylistItem() {
        return lastPlaylistItem;
    }

    public int getPlaylistIndex() {
        return playlistIndex;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public boolean isPlaylistItem() {
        return playlistId != null;
    }

    @Override
    public String toString() {
        return "[QI:"+ source.getSource() + "]";
    }

}
