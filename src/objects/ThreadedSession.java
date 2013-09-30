package objects;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import socketEngine.NetAdapter;
import socketEngine.NetExceptions;
import socketEngine.NetListener;
import helpers.PacketFunctions;

/**
 *
 * @author alexisvincent
 */
public class ThreadedSession implements Runnable {

    private Socket socket;
    private Thread sessionThread;
    private boolean active;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ArrayList<NetListener> listeners;
    private ArrayList<String> tags;

    public ThreadedSession(Socket socket) {

        initVars(socket);
        setActive(true);
    }

    private void initVars(Socket socket) {
        this.socket = socket;
        sessionThread = new Thread(this);

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.out.println("Failed to init streams");
        }

        listeners = new ArrayList<>();
        tags = new ArrayList<>();
    }

    private void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;

            if (active) {
                sessionThread.start();
                fireSessionStarted();
            } else {
                fireSessionEnded();
            }
        }
    }

    public synchronized void postPacket(Packet packet) {

        try {
            if (active) {
                //First make sure it has defaults
                PacketFunctions.fattenPacket(packet);
                out.flush();
                out.writeObject(packet);
                out.flush();
            } else {
                throw new IOException();
            }

        } catch (IOException ex) {
            System.out.println("Failed to post packet, Maybe session is dead?");
        }
    }

    /**
     *
     * This method is blocking. For the amount of time it takes to receive a
     * response, or until the timeout is reached.
     *
     * @param packet The packet you wish to post
     * @return Response Packet
     */
    public Packet postRequest(Packet packet) throws NetExceptions.RequestTimedOutException {

        //specific to the post request method
        final ArrayList<Packet> responses = new ArrayList<>(); //Additionally used as lock.
        final Thread responseThread = Thread.currentThread();

        NetListener responseListener = new NetAdapter() {
            @Override
            public void packetRecieved(Packet packet, ThreadedSession session) {
                synchronized (responseThread) {
                    responses.add(packet);
                    responseThread.notify();
                }
            }
        };

        this.addNetListener(responseListener);

        synchronized (responseThread) {

            //This also sets default values before sending with .fattenPacket()
            this.postPacket(packet);
            long timeSent = System.currentTimeMillis();

            int timeout = PacketFunctions.getTimeOut(packet);
            String packetID = PacketFunctions.getID(packet);

            int timeLeft = timeout;
            while (timeLeft > 0) {
                try {
                    responseThread.wait(timeLeft);
                } catch (InterruptedException ex) {
                    //Hey There Delila...
                }

                for (int i = 0; i < responses.size();) {
                    if (packetID.equals(PacketFunctions.getID(responses.get(i)))) {
                        removeNetListener(responseListener);
                        return responses.get(i);
                    } else {
                        responses.remove(i);
                    }
                }

                timeLeft = (int) (timeout - (System.currentTimeMillis() - timeSent));
            }

            removeNetListener(responseListener);
            throw new NetExceptions.RequestTimedOutException();

        }
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public boolean hasTag(String tag) {
        for (String tagItem : getTags()) {
            if (tagItem.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public int ping() throws NetExceptions.RequestTimedOutException {
        long timeSent = Long.parseLong(PacketFunctions.getValue("timeSent", this.postRequest(PacketFunctions.generatePing())));
        return (int) (System.currentTimeMillis() - timeSent);
    }

    @Override
    public void run() {
        while (active && in != null) {
            try {
                Packet packet = (Packet) in.readObject();
                firePacketRecieved(packet);
            } catch (IOException ex) {
                setActive(false);
            } catch (ClassNotFoundException ex) {
                System.out.println("Bad Packet Recieved");
            }
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void addNetListener(NetListener listener) {
        this.listeners.add(listener);
    }

    public void removeNetListener(NetListener listener) {
        this.listeners.remove(listener);
    }

    public void fireSessionStarted() {
        for (NetListener netListener : listeners) {
            netListener.sessionStarted(this);
        }
    }

    public void fireSessionEnded() {
        for (NetListener netListener : listeners) {
            netListener.sessionEnded(this);
        }
    }

    public void firePacketRecieved(Packet packet) {
        for (NetListener netListener : listeners) {
            netListener.packetRecieved(packet, this);
        }
    }
}
