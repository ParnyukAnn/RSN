package com.aparnyuk.rsn;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
    private String name;
    private String number;
    private Bitmap picture;
    private boolean isSelected = false;

    public Contact(String name, String number, Bitmap picture){
        this.name = name;
        this.number = number;
        this.picture = picture;
    }

    protected Contact(Parcel in) {
        name = in.readString();
        number = in.readString();
        picture = in.readParcelable(Bitmap.class.getClassLoader());
        isSelected = in.readByte() != 0;
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public String getName(){
        return name;
    }

    public String getNumber(){
        return number;
    }

    public Bitmap getPicture(){
        return picture;
    }

    public boolean isSelected(){
        return isSelected ;
    }
    public void setSelected(boolean b){
        isSelected = b;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(number);
        dest.writeParcelable(picture, flags);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }
}
