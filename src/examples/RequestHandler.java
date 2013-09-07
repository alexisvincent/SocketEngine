package examples;

import common.NetAdapter;
import common.Packet;
import common.PacketFunctions;
import common.PacketHandler;
import common.Session;
import java.util.HashMap;

/**
 *
 * @author alexisvincent
 */
public class RequestHandler {
    private PacketHandler packetHandler;

    public RequestHandler(PacketHandler packetHandler) {
        initVars(packetHandler);
    }
    
    private void initVars(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
        
        packetHandler.addRequestListener(new NetAdapter() {

            @Override
            public void requestRecieved(Packet request, Session session) {
                System.out.println("Request Recieved: " + request.getHeader());
                doRequest(request, session);
            }
        });
    }
    
    private void doRequest(Packet packet, Session session) {
        
        HashMap<String, String> packetProperties = PacketFunctions.analyzePacketHeader(packet);
        Object payload = packet.getPayload();
        
        Packet response = PacketFunctions.generateResponse(packet);
        
        if (PacketFunctions.isPing(packet)) {
            System.out.println("recieved ping");
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
