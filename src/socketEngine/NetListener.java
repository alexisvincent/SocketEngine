package socketEngine;

import objects.ThreadedSession;
import objects.Packet;

/**
 *
 * @author alexisvincent
 */
public interface NetListener {
    
    public void sessionStarted(ThreadedSession session);
    public void sessionEnded(ThreadedSession session);
    
    public void packetRecieved(Packet packet, ThreadedSession session);
    
    
}
