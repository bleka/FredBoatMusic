package fredboat.agent;

import fredboat.commandmeta.CommandManager;
import fredboat.event.EventListenerBoat;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.JDA;

public class CarbonAgent extends Thread {

    public final String carbonHost;
    public final int CARBON_PORT = 2003;
    public final boolean logProductionStats;
    private int commandsExecutedLastSubmission = 0;
    private int messagesReceivedLastSubmission = 0;
    public final JDA jda;
    public final String buildStream;
    private int timesSubmitted = 0;

    
    public CarbonAgent(JDA jda, String carbonHost, String buildStream, boolean logProductionStats) {
        this.jda = jda;
        this.carbonHost = carbonHost;
        this.buildStream = buildStream;
        this.logProductionStats = logProductionStats;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    this.wait(1000 * 60);//Only tick once per minute

                    timesSubmitted++;
                    if (timesSubmitted % 5 == 0) {
                        handleEvery5Minutes();
                    }

                    if (timesSubmitted % 60 == 0) {
                        handleHourly();
                    }

                    //Track command usage
                } catch (InterruptedException ex) {
                    Logger.getLogger(CarbonAgent.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            }

        }
    }

    private void handleEvery5Minutes() {
        submitData("carbon.fredboat.users." + buildStream, String.valueOf(jda.getUsers().size()));
        submitData("carbon.fredboat.guilds." + buildStream, String.valueOf(jda.getGuilds().size()));
        
        if (logProductionStats) {
            submitData("carbon.fredboat.memoryUsage." + buildStream, String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));//In bytes
        }
    }

    private void handleHourly() {
        submitData("carbon.fredboat.commandsExecuted." + buildStream, String.valueOf(CommandManager.commandsExecuted - commandsExecutedLastSubmission));
        commandsExecutedLastSubmission = CommandManager.commandsExecuted;
        submitData("carbon.fredboat.messagesReceived." + buildStream, String.valueOf(EventListenerBoat.messagesReceived - messagesReceivedLastSubmission));
        messagesReceivedLastSubmission = EventListenerBoat.messagesReceived;
    }

    public void submitData(String path, String value) {
        try {
            String output = path + " " + value + " " + System.currentTimeMillis() / 1000;
            Socket socket = new Socket(carbonHost, CARBON_PORT);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeBytes(output + "\n");
            dos.flush();
            socket.close();

            System.out.println("Submitted data: " + output);
        } catch (IOException ex) {
            Logger.getLogger(CarbonAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
