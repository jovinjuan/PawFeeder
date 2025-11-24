package com.uph23.edu.pawfeeder.model;

import java.util.Date;

public class DateItem {
    private int date;
    private String dayName;
    private String month;
    private boolean isSelected;
    private Date fullDate;

    public DateItem(int date, String dayName, String month, boolean isSelected, Date fullDate){
        this.date = date;
        this.dayName = dayName;
        this.month = month;
        this.isSelected = isSelected;
        this.fullDate = fullDate;
    }

    //get date
    public int getDate() {return date;}
    public void setDate(int date) {this.date = date;}

    public String getDayName() {return dayName;}
    public void setDayName(String dayName) {this.dayName = dayName;}

    public String getMonth() {return month;}
    public void setMonth(String month) {this.month = month;}

    public boolean isSelected() {return isSelected;}
    public void setSelected(boolean isSelected) {this.isSelected = isSelected;}

    public Date getFullDate() {return fullDate;}
    public void setFullDate(Date fullDate) {this.fullDate = fullDate;}
}
