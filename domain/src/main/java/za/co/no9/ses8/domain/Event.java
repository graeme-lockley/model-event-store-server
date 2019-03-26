package za.co.no9.ses8.domain;

import java.util.Date;

public class Event {
    public final int id;
    public final Date when;
    public final String name;
    public final String content;


    public Event(int id, Date when, String name, String content) {
        this.id = id;
        this.when = when;
        this.name = name;
        this.content = content;
    }


    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", when=" + when +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
