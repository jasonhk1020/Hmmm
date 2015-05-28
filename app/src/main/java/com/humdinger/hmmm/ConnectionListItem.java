package com.humdinger.hmmm;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by jasonhk1020 on 5/18/2015
 */
public class ConnectionListItem implements Serializable{
    private Long id;
    private String matchUsername;
    private String status;
    private Calendar date;
    private String room;
    private CharSequence matchInfo;

    public ConnectionListItem(String matchUsername, String room, CharSequence matchInfo) {
        this(null, matchUsername, "Open", Calendar.getInstance(), room, matchInfo);
    }

    public ConnectionListItem(Long id, String matchUsername, String status, Calendar date, String room, CharSequence matchInfo){
        setId(id);
        setMatchUsername(matchUsername);
        setStatus(status);
        setDate(date);
        setRoom(room);
        setMatchInfo(matchInfo);
    }

    public String getMatchUsername() {
        return matchUsername;
    }

    public void setMatchUsername(String matchUsername) {
        this.matchUsername = matchUsername;
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

    public CharSequence getMatchInfo() {
        return matchInfo;
    }

    public void setMatchInfo(CharSequence matchInfo) {
        this.matchInfo = matchInfo;
    }

}