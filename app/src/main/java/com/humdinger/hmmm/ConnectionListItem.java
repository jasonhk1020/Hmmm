package com.humdinger.hmmm;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by jasonhk1020 on 5/18/2015
 */
public class ConnectionListItem implements Serializable{
    private Long id;
    private String text;
    private String status;
    private Calendar date;
    private String room;

    public ConnectionListItem(String text, String room) {
        this(null, text, "Open", Calendar.getInstance(), room);
    }

    public ConnectionListItem(Long id, String text, String status, Calendar date, String room){
        setId(id);
        setText(text);
        setStatus(status);
        setDate(date);
        setRoom(room);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRoom() {
        return room;
    }
}