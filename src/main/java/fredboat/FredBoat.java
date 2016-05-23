package fredboat;

import fredboat.event.EventListenerBoat;
import frederikam.jca.JCA;
import frederikam.jca.JCABuilder;
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
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

public class FredBoat {

    public static final boolean IS_BETA = true;
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
        FredBoat instance = new FredBoat();
        InputStream is = new FileInputStream(new File("./credentials.json"));
        //InputStream is = instance.getClass().getClassLoader().getResourceAsStream("credentials.json");
        Scanner scanner = new Scanner(is);
        JSONObject credsjson = new JSONObject(scanner.useDelimiter("\\A").next());

        //accountEmail = credsjson.getString(ACCOUNT_EMAIL_KEY);
        //accountPassword = credsjson.getString(ACCOUNT_PASSWORD_KEY);
        accountToken = credsjson.getString(ACCOUNT_TOKEN_KEY);
        String cbUser = credsjson.getString("cbUser");
        String cbKey = credsjson.getString("cbKey");
        String redisPassword = credsjson.getString("redisPassword");

        if (credsjson.has("scopePasswords")) {
            JSONObject scopePasswords = credsjson.getJSONObject("scopePasswords");
            for (String k : scopePasswords.keySet()) {
                scopePasswords.put(k, scopePasswords.getString(k));
            }
        }

        scanner.close();

        //Initialise event listeners
        listenerBot = new EventListenerBoat(0x01, PREFIX);

        fredboat.util.HttpUtils.init();
        jdaBot = new JDABuilder().addListener(listenerBot).setBotToken(accountToken).buildAsync();
        System.out.println("JDA version:\t" + JDAInfo.VERSION);

        //Initialise JCA
        jca = new JCABuilder().setKey(cbKey).setUser(cbUser).buildBlocking();
        
        jedis = new Jedis("frednet3.revgamesrblx.com", 6379);
        jedis.auth(redisPassword);
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
        
    }
    
    public static void shutdown(int code){
        jdaBot.shutdown(true);
        jedis.shutdown();
        System.exit(code);
    }
}
