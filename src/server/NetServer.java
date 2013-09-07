package server;

import common.NetListener;
import common.Packet;
import common.Session;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alexisvincent
 */
public class NetServer {

    private ArrayList<Session> sessions;
    private ArrayList<SocketListener> listeners;
    
    private ArrayList<NetListener> sessionListeners;

    public NetServer() {
        initVars();
    }

    private void initVars() {
        sessions = new ArrayList<>();
        listeners = new ArrayList<>();
        sessionListeners = new ArrayList<>();
    }
    
    public void startSocketListener(int port) {
        try {
            
            listeners.add(new SocketListener(port));
        } catch (Exception ex) {
            Logger.getLogger(NetServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopSocketListener(int port) {
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i).getServerSocket().getLocalPort() == port) {
                listeners.get(i).dispose();
            }
        }
    }
    
    public void addSessionListener(NetListener listener) {
        this.sessionListeners.add(listener);
    }
    
    public void removeSessionListener(NetListener listener) {
        this.sessionListeners.remove(listener);
    }
    
    public void fireSessionStarted(Session session) {
        for (int i = 0; i < sessionListeners.size(); i++) {
            sessionListeners.get(i).sessionStarted(session);
        }
    }
    
    public void fireSessionEnded(Session session) {
        for (int i = 0; i < sessionListeners.size(); i++) {
            sessionListeners.get(i).sessionEnded(session);
        }
    }
    
    public void firePacketRecieved(Packet packet, Session session) {
        for (int i = 0; i < sessionListeners.size(); i++) {
            sessionListeners.get(i).packetRecieved(packet, session);
        }
    }

    public ArrayList<Session> getSessions() {
        return sessions;
    }

    public ArrayList<SocketListener> getListeners() {
        return listeners;
    }

    private void addSocketListener(SocketListener listener) {
        listeners.add(listener);
    }

    private void addSession(Session session) {
        sessions.add(session);
        fireSessionStarted(session);
    }

    private void removeSocketListener(SocketListener listener) {
        listeners.remove(listener);
    }

    private void removeSession(Session session) {
        sessions.remove(session);
        fireSessionEnded(session);
    }

    private void removeSocketListener(int port) {
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i).getServerSocket().getLocalPort() == port) {
                listeners.remove(listeners.get(i));
            }
        }
    }
    
    private class SocketListener implements Runnable {

        private ServerSocket serverSocket;
        private Thread listenerThread;
        private boolean listening;

        public SocketListener(int port) throws Exception {
            //MAKE BETTER EXCEPTIONS
            initVars(port);
            setListening(true);
        }

        private void initVars(int port) throws Exception {
            listenerThread = new Thread(this);
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(5);
        }

        public boolean isListening() {
            return listening;
        }

        public void setListening(boolean listening) {
            if (this.listening != listening) {
                this.listening = listening;

                if (listening) {
                    listenerThread.start();
                    System.out.println("Listening on port"+serverSocket.getLocalPort());
                }
            }
        }

        public void dispose() {
            setListening(false);
            NetServer.this.removeSocketListener(this);
        }

        @Override
        public void run() {
            while (listening) {
                try {
                    Session session = new Session(serverSocket.accept());
                    NetServer.this.addSession(session);
                } catch (IOException ex) {
                }
            }
        }

        public ServerSocket getServerSocket() {
            return this.serverSocket;
        }
    }
}
