package models;

import java.util.ArrayList;

public class Video {
    private String id;
    private String title;
    private String des;
    private String urlIndex;
    private String section;
    private long time;
    private long likesCount;
    private ArrayList likes;

    public Video(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getUrlIndex() {
        return urlIndex;
    }

    public void setUrlIndex(String urlIndex) {
        this.urlIndex = urlIndex;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public ArrayList getLikes() {
        return likes;
    }

    public void setLikes(ArrayList likes) {
        this.likes = likes;
    }

}
