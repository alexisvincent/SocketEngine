package client;

import common.NetListener;
import common.Packet;
import common.Session;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author alexisvincent
 */
public class NetClient {

    private ArrayList<Session> sessions;
    private ArrayList<NetListener> sessionListeners;

    public NetClient() {
        initVars();
    }

    public Session connect(String address, int port) throws Exception {
        //FIX EXCEPTIONS
        Session session = new Session(new Socket(address, port));
        addSession(session);
        return session;
    }

    private void initVars() {
        sessions = new ArrayList<>();
        sessionListeners = new ArrayList<>();
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

    private void addSession(Session session) {
        sessions.add(session);
        fireSessionStarted(session);
    }

    private void removeSession(Session session) {
        sessions.remove(session);
        fireSessionEnded(session);
    }
}
