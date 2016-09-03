package fredboat.command.music;

import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import fredboat.audio.VideoSelection;
import fredboat.commandmeta.Command;
import fredboat.util.YoutubeVideo;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.exceptions.PermissionException;

public class SelectCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        GuildPlayer player = PlayerRegistry.get(guild.getId());
        player.currentTC = channel;
        if (player.selections.containsKey(invoker.getId())) {
            VideoSelection selection = player.selections.get(invoker.getId());
            try {
                int i = Integer.valueOf(args[1]);
                if (selection.getChoices().size() < i || i < 1) {
                    throw new NumberFormatException();
                } else {
                    YoutubeVideo selected = selection.choices.get(i - 1);
                    player.selections.remove(invoker.getId());
                    String msg = "Song **#" + i + "** has been selected: **" + selected.getName() + "** (" + selected.getDurationFormatted() + ")";
                    selection.getOutMsg().updateMessage(msg);
                    player.playOrQueueSong("https://www.youtube.com/watch?v="+selected.id, channel, invoker);
                    try{
                        message.deleteMessage();
                    } catch(PermissionException ex){
                        
                    }
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException numberFormatException) {
                channel.sendMessage("Must be a number 1-" + selection.getChoices().size() + ".");
            }
        } else {
            channel.sendMessage("You must first be given a selection to choose from.");
        }
    }

}
