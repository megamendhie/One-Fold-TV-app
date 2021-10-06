package models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Event implements Parcelable {
    private String field;
    private String id;
    private String posterId;
    private String posterName;
    private String title;
    private String about;
    private String venue;
    private long timeEvent;
    private long timePosted;

    public Event(){}

    public Event(String field, String title, String venue, String about, String posterName, String posterId, long timeEvent ){
        this.field = field;
        this.title = title;
        this.venue = venue;
        this.about = about;
        this.posterName = posterName;
        this.posterId = posterId;
        this.timeEvent = timeEvent;
        id = "";
        timePosted = new Date().getTime();
    }

    protected Event(Parcel in) {
        field = in.readString();
        id = in.readString();
        posterId = in.readString();
        posterName = in.readString();
        title = in.readString();
        about = in.readString();
        venue = in.readString();
        timeEvent = in.readLong();
        timePosted = in.readLong();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public long getTimeEvent() {
        return timeEvent;
    }

    public void setTimeEvent(long timeEvent) {
        this.timeEvent = timeEvent;
    }

    public long getTimePosted() {
        return timePosted;
    }

    public void setTimePosted(long timePosted) {
        this.timePosted = timePosted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(field);
        parcel.writeString(id);
        parcel.writeString(posterId);
        parcel.writeString(posterName);
        parcel.writeString(title);
        parcel.writeString(about);
        parcel.writeString(venue);
        parcel.writeLong(timeEvent);
        parcel.writeLong(timePosted);
    }
}
