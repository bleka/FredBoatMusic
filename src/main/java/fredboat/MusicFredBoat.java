package fredboat;

import fredboat.agent.CarbonAgent;
import fredboat.agent.MusicGC;
import fredboat.audio.MusicPersistenceHandler;
import fredboat.audio.PlayerRegistry;
import fredboat.audio.queue.MusicQueueProcessor;
import fredboat.command.music.*;
import fredboat.command.maintenance.*;
import fredboat.command.util.HelpCommand;
import fredboat.commandmeta.CommandRegistry;
import fredboat.commons.util.CommonConstants;
import fredboat.event.EventListenerBoat;
import fredboat.event.EventLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.JDAImpl;
import org.json.JSONObject;

public class MusicFredBoat {

    public static final String OTHER_BOT_ID = CommonConstants.MAIN_BOT_ID;
    
    public static volatile JDA jdaBot;
    public static final long START_TIME = System.currentTimeMillis();
    //public static final String ACCOUNT_EMAIL_KEY = IS_BETA ? "emailBeta" : "emailProduction";
    //public static final String ACCOUNT_PASSWORD_KEY = IS_BETA ? "passwordBeta" : "passwordProduction";
    public static final String ACCOUNT_TOKEN_KEY = CommonConstants.IS_BETA ? "tokenBeta" : "token";
    private static String accountToken;
    public static String CLIENT_ID = "184405253028970496";
    public static String googleServerKey = null;

    public static String myUserId = "";
    public static volatile User myUser;
    
    public static String helpMsg = "";

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
        String carbonHost = credsjson.optString("carbonHost");

        scanner.close();
        
        InputStream helpIS = instance.getClass().getClassLoader().getResourceAsStream("help.txt");
        BufferedReader in = new BufferedReader(new InputStreamReader(helpIS));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            helpMsg = helpMsg + inputLine + "\n";
        }
        in.close();

        //Initialise event listeners
        listenerBot = new EventListenerBoat(0x01, CommonConstants.DEFAULT_BOT_PREFIX);

        jdaBot = new JDABuilder()
                .addListener(listenerBot)
                .addListener(new EventLogger("216689009110417408"))
                .setBotToken(accountToken)
                .buildAsync();
        System.out.println("JDA version:\t" + JDAInfo.VERSION);

        PlayerRegistry.init(jdaBot);
        
        //Start statistics agent
        if (!CommonConstants.IS_BETA) {
            if (!carbonHost.equals("")) {
                CarbonAgent carbonAgent = new CarbonAgent(jdaBot, carbonHost, "music", !CommonConstants.IS_BETA);
                carbonAgent.setDaemon(true);
                carbonAgent.start();
                System.out.println("Started reporting to carbon-cache at " + carbonHost + ".");
            } else {
                System.out.println("No carbon host configured. Skipping carbon daemon.");
            }
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
        CommandRegistry.registerCommand(0x11, "mstats", new StatsCommand());
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
        CommandRegistry.registerCommand(0x11, "getid", new GetIdCommand());
        CommandRegistry.registerCommand(0x11, "shuffle", new ShuffleCommand());
        CommandRegistry.registerCommand(0x11, "repeat", new RepeatCommand());
        CommandRegistry.registerCommand(0x11, "volume", new VolumeCommand());
        CommandRegistry.registerCommand(0x11, "playerdebug", new PlayerDebugCommand());
        
        //Backup
        CommandRegistry.registerCommand(0x11, "help", new HelpCommand());
        
        //Threads
        MusicQueueProcessor qp = new MusicQueueProcessor();
        qp.setDaemon(true);
        qp.start();
        
        MusicGC mgc = new MusicGC(jdaBot);
        mgc.setDaemon(true);
        mgc.start();
        
        try {
            MusicPersistenceHandler.reloadPlaylists();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdown(int code) {
        MusicPersistenceHandler.handlePreShutdown(code);
        
        for (Object listener : ((JDAImpl) jdaBot).getEventManager().getRegisteredListeners()){
            if(listener instanceof EventLogger){
                ((EventLogger) listener).onExit(code);
            }
        }
        
        jdaBot.shutdown(true);
        //if (jedis != null) {
        //    jedis.shutdown();
        //}
        System.exit(code);
    }
}
