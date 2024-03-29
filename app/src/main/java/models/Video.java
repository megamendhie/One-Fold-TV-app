package models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Video implements Parcelable {
    private String id;
    private String title;
    private String des;
    private String urlIndex;
    private String type;
    private String posterName;
    private String posterId;
    private long time;
    private long likesCount;
    private List<String> likes = new ArrayList<>();
    private boolean active;

    public Video(){}

    public Video(String title, String des, String urlIndex, String posterName, String posterId, long time){
        this.active = true;
        this.title = title;
        this.des = des;
        this.urlIndex = urlIndex;
        this.id = "live";
        this.type = "live";
        this.posterName = posterName;
        this.posterId = posterId;
        this.time = time;
        likesCount= 0;
    }

    public Video(String title, String des, String urlIndex, String type, String posterName, String posterId, long time){
        this.title = title;
        this.des = des;
        this.urlIndex = urlIndex;
        this.type = type;
        this.posterName = posterName;
        this.posterId = posterId;
        this.time = time;
        likesCount= 0;
    }

    protected Video(Parcel in){
        id = in.readString();
        title = in.readString();
        des = in.readString();
        urlIndex = in.readString();
        type = in.readString();
        posterName = in.readString();
        posterId = in.readString();
        time = in.readLong();
        likesCount = in.readLong();
        likes = in.createStringArrayList();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public void addLike(String userId){
        this.likes.add(userId);
    }

    public void removeLike(String userId){
        this.likes.remove(userId);
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(des);
        parcel.writeString(urlIndex);
        parcel.writeString(type);
        parcel.writeString(posterName);
        parcel.writeString(posterId);
        parcel.writeLong(time);
        parcel.writeLong(likesCount);
        parcel.writeStringList(likes);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
