package com.example.arife.mobileprogrammingproject;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;

/**
 * Help class
 */

public class Help {

    private String content;
    private String description;
    private String title;
    private String name;
    private String uId;

    public Help(){

    }
    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
