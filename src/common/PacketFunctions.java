package common;

import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author alexisvincent
 */
public class PacketFunctions {
    
    public static int DEFAULT_TIMEOUT = 2000;

    public static HashMap<String, String> analyzePacketHeader(Packet packet) {
        HashMap<String, String> packetProperties = new HashMap<>();
        String header = packet.getHeader();
        
        try (Scanner scnHeader = new Scanner(header)) {
            while (scnHeader.hasNext()) {
                try (Scanner scnProperty = new Scanner(scnHeader.next()).useDelimiter("=")) {
                    try {
                        packetProperties.put(scnProperty.next(), scnProperty.next());
                    } catch (Exception e) {
                        //INVALID PROPERTY
                    }
                }
            }
        }

        return packetProperties;
    }

    public static void setHeader(HashMap<String, String> headerProperties, Packet packet) {
        String newHeader = "";

        String[] keys = headerProperties.keySet().toArray(new String[0]);
        String[] values = headerProperties.values().toArray(new String[0]);

        for (int i = 0; i < headerProperties.size(); i++) {
            newHeader += " " + keys[i] + "=" + values[i];
        }

        packet.setHeader(newHeader);

    }

    public static String getValue(String key, Packet packet) {

        HashMap<String, String> packetProperties = analyzePacketHeader(packet);

        for (int i = 0; i < packetProperties.size(); i++) {
            if (packetProperties.containsKey(key)) {
                return packetProperties.get(key);
            }
        }

        return null;
    }

    public static boolean isRequest(Packet packet) {

        String type = getValue("type", packet);
        
        if (type.equalsIgnoreCase("request")) {
            return true;
        }

        return false;
    }

    public static boolean isResponce(Packet packet) {

        String type = getValue("type", packet);

        if (type.equalsIgnoreCase("response")) {
            return true;
        }

        return false;
    }

    public static String getID(Packet packet) {
        return getValue("id", packet);
    }

    public static void removeProperty(Packet packet, String key) {
        analyzePacketHeader(packet).remove(key);
    }

    public static void addProperties(Packet packet, HashMap<String, String> properties) {

        HashMap<String, String> packetProperties = analyzePacketHeader(packet);
        packetProperties.putAll(properties);
        setHeader(packetProperties, packet);
    }
    
    public static void addProperty(Packet packet, String key, String value) {

        HashMap<String, String> property = new HashMap<>();
        property.put(key, value);
        addProperties(packet, property);
    }
    
    private static String generateKey(int size) {
        String key = "";

        for (int i = 0; i < size; i++) {
            char character = '*';
            while (!Character.isLetterOrDigit(character)) {
                character = (char) ((Math.random() * 97) + 42);
            }
            key += character;
        }

        return key;
    }
    
    public static String generateID() {
        return generateKey(5);
    }

    public static void setID(Packet packet, String id) {
        addProperty(packet, "id", id);
    }
    
    public static void fattenPacket(Packet packet) {
        if (getID(packet) == null) {
            setID(packet, generateID());
        }
        
        if (getTimeOut(packet) == 0) {
            setTimeOut(packet, 2000);
        }
        
        //OTHER DEFAULT VALUES
    }
    
    public static Packet generatePacket() {
        return new Packet("");
    }
    
    public static Packet generateResponse(Packet packet) {
        Packet response = generatePacket();
        PacketFunctions.addProperty(response, "type", "response");
        PacketFunctions.setID(response, PacketFunctions.getID(packet));
        return response;
    }
    
    public static Packet generateRequest() {
        Packet request = generatePacket();
        PacketFunctions.addProperty(request, "type", "request");
        return request;
    }
    
    public static Packet generatePing() {
        Packet ping = generateRequest();
        PacketFunctions.addProperty(ping, "method", "ping");
        PacketFunctions.addProperty(ping, "timeSent", ""+System.currentTimeMillis());
        return ping;
    }
    
    public static Packet generatePingReply(Packet packet) {
        addProperty(packet, "type", "response");
        return packet;
    }
    
    public static boolean isPing(Packet packet) {

        if (PacketFunctions.isRequest(packet) && getValue("method", packet).equalsIgnoreCase("ping")) {
            return true;
        }

        return false;
    }
    
    public static void setTimeOut(Packet packet, int timeout) {
        addProperty(packet, "timeout", ""+DEFAULT_TIMEOUT);
    }
    
    public static int getTimeOut(Packet packet) {
        try{
            return Integer.parseInt(getValue("timeout", packet));
        } catch (Exception e) {
            return 0;
        }
        
    }
}
