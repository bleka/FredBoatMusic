package fredboat.command.music;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.audio.VideoSelection;
import fredboat.commandmeta.Command;
import fredboat.util.YoutubeAPI;
import fredboat.util.YoutubeVideo;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class PlayCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        if (args.length < 2) {
            //channel.sendMessage("Proper syntax: ;;play <url-or-search-terms>");
            handleNoArguments(guild, channel, invoker, message);
            return;
        }

        //Search youtube for videos and let the user select a video
        if (!args[1].startsWith("http")) {
            searchForVideos(guild, channel, invoker, message, args);
            return;
        }

        SelfInfo self = guild.getJDA().getSelfInfo();
        GuildPlayer player = PlayerRegistry.get(guild.getId());
        player.currentTC = channel;

        player.playOrQueueSong(args[1], channel, invoker);

        try {
            message.deleteMessage();
        } catch (Exception ex) {

        }
    }
    
    private void handleNoArguments(Guild guild, TextChannel channel, User invoker, Message message){
        GuildPlayer player = PlayerRegistry.get(guild.getId());
        if(player.getCurrentAudioSource() == null && player.getAudioQueue().isEmpty()){
            channel.sendMessage("The player is not currently playing anything. Use the following syntax to add a song:\n;;play <url-or-search-terms>");
        } else if(player.isPlaying()){
            channel.sendMessage("The player is already playing.");
        } else if(player.getUsersInVC().isEmpty()){
            channel.sendMessage("There are no users in the voice chat.");
        } else {
            player.play();
            channel.sendMessage("The player will now play.");
        }
    }

    private void searchForVideos(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        Matcher m = Pattern.compile("\\S+\\s+(.*)").matcher(message.getStrippedContent());
        m.find();
        String query = m.group(1);

        Message outMsg = channel.sendMessage("Searching YouTube for `{q}`...".replace("{q}", query));

        ArrayList<YoutubeVideo> vids = YoutubeAPI.searchForVideos(query);

        if (vids.isEmpty()) {
            outMsg.updateMessage("No results for `{q}`".replace("{q}", query));
        } else {
            MessageBuilder builder = new MessageBuilder();
            builder.appendString("**Please select a video with the `;;select n` command:**");

            int i = 1;
            for (YoutubeVideo vid : vids) {
                builder.appendString("\n**")
                        .appendString(String.valueOf(i))
                        .appendString(":** ")
                        .appendString(vid.name)
                        .appendString(" (")
                        .appendString(vid.getDurationFormatted())
                        .appendString(")");
                i++;
            }

            outMsg.updateMessage(builder.build().getRawContent());
            
            GuildPlayer player = PlayerRegistry.get(guild.getId());
            player.currentTC = channel;
            player.selections.put(invoker.getId(), new VideoSelection(vids, outMsg));
        }
    }

}
