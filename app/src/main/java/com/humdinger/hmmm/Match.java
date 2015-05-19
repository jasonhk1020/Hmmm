package com.humdinger.hmmm;

/**
 * Created by jasonhk1020 on 5/15/2015.
 */
public class Match {
    private String uid;
    private String industry;
    private String description;
    private String company;
    private String username;
    private String position;
    private String photoUrl;


    private Match() {}

    public Match(String uid, String industry, String description, String company, String username, String position, String photoUrl) {
        this.uid = uid;
        this.industry = industry;
        this.description = description;
        this.company = company;
        this.username = username;
        this.position = position;
        this.photoUrl = photoUrl;
    }

    public String getUid() { return uid; }

    public String getIndustry() { return industry; }

    public String getDescription() { return description; }

    public String getCompany() { return company; }

    public String getUsername() { return username; }

    public String getPosition() { return position; }

    public String getPhotoUrl() { return photoUrl; }

}
