package com.uph23.edu.pawfeeder.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Schedule implements Parcelable {
    private String id;
    private String Id_User;
    private String Schedule_Name;
    private String Portion;
    private String FeedTime;
    private String FeedDate;
    private boolean notification;

    public Schedule(){

    }

    public Schedule(String id,String Id_User, String schedule_Name, String portion, String feedTime, String feedDate, boolean repeat_weekly, boolean notification) {
        this.id = id;
        this.Id_User = Id_User;
        Schedule_Name = schedule_Name;
        Portion = portion;
        FeedTime = feedTime;
        FeedDate = feedDate;
        this.notification = notification;
    }
    protected Schedule(Parcel in) {
        id = in.readString();
        Id_User = in.readString();
        Schedule_Name = in.readString();
        FeedTime = in.readString();
        Portion = in.readString();
        FeedDate = in.readString();
        notification = in.readByte() != 0;
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(Id_User);
        dest.writeString(Schedule_Name);
        dest.writeString(FeedTime);
        dest.writeString(Portion);
        dest.writeString(FeedDate);
        dest.writeByte((byte) (notification ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchedule_Name() {
        return Schedule_Name;
    }

    public void setSchedule_Name(String schedule_Name) {
        Schedule_Name = schedule_Name;
    }

    public String getPortion() {
        return Portion;
    }

    public void setPortion(String portion) {
        Portion = portion;
    }

    public String getFeedTime() {
        return FeedTime;
    }

    public void setFeedTime(String feedTime) {
        FeedTime = feedTime;
    }

    public String getFeedDate() {
        return FeedDate;
    }

    public void setFeedDate(String feedDate) {
        FeedDate = feedDate;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getId_User() {
        return Id_User;
    }

    public void setId_User(String id_User) {
        Id_User = id_User;
    }
}
