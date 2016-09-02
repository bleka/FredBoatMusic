package fredboat.command.maintenance;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import fredboat.commandmeta.CommandManager;
import fredboat.MusicFredBoat;
import fredboat.audio.PlayerRegistry;
import fredboat.commandmeta.Command;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import fredboat.commons.util.TextUtils;
import net.dv8tion.jda.JDAInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class StatsCommand extends Command {

    @Override
    public void onInvoke(Guild guild, TextChannel channel, User invoker, Message message, String[] args) {
        long totalSecs = (System.currentTimeMillis() - MusicFredBoat.START_TIME) / 1000;
        int days = (int) (totalSecs / (60 * 60 * 24));
        int hours = (int) ((totalSecs / (60 * 60)) % 24);
        int mins = (int) ((totalSecs / 60) % 60);
        int secs = (int) (totalSecs % 60);

        String uptime30Days = "ERROR";
        String url = "https://api.uptimerobot.com/getMonitors?apiKey=m777550344-171dca574df5a9f8dc7f2484&responseTimesAverage=180&monitors=15830-32696&customUptimeRatio=30&format=json";
        try {
            String jsonstr = Unirest.get(url).asString().getBody().substring("jsonUptimeRobotApi(".length());
            jsonstr = jsonstr.substring(0, jsonstr.length() - 1);
            uptime30Days = new JSONObject(jsonstr).getJSONObject("monitors").getJSONArray("monitor").getJSONObject(0).getString("customuptimeratio") + "%";
            //uptime30Days = Unirest.get(url).asJson().getBody().getObject().getJSONObject("monitors").getJSONArray("monitor").getJSONObject(0).getString("customUptimeRatio") + "%";
        } catch (JSONException | UnirestException ex) {
            ex.printStackTrace();
        }
        
        String str = " This boat has been floating for "
                + days + " days, "
                + hours + " hours, "
                + mins + " minutes and "
                + secs + " seconds.\n"
                + "This bot has executed " + (CommandManager.commandsExecuted - 1) + " commands this session.\n";
        str = str + "That's a rate of " + (float) (CommandManager.commandsExecuted - 1) / ((float) totalSecs / (float) (60 * 60)) + " commands per hour\n\n```";
        str = str + "Host uptime last 30 days:  " + uptime30Days + "\n";
        str = str + "Reserved memory:           " + Runtime.getRuntime().totalMemory() / 1000000 + "MB\n";
        str = str + "-> Of which is used:       " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000 + "MB\n";
        str = str + "-> Of which is free:       " + Runtime.getRuntime().freeMemory() / 1000000 + "MB\n";
        str = str + "Max reservable:            " + Runtime.getRuntime().maxMemory() / 1000000 + "MB\n";

        str = str + "\n----------\n\n";

        str = str + "Guilds playing music in:   " + PlayerRegistry.getPlayingPlayers().size() + "\n";
        str = str + "Known servers:             " + guild.getJDA().getGuilds().size() + "\n";
        str = str + "Known text channels:       " + guild.getJDA().getTextChannels().size() + "\n";
        str = str + "Known users in servers:    " + guild.getJDA().getUsers().size() + "\n";
        str = str + "Is beta:                   " + MusicFredBoat.IS_BETA + "\n";
        str = str + "JDA responses total:       " + guild.getJDA().getResponseTotal() + "\n";
        str = str + "JDA version:               " + JDAInfo.VERSION;
        
        str = str + "```";

        channel.sendMessage(TextUtils.prefaceWithMention(invoker, str));
        //channel.sendMessage(str2);
        //hannel.sendMessage(str3);
    }

}
