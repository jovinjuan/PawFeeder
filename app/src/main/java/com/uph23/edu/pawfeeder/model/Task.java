package com.uph23.edu.pawfeeder.model;

public class Task {
    private String Description;
    private String Id_User;
    private String Priority;
    private String Title;
    private String docId;
    public Task(){}

    public Task(String description, String id_User, String priority, String title, String docId) {
        Description = description;
        Id_User = id_User;
        Priority = priority;
        Title = title;
        this.docId = docId;
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

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
}
