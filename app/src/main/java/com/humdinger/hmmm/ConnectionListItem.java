package com.humdinger.hmmm;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by jasonhk1020 on 5/18/2015
 */
public class ConnectionListItem implements Serializable{
    private String status;
    private Calendar date;
    private String room;
    private String matchUid;

    public ConnectionListItem(String room, String matchUid) {
        this("Open", Calendar.getInstance(), room, matchUid);
    }

    public ConnectionListItem(String status, Calendar date, String room, String matchUid) {
        setStatus(status);
        setDate(date);
        setRoom(room);
        setMatchUid(matchUid);
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

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRoom() {
        return room;
    }

    public void setMatchUid(String matchUid) {
        this.matchUid = matchUid;
    }

    public String getMatchUid() {
        return matchUid;
    }

}