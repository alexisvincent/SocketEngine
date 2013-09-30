package socketEngine;

import objects.ThreadedSession;
import objects.Packet;

/**
 *
 * @author alexisvincent
 */
public class NetAdapter implements NetListener {

    @Override
    public void sessionStarted(ThreadedSession session) {
    }

    @Override
    public void sessionEnded(ThreadedSession session) {
    }

    @Override
    public void packetRecieved(Packet packet, ThreadedSession session) {
    }

}
