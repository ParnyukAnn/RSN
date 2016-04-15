package com.aparnyuk.rsn.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//!! Добавить проверку в сеттерах на пустые значения обязательних полей и не давать их изменять

public class Calls extends AbstractTask{
    private ArrayList<String> numbers;
    private Sim sim;
//    private String text;
//    private Date date;
    private boolean repeat;
    private int repeatPeriod; // повторять каждый час, день, через месяц
    private int repeatCount; // количество повторений
    private boolean open; // напоминание активно. ???

    public Calls() {
    }

    public Calls(ArrayList<String> numbers, Sim sim, Date date) {
        this.numbers = numbers;
        this.sim = sim;
        this.setText("Call");
        this.setDate(date);
        this.repeat = false;
        this.repeatPeriod = 0;
        this.repeatCount = 0;
        this.open = true;
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
