package common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author alexisvincent
 */
public class Session implements Runnable {

    private Socket socket;
    private Thread sessionThread;
    private boolean active;
    
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    private ArrayList<NetListener> listeners;

    public Session(Socket socket) {
        
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

    public void postPacket(Packet packet) {
        
        //First make sure it has defaults
        PacketFunctions.fattenPacket(packet);
        
        try {
            if (active) {
                out.writeObject(packet);
            } else {
                throw new IOException();
            }
            
        } catch (IOException ex) {
            System.out.println("Failed to post packet, Maybe stream is dead?");
        }
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
    
    public void addNetListener(NetListener listener) {
        this.listeners.add(listener);
    }
    
    public void removeNetListener(NetListener listener) {
        this.listeners.remove(listener);
    }
    
    public void fireSessionStarted() {
        for (int i=0;i<listeners.size();i++) {
            listeners.get(i).sessionStarted(this);
        }
    }
    
    public void fireSessionEnded() {
        for (int i=0;i<listeners.size();i++) {
            listeners.get(i).sessionEnded(this);
        }
    }
    
    public void firePacketRecieved(Packet packet) {
        for (int i=0;i<listeners.size();i++) {
            listeners.get(i).packetRecieved(packet, this);
        }
    }
}
