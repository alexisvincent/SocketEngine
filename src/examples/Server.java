package examples;

import common.PacketHandler;
import server.NetServer;

/**
 *
 * @author alexisvincent
 */
public class Server {
    
    public static void main(String[] args) {
        NetServer server = new NetServer();
        PacketHandler packetHandler = new PacketHandler(server);
        RequestHandler requestHandler = new RequestHandler(packetHandler);
        server.startSocketListener(12345);
    }
}
