package models;

public class Notification {
    private String id;
    private String title;
    private String body;
    private String posterName;
    private String posterId;
    private long time;

    public Notification(){}

    public Notification(String title, String body, String posterName, String posterId, long time){
        this.title = title;
        this.body = body;
        this.posterName = posterName;
        this.posterId = posterId;
        this.time = time;
    }

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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
