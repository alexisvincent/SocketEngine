package examples;

import client.NetClient;
import common.NetExceptions;
import common.Packet;
import common.PacketFunctions;
import common.PacketHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/**
 *
 * @author alexisvincent
 */
public class Client {

    public static void main(String[] args) {
        NetClient client = new NetClient();
        final PacketHandler packetHandler = new PacketHandler(client);
        try {
            client.connect("localhost", 12345);
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        Timer ping = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                try {
                    long timeSent = Long.parseLong((String)PacketFunctions.getValue("timeSent", packetHandler.postRequest(PacketFunctions.generatePing())));
                    long responseTime = System.currentTimeMillis()-timeSent;
                    System.out.println("Ping: " + responseTime + "ms");
                } catch (NetExceptions.BadSessionException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NetExceptions.RequestTimedOutException ex) {
                    System.out.println("Request Timed out");
                }
            }
        });
        ping.start();
    }
}
