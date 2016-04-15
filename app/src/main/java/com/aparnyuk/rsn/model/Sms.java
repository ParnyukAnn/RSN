package com.aparnyuk.rsn.model;
//!! Добавить проверку в сеттерах на пустые значения обязательних полей и не давать их изменять

import java.util.ArrayList;
import java.util.Date;

public class Sms extends AbstractTask {
    private ArrayList<String> numbers;
    private Sim sim;
    //    private String text;
//    private Date date;
    private boolean notificationBefore;
    private int notificationTime;
    private boolean deliveryReport;
    private boolean repeat;
    private int repeatPeriod; // повторять каждый час, день, через месяц
    private int repeatCount; // количество повторений
    private boolean open; // задание активно. ???

    public Sms() {
    }

    public Sms(ArrayList<String> numbers, Sim sim, String text, Date date) {
        this.numbers = numbers;
        this.sim = sim;
//        this.text = text;
//        this.date = date;
        this.setText(text);
        this.setDate(date);
        this.notificationBefore = false;
        this.deliveryReport = false;
        this.repeat = false;
        this.repeatPeriod = 0;
        this.repeatCount = 0;
        this.open = true;
    }

//    public Sms(ArrayList<String> numbers, Sim sim, String text, Date date, int i) {
//        this.numbers = numbers;
//        this.sim = sim;
//        this.text = text;
//        this.date = date;
////        this.setText(text);
////        this.setDate(date);
//        this.notificationBefore = false;
//        this.deliveryReport = false;
//        this.repeat = false;
//        this.repeatPeriod = 0;
//        this.repeatCount = 0;
//        this.open = true;
//    }

    public int getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(int notificationTime) {
        this.notificationTime = notificationTime;
    }

    public boolean isNotificationBefore() {
        return notificationBefore;
    }

    public void setNotificationBefore(boolean notificationBefore) {
        this.notificationBefore = notificationBefore;
    }

    public boolean isDeliveryReport() {
        return deliveryReport;
    }

    public void setDeliveryReport(boolean deliveryReport) {
        this.deliveryReport = deliveryReport;
    }

    public ArrayList<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(ArrayList<String> numbers) {
        this.numbers = numbers;
    }

    public Sim getSim() {
        return sim;
    }

    public void setSim(Sim sim) {
        this.sim = sim;
    }

/*    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }*/

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public int getRepeatPeriod() {
        return repeatPeriod;
    }

    public void setRepeatPeriod(int repeatPeriod) {
        this.repeatPeriod = repeatPeriod;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
