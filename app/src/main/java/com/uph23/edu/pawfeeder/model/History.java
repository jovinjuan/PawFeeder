package com.uph23.edu.pawfeeder.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class History {
    private String Id_User, ScheduleID, Status, Timestamp, Title, Type;
    private int Portion;

    public History(String id_User, String scheduleID, String status, String timestamp, String title, String type, int portion) {
        Id_User = id_User;
        ScheduleID = scheduleID;
        Status = status;
        Timestamp = timestamp;
        Title = title;
        Type = type;
        Portion = portion;
    }
    public History(){}

    public String getId_User() {
        return Id_User;
    }

    public void setId_User(String id_User) {
        Id_User = id_User;
    }

    public String getScheduleID() {
        return ScheduleID;
    }

    public void setScheduleID(String scheduleID) {
        ScheduleID = scheduleID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public int getPortion() {
        return Portion;
    }

    public void setPortion(int portion) {
        Portion = portion;
    }
    public String formatTime(String timestamp){
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            Date date = inputFormat.parse(timestamp);

            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return timestamp;
        }
    }
    public String formatPortion(int portion){
        if(portion <= 0){
            return "0 gr";
        }

        return portion + "gr";
    }
}
