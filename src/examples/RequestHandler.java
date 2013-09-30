package examples;

import socketEngine.NetAdapter;
import objects.Packet;
import helpers.PacketFunctions;
import objects.ThreadedSession;
import java.util.HashMap;
import socketEngine.ThreadedSocketEngine;

/**
 *
 * @author alexisvincent
 */
public class RequestHandler {

    public RequestHandler(ThreadedSocketEngine socketEngine) {
        initVars(socketEngine);
    }

    private void initVars(ThreadedSocketEngine socketEngine) {

        socketEngine.addSessionListener(new NetAdapter() {
            @Override
            public void packetRecieved(Packet request, ThreadedSession session) {
                if (PacketFunctions.isRequest(request)) {
                    //System.out.println("Request Recieved: " + request.getHeader());
                    doRequest(request, session);
                }

            }
        });
    }

    private void doRequest(Packet packet, ThreadedSession session) {

        HashMap<String, String> packetProperties = PacketFunctions.analyzePacketHeader(packet);
        Object payload = packet.getPayload();

        Packet response = PacketFunctions.generateResponse(packet);

        if (PacketFunctions.isPing(packet)) {
            System.out.println("Recieved ping");
            session.postPacket(PacketFunctions.generatePingReply(packet));

        } else if (RequestFunctions.getMethod(packet).equals("get")) {
            response.setPayload("Responce Payload");
            session.postPacket(response);

        } else if (RequestFunctions.getMethod(packet).equals("post")) {
        }
    }

    private static class RequestFunctions extends PacketFunctions {

        public static String getMethod(Packet packet) {
            return getValue("method", packet);
        }
    }
}
