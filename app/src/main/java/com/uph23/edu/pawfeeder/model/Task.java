package com.uph23.edu.pawfeeder.model;

public class Task {
    private String Description;
    private String Id_User;
    private String Priority;
    private String Title;
    public Task(){}

    public Task(String description, String id_User, String priority, String title) {
        Description = description;
        Id_User = id_User;
        Priority = priority;
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getId_User() {
        return Id_User;
    }

    public void setId_User(String id_User) {
        Id_User = id_User;
    }

    public String getPriority() {
        return Priority;
    }

    public void setPriority(String priority) {
        Priority = priority;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
