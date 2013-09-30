package examples;

import socketEngine.ThreadedSocketEngine;

/**
 *
 * @author alexisvincent
 */
public class Server {
    
    public static void main(String[] args) {
        ThreadedSocketEngine server = new ThreadedSocketEngine();
        RequestHandler requestHandler = new RequestHandler(server);
        server.startSocketListener(12345);
    }
}
