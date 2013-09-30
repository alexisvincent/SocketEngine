package helpers;

import objects.Packet;
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

        try (Scanner scnHeader = new Scanner(packet.getHeader())) {
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

        if (packetProperties.containsKey(key)) {
            return packetProperties.get(key);
        }

        return null;
    }

    public static boolean isRequest(Packet packet) {

        if (getValue("type", packet).equalsIgnoreCase("request")) {
            return true;
        }

        return false;
    }

    public static boolean isResponce(Packet packet) {

        if (getValue("type", packet).equalsIgnoreCase("response")) {
            return true;
        }

        return false;
    }

    public static String getID(Packet packet) {
        return getValue("id", packet);
    }

    public static void removeProperty(Packet packet, String key) {
        HashMap<String, String> properties = analyzePacketHeader(packet);
        properties.remove(key);
        setHeader(properties, packet);
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

    private static String generateID(int size) {
        String key = "";

        for (int i = 0; i < size; i++) {
            key += (char)((Math.random() * 30) + 97);
        }

        return key;
    }

    public static String generateID() {
        return generateID(5);
    }

    public static void setID(Packet packet, String id) {
        addProperty(packet, "id", id);
    }

    public static void fattenPacket(Packet packet) {
        if (getID(packet) == null) {
            setID(packet, generateID());
        }

        if (getTimeOut(packet) == 0) {
            setTimeOut(packet, DEFAULT_TIMEOUT);
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
        PacketFunctions.addProperty(ping, "timeSent", "" + System.currentTimeMillis());
        return ping;
    }

    public static Packet generatePingReply(Packet packet) {
        addProperty(packet, "type", "response");
        return packet;
    }

    public static boolean isPing(Packet packet) {

        if (getValue("method", packet).equalsIgnoreCase("ping")) {
            return true;
        }

        return false;
    }

    public static void setTimeOut(Packet packet, int timeout) {
        addProperty(packet, "timeout", "" + timeout);
    }

    public static int getTimeOut(Packet packet) {
        try {
            return Integer.parseInt(getValue("timeout", packet));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getMethod(Packet packet) {
        return getValue("method", packet);
    }
}
