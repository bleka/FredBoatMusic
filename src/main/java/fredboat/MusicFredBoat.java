package fredboat;

import fredboat.agent.CarbonAgent;
import fredboat.audio.MusicPersistenceHandler;
import fredboat.audio.PlayerRegistry;
import fredboat.command.music.*;
import fredboat.command.maintenance.*;
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

    public static final boolean IS_BETA = System.getProperty("os.name").toLowerCase().contains("windows");
    public static volatile JDA jdaBot;
    public static JCA jca;
    public static final String PREFIX = IS_BETA ? "¤" : ";;";
    public static final String OWNER_ID = "81011298891993088";
    public static Jedis jedis;
    public static final long START_TIME = System.currentTimeMillis();
    //public static final String ACCOUNT_EMAIL_KEY = IS_BETA ? "emailBeta" : "emailProduction";
    //public static final String ACCOUNT_PASSWORD_KEY = IS_BETA ? "passwordBeta" : "passwordProduction";
    public static final String ACCOUNT_TOKEN_KEY = IS_BETA ? "tokenBeta" : "token";
    private static String accountToken;
    public static String CLIENT_ID = "184405253028970496";
    public static String googleServerKey = null;

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
        googleServerKey = credsjson.getString("googleServerKey");

        scanner.close();

        //Initialise event listeners
        listenerBot = new EventListenerBoat(0x01, PREFIX);

        jdaBot = new JDABuilder().addListener(listenerBot).setBotToken(accountToken).buildAsync();
        System.out.println("JDA version:\t" + JDAInfo.VERSION);

        PlayerRegistry.init(jdaBot);
        
        //Start statistics agent
        if (!IS_BETA) {
            CarbonAgent carbon = new CarbonAgent(jdaBot, "music", true);
            carbon.start();
        }
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
        CommandRegistry.registerCommand(0x11, "mupdate", new UpdateCommand());
        CommandRegistry.registerCommand(0x11, "select", new SelectCommand());
        CommandRegistry.registerCommand(0x11, "stop", new StopCommand());
        CommandRegistry.registerCommand(0x11, "pause", new PauseCommand());
        CommandRegistry.registerCommand(0x11, "unpause", new UnpauseCommand());
        
        MusicPersistenceHandler.reloadPlaylists();
    }

    public static void shutdown(int code) {
        MusicPersistenceHandler.handlePreShutdown(code);
        jdaBot.shutdown(true);
        //if (jedis != null) {
        //    jedis.shutdown();
        //}
        System.exit(code);
    }
}
