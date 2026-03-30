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
    public String getDescription(){
        String time = "";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("hh.mm a", Locale.getDefault());

            Date date = input.parse(Timestamp);
            time = output.format(date);
        } catch (Exception e) {
            if (Timestamp != null && Timestamp.length() >= 16) {
                time = Timestamp.substring(11, 16);
            }
        }
        return time + "-" + Portion + "g";
    }
}
