package fredboat.command.music;

import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import fredboat.commandmeta.MessagingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.source.RemoteSource;
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
        
        RemoteSource source = new RemoteSource(args[1]);
        if(source.getInfo().getError() != null){
            throw new MessagingException("Could not load URL: " + source.getInfo().getError());
        }
        player.getAudioQueue().add(source);
        player.play();
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
        while (manager.isAttemptingToConnect()) {
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

}
