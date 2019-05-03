import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import java.util.ArrayList;
import java.util.List;

public class Bot {
    final String SERVER_ADRESS = "Nope";
    final String QUERY_USER = "Nah";
    final String QUERY_PASSWORD = "No chance";
    final String BOT_NAME = "AFK Police";
    final int AFK_WARN_TIME = 1000 * 60 * 10;
    final int AFK_MOVE_TIME = 1000 * 60 * 15;
    final int AFK_CHANNEL_ID = 25; // Replace this with the ID of the channel you want the clients to get moved to
    final List<Integer> alreadyWarned = new ArrayList<Integer>();


    TS3Api api;
    TS3Config config;
    TS3Query query;

    public Bot() {
        config = new TS3Config();
        config.setHost(SERVER_ADRESS);

        query = new TS3Query(config);
        query.connect();

        api = query.getApi();
        api.login(QUERY_USER, QUERY_PASSWORD);
        api.selectVirtualServerById(1);
        api.setNickname(BOT_NAME);
        run();
        System.out.println(BOT_NAME + " is starting its work now.");
    }

    private void run() {
        Thread threaddyKrueger = new Thread() {
            public void run() {
                while (true) {
                    List<Client> clients = api.getClients();
                    for (Client client : clients) {
                        final int clientId = client.getId();
                        final long idleTime = client.getIdleTime();
                        final int currentChannel = client.getChannelId();
                        final String clientName = client.getNickname();

                        if (!clientName.equals(BOT_NAME) && currentChannel != AFK_CHANNEL_ID && idleTime > AFK_WARN_TIME) {
                            if (idleTime > AFK_WARN_TIME && idleTime < AFK_MOVE_TIME) {
                                if (!alreadyWarned.contains(clientId)) {
                                    api.sendPrivateMessage(clientId, "If you stay AFK, you will get moved to the AFK channel in 5 minutes from now.");
                                    alreadyWarned.add(clientId);
                                }
                            } else if (idleTime > AFK_MOVE_TIME) {
                                api.moveClient(clientId, AFK_CHANNEL_ID);
                                api.pokeClient(clientId, "Because you were AFK for longer than 15 minutes, you got moved to the AFK channel.");
                                alreadyWarned.remove(alreadyWarned.indexOf(clientId));
                            }
                        }
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        threaddyKrueger.start();
    }

    public static void main(String[] args) {
        new Bot();
    }
}
