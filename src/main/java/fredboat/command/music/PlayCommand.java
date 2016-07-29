package fredboat.command.music;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import fredboat.commandmeta.MessagingException;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.player.source.RemoteSource;

public class PlayCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        if (args.length < 2) {
            channel.sendMessage("Proper syntax: ;;play <url>");
            return;
        }

        SelfInfo self = guild.getJDA().getSelfInfo();
        GuildPlayer player = PlayerRegistry.get(guild.getId());

        //Check that we are in the same voice channel
        if (guild.getVoiceStatusOfUser(invoker).getChannel() != guild.getVoiceStatusOfUser(self).getChannel()) {
            player.joinChannel(invoker);
        }

        //Now we will either have thrown an exception or be in the same channel
        AudioManager manager = guild.getAudioManager();
        manager.setSendingHandler(player);

        Playlist playlist;
        try {
            playlist = Playlist.getPlaylist(args[1]);
        } catch (NullPointerException ex) {
            String url = args[1];
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
            if (player.getAudioQueue().isEmpty()) {
                manager.closeAudioConnection();
                throw new MessagingException("The playlist is currently empty.");
            }
        } else if (playlist.getSources().size() == 1) {
            AudioSource source = playlist.getSources().get(0);
            if (source.getInfo().getError() != null) {
                manager.closeAudioConnection();
                throw new MessagingException("Could not load URL: " + source.getInfo().getError());
            }
            player.getAudioQueue().add(source);
            if (player.isPlaying()) {
                channel.sendMessage("**" + source.getInfo().getTitle() + "** has been added to the queue.");
            } else {
                channel.sendMessage("**" + source.getInfo().getTitle() + "** will now play.");
                player.play();
            }
        } else {
            //We have multiple sources in the playlist
            channel.sendMessage("Found a playlist with " + playlist.getSources().size() + "entries");
            int successfullyAdded = 0;
            int i = 0;
            if (playlist.getSources().size() > 30) {
                channel.sendMessage("This playlist contains too many entries. Adding the first 30 instead...");
            }
            for (AudioSource source : playlist.getSources()) {
                i++;
                if (source.getInfo().getError() == null) {
                    successfullyAdded++;
                    player.getAudioQueue().add(source);
                } else {
                    channel.sendMessage("Failed to queue #" + i + ": " + source.getInfo().getError());
                }

                //Begin to play if we are not already and if we have at least one source
                if (player.isPlaying() == false && player.getAudioQueue().isEmpty() == false) {
                    player.play();
                }
                
                if(successfullyAdded == 30){
                    break;
                }
            }

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

            if (!player.isPlaying()) {
                player.play();
            }
        }

        try {
            message.deleteMessage();
        } catch (Exception ex) {

        }
    }

}
