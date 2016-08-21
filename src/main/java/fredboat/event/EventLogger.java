package fredboat.event;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

public class EventLogger extends ListenerAdapter {

    public final String logChannelId;
    public JDA jda;

    public EventLogger(String logChannelId) {
        this.logChannelId = logChannelId;
    }

    public TextChannel getChannel() {
        return jda.getTextChannelById(logChannelId);
    }

    @Override
    public void onReady(ReadyEvent event) {
        jda = event.getJDA();
        getChannel().sendMessage(new MessageBuilder()
                .appendString("[:rocket:] Received ready event.")
                .build()
        );
    }
    
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        getChannel().sendMessage(
                "[:white_check_mark:] Joined guild `" + event.getGuild() + "`. Users: `" + event.getGuild().getUsers().size() + "`."
        );
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        getChannel().sendMessage(
                "[:x:] Left guild `" + event.getGuild() + "`. Users: `" + event.getGuild().getUsers().size() + "`."
        );
    }
    
    public void onExit(int code){
        getChannel().sendMessage("[:zzz:] Exiting with code `" + code + "`.");
    }

}
