package com.aparnyuk.rsn.model;
//!! Добавить проверку в сеттерах на пустые значения обязательних полей и не давать их изменять

import java.util.Date;

public class Remind extends AbstractTask{
//    private String text;
//    private Date date;
    private boolean repeat;
    private long repeatPeriod; // повторять каждый час, день, через месяц
    private int repeatCount; // количество повторений
    private boolean open; // напоминание активно. ???

    public Remind() {
    }

    public Remind(String text, Date date) {
/*        this.text = text;
        this.date = date;*/
        this.setText(text);
        this.setDate(date);
        this.repeat = false;
        this.repeatPeriod = 0;
        this.repeatCount = 0;
        this.open = true;
    }

/*    public void setText(String text) {
        this.text = text;
    }

    public void setDate(Date date) {
        this.date = date;
    }*/

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public void setRepeatPeriod(long repeatPeriod) {
        this.repeatPeriod = repeatPeriod;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

/*
    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }
*/

    public boolean isRepeat() {
        return repeat;
    }

    public long getRepeatPeriod() {
        return repeatPeriod;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public boolean isOpen() {
        return open;
    }
}
