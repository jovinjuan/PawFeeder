package com.uph23.edu.pawfeeder.model;

public class DateItem {
    private int date;
    private String dayName;
    private String month;
    private boolean isSelected;
    private DateItem fullDate;

    public DateItem(int date, String dayName, String month, boolean isSelected, DateItem fullDate){
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

    public DateItem getFullDate() {return fullDate;}
    public void setFullDate(DateItem fullDate) {this.fullDate = fullDate;}
}
