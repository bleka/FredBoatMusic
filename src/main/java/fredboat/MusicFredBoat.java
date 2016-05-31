package fredboat;

import fredboat.command.maintenance.EvalCommand;
import fredboat.command.maintenance.ExitCommand;
import fredboat.command.maintenance.RestartCommand;
import fredboat.command.music.JoinCommand;
import fredboat.command.music.LeaveCommand;
import fredboat.command.music.ListCommand;
import fredboat.command.music.MusicInfoCommand;
import fredboat.command.music.NowplayingCommand;
import fredboat.command.music.PlayCommand;
import fredboat.command.music.SkipCommand;
import fredboat.commandmeta.CommandRegistry;
import fredboat.event.EventListenerBoat;
import frederikam.jca.JCA;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.entities.User;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

public class MusicFredBoat {

    public static final boolean IS_BETA = false;
    public static volatile JDA jdaBot;
    public static JCA jca;
    public static final String PREFIX = IS_BETA ? "¤" : ";;";
    public static final String OWNER_ID = "81011298891993088";
    public static Jedis jedis;
    public static final long START_TIME = System.currentTimeMillis();
    //public static final String ACCOUNT_EMAIL_KEY = IS_BETA ? "emailBeta" : "emailProduction";
    //public static final String ACCOUNT_PASSWORD_KEY = IS_BETA ? "passwordBeta" : "passwordProduction";
    public static final String ACCOUNT_TOKEN_KEY = "token";
    private static String accountToken;
    public static String CLIENT_ID = "184405253028970496";

    public static String myUserId = "";
    public static volatile User myUser;

    public static int readyEvents = 0;
    public static final int READY_EVENTS_REQUIRED = 1;

    public static EventListenerBoat listenerBot;

    public static void main(String[] args) throws LoginException, IllegalArgumentException, InterruptedException, IOException {
        //Load credentials file
        MusicFredBoat instance = new MusicFredBoat();
        InputStream is = new FileInputStream(new File("./credentials.json"));
        //InputStream is = instance.getClass().getClassLoader().getResourceAsStream("credentials.json");
        Scanner scanner = new Scanner(is);
        JSONObject credsjson = new JSONObject(scanner.useDelimiter("\\A").next());

        accountToken = credsjson.getString(ACCOUNT_TOKEN_KEY);

        scanner.close();

        //Initialise event listeners
        listenerBot = new EventListenerBoat(0x01, PREFIX);

        jdaBot = new JDABuilder().addListener(listenerBot).setBotToken(accountToken).buildAsync();
        System.out.println("JDA version:\t" + JDAInfo.VERSION);
    }

    public static void init() {
        readyEvents = readyEvents + 1;

        System.out.println("INIT: " + readyEvents);

        if (readyEvents < READY_EVENTS_REQUIRED) {
            return;
        }

        /*for (Guild guild : jdaBot.getGuilds()) {
            System.out.println(guild.getName());

            for (TextChannel channel : guild.getTextChannels()) {
                System.out.println("\t" + channel.getName());
            }
        }*/

        myUserId = jdaBot.getSelfInfo().getId();
        myUser = jdaBot.getUserById(myUserId);

        //Commands
        CommandRegistry.registerCommand(0x01, "mexit", new ExitCommand());
        CommandRegistry.registerCommand(0x11, "mrestart", new RestartCommand());
        CommandRegistry.registerCommand(0x11, "play", new PlayCommand());
        CommandRegistry.registerCommand(0x11, "minfo", new MusicInfoCommand());
        CommandRegistry.registerCommand(0x11, "meval", new EvalCommand());
        CommandRegistry.registerCommand(0x11, "skip", new SkipCommand());
        CommandRegistry.registerCommand(0x11, "join", new JoinCommand());
        CommandRegistry.registerCommand(0x11, "nowplaying", new NowplayingCommand());
        CommandRegistry.registerCommand(0x11, "leave", new LeaveCommand());
        CommandRegistry.registerCommand(0x11, "list", new ListCommand());
    }
    
    public static void shutdown(int code){
        jdaBot.shutdown(true);
        jedis.shutdown();
        System.exit(code);
    }
}
