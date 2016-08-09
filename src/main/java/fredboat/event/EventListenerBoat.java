/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fredboat.event;

import fredboat.commandmeta.Command;
import fredboat.commandmeta.CommandManager;
import fredboat.commandmeta.CommandRegistry;
import java.util.HashMap;
import java.util.regex.Pattern;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.InviteReceivedEvent;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.ReconnectedEvent;
import net.dv8tion.jda.events.message.MessageDeleteEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import fredboat.MusicFredBoat;
import static fredboat.MusicFredBoat.jdaBot;
import fredboat.audio.GuildPlayer;
import fredboat.audio.PlayerRegistry;
import java.util.regex.Matcher;
import net.dv8tion.jda.events.voice.VoiceLeaveEvent;

public class EventListenerBoat extends ListenerAdapter {

    public static HashMap<String, Message> messagesToDeleteIfIdDeleted = new HashMap<>();
    public static HashMap<VoiceChannel, Runnable> toRunOnConnectingToVoice = new HashMap<>();
    public User lastUserToReceiveHelp;
    public final int scope;
    public final String prefix;
    private final Pattern commandNamePrefix;

    public static int messagesReceived = 0;

    public EventListenerBoat(int scope, String prefix) {
        this.scope = scope;
        this.prefix = prefix;
        this.commandNamePrefix = Pattern.compile("(\\w+)");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        messagesReceived++;

        if (event.getPrivateChannel() != null) {
            System.out.println("PRIVATE" + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
            return;
        }

        if (event.getAuthor().getUsername().equals(event.getJDA().getSelfInfo().getUsername())) {
            System.out.println(event.getGuild().getName() + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
            return;
        }

        if (event.getMessage().getContent().length() < prefix.length()) {
            return;
        }

        if (event.getMessage().getContent().substring(0, prefix.length()).equals(prefix)) {
            String cmdName;
            Command invoked = null;
            try {
                System.out.println(event.getGuild().getName() + " \t " + event.getAuthor().getUsername() + " \t " + event.getMessage().getRawContent());
                Matcher matcher = commandNamePrefix.matcher(event.getMessage().getContent());
                matcher.find();

                invoked = CommandRegistry.getCommandFromScope(scope, matcher.group()).command;
            } catch (NullPointerException ex) {

            }

            if (invoked == null) {
                return;
            }

            CommandManager.prefixCalled(invoked, event.getGuild(), event.getTextChannel(), event.getAuthor(), event.getMessage());
        }
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (messagesToDeleteIfIdDeleted.containsKey(event.getMessageId())) {
            Message msg = messagesToDeleteIfIdDeleted.remove(event.getMessageId());
            if (msg.getJDA() == jdaBot) {
                msg.deleteMessage();
            }
        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        //Ignore self
        if (event.getAuthor().getUsername().equals(event.getJDA().getSelfInfo().getUsername())) {
            return;
        }

        //Ignore invites (handled elsewhere)
        if (event.getMessage().getContent().contains("discord.gg")) {
            return;
        }

        if (event.getAuthor() == lastUserToReceiveHelp) {
            //Ignore, just got help!
            return;
        }

        lastUserToReceiveHelp = event.getAuthor();
    }

    @Override
    public void onInviteReceived(InviteReceivedEvent event) {
        /*if (event.getMessage().isPrivate()) {
            event.getAuthor().getPrivateChannel().sendMessage("Sorry! Since the release of the official API, registered bots must now be invited by someone with Manage **Server permissions**. If you have permissions, you can invite me at:\n"
                    + "https://discordapp.com/oauth2/authorize?&client_id=" + MusicFredBoat.CLIENT_ID + "&scope=bot");
        }*/
    }

    @Override
    public void onReady(ReadyEvent event) {
        MusicFredBoat.init();
        jdaBot.getAccountManager().setGame("Say ;;help");
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        jdaBot.getAccountManager().setGame("music");
    }

    /* music related */
    @Override
    public void onVoiceLeave(VoiceLeaveEvent event) {
        GuildPlayer player = PlayerRegistry.get(event.getGuild());
        if (player.getUsersInVC().isEmpty() && player.isPaused() == false) {
                player.pause();
                player.getActiveTextChannel().sendMessage("All users have left the voice channel. The player has been paused.");
        }
    }

}
