package socketEngine;



import objects.ThreadedSession;
import objects.Packet;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alexisvincent
 */
public class ThreadedSocketEngine {

    private ArrayList<ThreadedSession> sessions;
    private ArrayList<SocketListener> socketListeners;
    private ArrayList<NetListener> sessionListeners;

    public ThreadedSocketEngine() {
        initVars();
        
        addSessionListener(new NetAdapter() {

            @Override
            public void sessionStarted(ThreadedSession session) {
                session.addNetListener(new NetAdapter() {

                    @Override
                    public void packetRecieved(Packet packet, ThreadedSession session) {
                        firePacketRecieved(packet, session);
                    }
                });
            }
            
        });
    }

    private void initVars() {
        sessions = new ArrayList<>();
        socketListeners = new ArrayList<>();
        sessionListeners = new ArrayList<>();
    }
    
    public void startSocketListener(int port) {
        try {
            
            socketListeners.add(new SocketListener(port));
        } catch (Exception ex) {
            Logger.getLogger(ThreadedSocketEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopSocketListener(int port) {
        for (int i = 0; i < socketListeners.size(); i++) {
            if (socketListeners.get(i).getServerSocket().getLocalPort() == port) {
                socketListeners.get(i).dispose();
            }
        }
    }
    
    public ThreadedSession connectTo(String address, int port) throws Exception {
        //FIX EXCEPTIONS
        ThreadedSession session = new ThreadedSession(new Socket(address, port));
        addSession(session);
        return session;
    }
    
    public void addSessionListener(NetListener listener) {
        this.sessionListeners.add(listener);
    }
    
    public void removeSessionListener(NetListener listener) {
        this.sessionListeners.remove(listener);
    }
    
    public void fireSessionStarted(ThreadedSession session) {
        for (int i = 0; i < sessionListeners.size(); i++) {
            sessionListeners.get(i).sessionStarted(session);
        }
    }
    
    public void fireSessionEnded(ThreadedSession session) {
        for (int i = 0; i < sessionListeners.size(); i++) {
            sessionListeners.get(i).sessionEnded(session);
        }
    }
    
    public void firePacketRecieved(Packet packet, ThreadedSession session) {
        for (int i = 0; i < sessionListeners.size(); i++) {
            sessionListeners.get(i).packetRecieved(packet, session);
        }
    }
    
    public ThreadedSession getFirstSession() {
        if (sessions.size()>0) {
            return sessions.get(0);
        }
        
        return null;
    }

    public ArrayList<ThreadedSession> getSessions() {
        return sessions;
    }
    
    public ArrayList<ThreadedSession> getSessions(String tag) {
        ArrayList<ThreadedSession> taggedSessions = new ArrayList<>();
        
        for (ThreadedSession session : getSessions()) {
            if (session.hasTag(tag)) {
                taggedSessions.add(session);
            }
        }
        
        if (taggedSessions.size() == 0) {
            taggedSessions = null;
        }
        
        return taggedSessions;
    }

    public ArrayList<SocketListener> getListeners() {
        return socketListeners;
    }

    private void addSocketListener(SocketListener listener) {
        socketListeners.add(listener);
    }

    private void addSession(ThreadedSession session) {
        sessions.add(session);
        fireSessionStarted(session);
    }

    private void removeSocketListener(SocketListener listener) {
        socketListeners.remove(listener);
    }

    private void removeSession(ThreadedSession session) {
        sessions.remove(session);
        fireSessionEnded(session);
    }

    private void removeSocketListener(int port) {
        for (int i = 0; i < socketListeners.size(); i++) {
            if (socketListeners.get(i).getServerSocket().getLocalPort() == port) {
                socketListeners.remove(socketListeners.get(i));
            }
        }
    }
    
    public class SocketListener implements Runnable {

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
                    System.out.println("Listening on port: "+serverSocket.getLocalPort());
                }
            }
        }

        public void dispose() {
            setListening(false);
            ThreadedSocketEngine.this.removeSocketListener(this);
        }

        @Override
        public void run() {
            while (listening) {
                try {
                    ThreadedSession session = new ThreadedSession(serverSocket.accept());
                    session.addTag("server");
                    ThreadedSocketEngine.this.addSession(session);
                } catch (IOException ex) {
                }
            }
        }

        public ServerSocket getServerSocket() {
            return this.serverSocket;
        }
    }
}
