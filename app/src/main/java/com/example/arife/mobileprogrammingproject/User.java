package com.example.arife.mobileprogrammingproject;

/**
 * Created by Arife on 4.05.2018.
 */

public class User {
    private String name;
    private String mail;
    private String image;
    private int dailyCount; //gönderi atma hakkı var mı?

    public User() {
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public String getMail() {
        return mail;
    }

    public int getDailyCount() {
        return dailyCount;
    }

    public void setDailyCount(int dailyCount) {
        this.dailyCount = dailyCount;
    }
}
