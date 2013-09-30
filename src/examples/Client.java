package examples;

import socketEngine.NetExceptions;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import socketEngine.ThreadedSocketEngine;

/**
 *
 * @author alexisvincent
 */
public class Client {

    public static void main(String[] args) {
        final ThreadedSocketEngine client = new ThreadedSocketEngine();
        try {
            client.connectTo("localhost", 12345).addTag("client");
            
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        Timer ping = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    System.out.println("Ping: " + client.getFirstSession().ping() + "ms");
                } catch (NetExceptions.RequestTimedOutException ex) {
                    System.out.println("Request Timed out");
                }
            }
        });
        ping.start();

    }
}
