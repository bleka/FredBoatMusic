package fredboat.command.music;

import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.source.AudioSource;

public class ListCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        MusicPlayer player = PlayerRegistry.get(guild.getId());
        if(player.getCurrentAudioSource() != null){
            MessageBuilder mb = new MessageBuilder();
            String status = player.isPlaying() ? "[PLAYING] " : "[PAUSED] ";
            mb.appendString(status, MessageBuilder.Formatting.BOLD)
                    .appendString(player.getCurrentAudioSource().getInfo().getTitle())
                    .appendString("\n");
            
            //Now add the queue
            int i = 1;
            for(AudioSource src : player.getAudioQueue()){
                mb.appendString(src.getInfo().getTitle())
                        .appendString("\n");
                if(i == 10){
                    break;
                }
            }
            
            mb.appendString("\n\nThere are a total of ")
                    .appendString(String.valueOf(player.getAudioQueue().size()), MessageBuilder.Formatting.BOLD)
                    .appendString(" queued.");
            
            channel.sendMessage(mb.build());
        } else {
            channel.sendMessage("Not currently playing anything");
        }
    }
    
}
