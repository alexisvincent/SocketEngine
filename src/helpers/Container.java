package helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author alexisvincent
 */
public class Container {

    private ArrayList<Container> containers;
    private HashMap<String, String> properties;

    public Container() {
        initVars();
    }
    
    public Container(Element xmlContainer) throws JDOMException {
        initVars();
        setElement(xmlContainer);
    }

    public Container(String xmlPayload) throws JDOMException {
        initVars();
        setElement(buildElement(xmlPayload));
    }

    private void initVars() {
        containers = new ArrayList<>();
        properties = new HashMap<>();
    }
    
    private static Element buildElement(String xmlString) throws JDOMException {
        ByteArrayInputStream xmlInputStream = null;
        try {
            xmlInputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
            return new SAXBuilder().build(xmlInputStream).getRootElement();
        } catch (JDOMException | IOException ex) {
            throw new JDOMException();
        } finally {
            try {
                xmlInputStream.close();
            } catch (IOException ex) {
            }
        }
    }
    
    private void setElement(Element element) throws JDOMException {
        
        int containerCount = Integer.parseInt(element.getAttributeValue("ContainerCount"));
        int propertiesCount = Integer.parseInt(element.getAttributeValue("PropertiesCount"));
        
        for (int i = 0; i < containerCount; i++) {
            addContainer(new Container(element.getChild("Container"+i)));
        }
        
        ArrayList<String> propertyKeys = new ArrayList<>();
        
        for (int i = 0; i < propertiesCount; i++) {
            propertyKeys.add(element.getAttributeValue("Property"+i));
        }
        
        for (int i = 0; i < propertiesCount; i++) {
            properties.put(propertyKeys.get(i), element.getAttributeValue(propertyKeys.get(i)));
        }
    }
    
    public String getPayload() {
        return new XMLOutputter().outputString(new Document(this.getElement()));
    }
    
    public Element getElement() {
        Element element = new Element("Container");
        
        element.setAttribute("ContainerCount", ""+getContainerCount());
        for (int i = 0; i < getContainerCount(); i++) {
            element.addContent(getContainers().get(i).getElement());
        }
        
        element.setAttribute("PropertyCount", ""+getPropertyCount());
        
        String [] keySet = getProperties().keySet().toArray(new String[0]);
        String [] values = getProperties().values().toArray(new String[0]);
        for (int i = 0; i < getPropertyCount(); i++) {
            element.setAttribute("Property"+i, keySet[i]);
        }
        for (int i = 0; i < getPropertyCount(); i++) {
            element.setAttribute(keySet[i], values[i]);
        }
        
        return element;
    }

    public ArrayList<Container> getContainers() {
        return containers;
    }

    public void setContainers(ArrayList<Container> containers) {
        this.containers = containers;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }
    
    public int getPropertyCount() {
        return getProperties().size();
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }
    
    private void addContainer(Container container) {
        this.containers.add(container);
    }
    
    private void removeContainer(Container container) {
        this.containers.remove(container);
    }
    
    private int getContainerCount() {
        return containers.size();
    }
    
    public void addProperties(HashMap<String, String> properties) {
        this.properties.putAll(properties);
    }
    
    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }
    
    public void removeProperties(ArrayList<String> properties) {
        for (String string : properties) {
            this.properties.remove(string);
        }
    }
    
    public void removeProperty(String key) {
        this.properties.remove(key);
    }
}
