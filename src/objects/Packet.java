package objects;

import java.io.Serializable;

/**
 *
 * @author alexisvincent
 */
public class Packet implements Serializable {
    
    /**
     * Packet Header. Containing packet information. By default, 
     * any information omitted will be assigned default values by
     * server, according to server defaults. Any 'bad' header 
     * properties will simply be ignored. keys and values in camelCase.
     * 
     * Format: "key1=value1 key2=value2 key3=value3" ...
     * 
     * Properties are server specific, however here are some standard
     * examples.
     * 
     * type=request | type=response
     * timeout=1000     (in ms)
     * payload=xml
     */
    private String header;
    
    /**
     * Packet Payload. This can be any object whatsoever (serializable).
     * I recommended XML or JSON however to minimize breakage when 
     * implementation changes.
     */
    private Object payload;

    public Packet() {
        this("");
    }

    public Packet(String header) {
        this(header, null);
    }

    public Packet(String header, Object payload) {
        this.header = header;
        this.payload = payload;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
}
