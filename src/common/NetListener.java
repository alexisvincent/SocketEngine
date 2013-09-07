package common;

/**
 *
 * @author alexisvincent
 */
public interface NetListener {
    
    public void sessionStarted(Session session);
    public void sessionEnded(Session session);
    
    public void packetRecieved(Packet packet, Session session);
    public void requestRecieved(Packet request, Session session);
    public void responceRecieved(Packet responce, Session session);
    
    
}
