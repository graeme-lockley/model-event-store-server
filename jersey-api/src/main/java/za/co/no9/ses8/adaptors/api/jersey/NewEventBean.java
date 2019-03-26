package za.co.no9.ses8.adaptors.api.jersey;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NewEventBean {
    public String name;
    public String content;


    public NewEventBean() {
    }


    public NewEventBean(String name, String content) {
        this.name = name;
        this.content = content;
    }
}
