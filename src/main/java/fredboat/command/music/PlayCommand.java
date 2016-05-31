package fredboat.command.music;

import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import fredboat.commandmeta.MessagingException;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.utils.PermissionUtil;

public class PlayCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        SelfInfo self = guild.getJDA().getSelfInfo();

        //Check that we are in the same voice channel
        if (guild.getVoiceStatusOfUser(invoker).getChannel() != guild.getVoiceStatusOfUser(self).getChannel()) {
            joinChannel(guild, self, invoker);
        }

        //Now we will either have thrown an exception or be in the same channel
        MusicPlayer player = PlayerRegistry.get(guild.getId());
        AudioManager manager = guild.getAudioManager();
        manager.setSendingHandler(player);

        Playlist playlist = Playlist.getPlaylist(args[1]);
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
            if(player.isPlaying()){
                channel.sendMessage("**"+source.getInfo().getTitle()+"** has been added to the queue.");
            } else {
                channel.sendMessage("**"+source.getInfo().getTitle()+"** will now play.");
                player.play();
            }
        } else {
            //We have multiple sources in the playlist
            System.out.println("Found a playlist with "+playlist.getSources().size() + "entries");
            int successfullyAdded = 0;
            int i = 0;
            for (AudioSource source : playlist.getSources()) {
                i++;
                if (source.getInfo().getError() == null){
                    successfullyAdded++;
                    player.getAudioQueue().add(source);
                } else {
                    channel.sendMessage("Failed to queue #"+i+": "+source.getInfo().getError());
                }
                
                //Begin to play if we are not already and if we have at least one source
                if(player.isPlaying() == false && player.getAudioQueue().isEmpty() == false){
                    player.play();
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
                    channel.sendMessage("**"+successfullyAdded+" songs** have been successfully added.");
                    break;
            }
            
            if(!player.isPlaying()){
                player.play();
            }
        }
    }

    public void joinChannel(Guild guild, SelfInfo self, User usr) {
        VoiceChannel targetChannel = guild.getVoiceStatusOfUser(usr).getChannel();

        if (!guild.getVoiceStatusOfUser(usr).inVoiceChannel()) {
            throw new MessagingException("You must join a voice channel first.");
        }

        if (guild.getVoiceStatusOfUser(self).inVoiceChannel()) {
            throw new MessagingException("I need to leave my current channel first.");
        }

        if (PermissionUtil.checkPermission(self, Permission.VOICE_CONNECT, targetChannel) == false) {
            throw new MessagingException("I am not permitted to connect to that voice channel.");
        }

        if (PermissionUtil.checkPermission(self, Permission.VOICE_SPEAK, targetChannel) == false) {
            throw new MessagingException("I am not permitted to play music in that voice channel.");
        }

        AudioManager manager = guild.getAudioManager();
        manager.openAudioConnection(targetChannel);
        /*while (manager.isAttemptingToConnect() == true && manager.isConnected() == false) {
             System.out.println(manager.isAttemptingToConnect() + " : " + manager.isConnected());
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                }
            }
        }*/
        System.out.println("Connected to voice channel");
    }

}
