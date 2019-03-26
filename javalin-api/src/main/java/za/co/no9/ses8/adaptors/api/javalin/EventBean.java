package za.co.no9.ses8.adaptors.api.javalin;

import za.co.no9.ses8.domain.Event;

import java.util.Date;


public class EventBean {
    public int id;
    public Date when;
    public String name;
    public String content;


    public EventBean(int id, Date when, String name, String content) {
        this.id = id;
        this.when = when;
        this.name = name;
        this.content = content;
    }


    public static final EventBean from(Event event) {
        return new EventBean(event.id, event.when, event.name, event.content);
    }
}
