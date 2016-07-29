package fredboat.audio;

import fredboat.commandmeta.MessagingException;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.utils.PermissionUtil;

public class GuildPlayer extends MusicPlayer {
    
    public final SelfInfo self;
    public final JDA jda;
    public final Guild guild;

    public GuildPlayer(JDA jda, Guild guild) {
        this.jda = jda;
        this.guild = guild;
        this.self = jda.getSelfInfo();
        
        AudioManager manager = guild.getAudioManager();
        manager.setSendingHandler(this);
    }
    
    public void joinChannel(User usr) throws MessagingException {
        VoiceChannel targetChannel = getUserCurrentVoiceChannel(usr);

        if (targetChannel == null) {
            throw new MessagingException("You must join a voice channel first.");
        }

        /*if (guild.getVoiceStatusOfUser(self).inVoiceChannel()) {
            throw new MessagingException("I need to leave my current channel first.");
        }*/
        if (PermissionUtil.checkPermission(self, Permission.VOICE_CONNECT, targetChannel) == false) {
            throw new MessagingException("I am not permitted to connect to that voice channel.");
        }

        if (PermissionUtil.checkPermission(self, Permission.VOICE_SPEAK, targetChannel) == false) {
            throw new MessagingException("I am not permitted to play music in that voice channel.");
        }

        AudioManager manager = guild.getAudioManager();
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
    
    public void leaveVoiceChannelRequest(TextChannel channel){
        AudioManager manager = guild.getAudioManager();
        if(manager.getConnectedChannel() == null){
            channel.sendMessage("Not currently in a channel");
        } else {
            channel.sendMessage("Left channel" + getChannel().getName());
        }
        manager.closeAudioConnection();
    }
    
    public VoiceChannel getUserCurrentVoiceChannel(User usr){
        for(VoiceChannel chn : guild.getVoiceChannels()){
            if(chn.getUsers().contains(usr)){
                return chn;
            }
        }
        return null;
    }
    
    public VoiceChannel getChannel(){
        return getUserCurrentVoiceChannel(jda.getSelfInfo());
    }
    
}
