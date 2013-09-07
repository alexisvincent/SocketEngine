package common;

import client.NetClient;
import java.util.ArrayList;
import server.NetServer;

/**
 *
 * @author alexisvincent
 */
public class PacketHandler {

    private ArrayList<Session> sessions;
    private ArrayList<Packet> responses;
    private ArrayList<NetListener> listeners;

    public PacketHandler(NetServer server) {
        initVars();
        setSessions(server.getSessions());
        server.addSessionListener(new NetAdapter() {
            @Override
            public void sessionStarted(Session session) {
                addSession(session);
            }
        });
    }

    public PacketHandler(NetClient client) {
        initVars();
        setSessions(client.getSessions());
        client.addSessionListener(new NetAdapter() {
            @Override
            public void sessionStarted(Session session) {
                addSession(session);
            }
        });
    }

    public void postPacket(Packet packet) throws NetExceptions.BadSessionException {
        postPacket(packet, getDefaultSession());
    }

    public void postPacket(Packet packet, Session session) throws NetExceptions.BadSessionException {
        try {
            session.postPacket(packet);
        } catch (Exception e) {
            throw new NetExceptions.BadSessionException();
        }
    }

    public Session getDefaultSession() {
        if (!sessions.isEmpty()) {
            return sessions.get(0);
        }
        return null;
    }

    public Packet postRequest(Packet packet) throws NetExceptions.BadSessionException, NetExceptions.RequestTimedOutException {
        //FIX EXCEPTIONS
        return postRequest(packet, getDefaultSession());

    }

    /**
     *
     * Call this method from within a separate Thread. Else it will hang your
     * app for the amount of time it takes to receive a response, or until the
     * timeout is reached.
     *
     * @param packet
     * @param session
     * @return Response Packet
     */
    public Packet postRequest(Packet packet, Session session) throws NetExceptions.BadSessionException, NetExceptions.RequestTimedOutException {

        int checkDelay = 2;

        try {
            //This also sets default values before sending with .fattenPacket()
            session.postPacket(packet);
        } catch (Exception e) {
            throw new NetExceptions.BadSessionException();
        }

        long timeSent = System.currentTimeMillis();

        while (System.currentTimeMillis() - timeSent < PacketFunctions.getTimeOut(packet)) {
            for (int i = 0; i < responses.size(); i++) {
                if (PacketFunctions.getID(responses.get(i)).equals(PacketFunctions.getID(packet))) {
                    Packet responce = responses.get(i);
                    responses.remove(responce);
                    return responce;
                }
            }

            try {
                Thread.sleep(checkDelay);
            } catch (InterruptedException ex) {
            }
        }

        throw new NetExceptions.RequestTimedOutException();
    }

    private void initVars() {
        sessions = new ArrayList<>();
        responses = new ArrayList<>();
        listeners = new ArrayList<>();

    }

    public void addRequestListener(NetListener listener) {
        listeners.add(listener);
    }

    public void removeRequestListener(NetListener listener) {
        listeners.remove(listener);
    }

    public void fireRequestRecieved(Packet packet, Session session) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).requestRecieved(packet, session);
        }
    }

    public void addSession(Session session) {

        session.addNetListener(new NetAdapter() {
            @Override
            public void packetRecieved(Packet packet, Session session) {
                if (PacketFunctions.isRequest(packet)) {
                    fireRequestRecieved(packet, session);
                } else if (PacketFunctions.isResponce(packet)) {
                    addResponse(packet);
                }
            }
        });

        sessions.add(session);
    }

    public void setSessions(ArrayList<Session> sessions) {
        this.sessions = sessions;

        for (int i = 0; i < sessions.size(); i++) {
            sessions.get(i).addNetListener(new NetAdapter() {
                @Override
                public void packetRecieved(Packet packet, Session session) {
                    if (PacketFunctions.isRequest(packet)) {
                        fireRequestRecieved(packet, session);
                    } else if (PacketFunctions.isResponce(packet)) {
                        addResponse(packet);
                    }
                }
            });
        }
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }

    public void addResponse(Packet responce) {
        responses.add(responce);
    }

    public void removeResponse(Packet responce) {
        responses.remove(responce);
    }
}
