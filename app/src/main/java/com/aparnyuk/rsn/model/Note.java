package com.aparnyuk.rsn.model;
//!! Добавить проверку в сеттерах на пустые значения обязательних полей и не давать их изменять
import java.util.Date;

public class Note extends AbstractTask {

    public Note() {

    }

    public Note(String text,Date date) {
        this.setText(text);
        this.setDate(date);
    }

}
