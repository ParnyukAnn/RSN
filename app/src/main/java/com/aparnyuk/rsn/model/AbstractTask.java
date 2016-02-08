package com.aparnyuk.rsn.model;

import java.util.Date;

public class AbstractTask {
    private int ID;
    private String text;
    private Date date;

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getID() {
        return ID;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }
}
