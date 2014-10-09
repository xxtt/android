package com.example.xml;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

public class Place implements Parcelable {

    private String title;
    private String text;
    private double x;
    private double y;
    private String youTubeId;
    private int category;
    private String address;
    private String phone;
    private String link;
    private String news;
    private boolean smoking;
    private boolean baby;
    private boolean parking;
    private boolean music;
    private Marker marker = null;
    private int resourceId;

    public Place(Parcel source) {
        title = source.readString();
        text = source.readString();
        x = source.readDouble();
        y = source.readDouble();
        youTubeId = source.readString();
        address = source.readString();
        phone = source.readString();
        link = source.readString();
        news = source.readString();
        smoking = source.readByte() != 0;
        baby = source.readByte() != 0;
        parking = source.readByte() != 0;
        music = source.readByte() != 0;
        resourceId = source.readInt();

    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setNews(String news) {
        this.news = news;
    }

    public void setSmoking(boolean smoking) {
        this.smoking = smoking;
    }

    public void setBaby(boolean baby) {
        this.baby = baby;
    }

    public void setParking(boolean parking) {
        this.parking = parking;
    }

    public void setMusic(boolean music) {
        this.music = music;
    }

    public String getAddress() {

        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getLink() {
        return link;
    }

    public String getNews() {
        return news;
    }

    public boolean isSmoking() {
        return smoking;
    }

    public boolean isBaby() {
        return baby;
    }

    public boolean isParking() {
        return parking;
    }

    public boolean isMusic() {
        return music;
    }

    public Place(String title, String text, double x, double y, String youTubeId, int category, String address, String phone, String link, String news, boolean smoking, boolean baby, boolean parking, boolean music) {
        this.title = title;
        this.text = text;
        this.x = x;
        this.y = y;
        this.youTubeId = youTubeId;
        this.category = category;
        this.address = address;
        this.phone = phone;
        this.link = link;
        this.news = news;
        this.smoking = smoking;
        this.baby = baby;
        this.parking = parking;
        this.music = music;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Marker getMarker() {

        return marker;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setYouTubeId(String youTubeId) {
        this.youTubeId = youTubeId;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getYouTubeId() {
        return youTubeId;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getCategory() {
        return category;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getResourceId() {
        return resourceId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(text);
        dest.writeDouble(x);
        dest.writeDouble(y);
        dest.writeString(youTubeId);
        dest.writeString(address);
        dest.writeString(phone);
        dest.writeString(link);
        dest.writeString(news);
        dest.writeByte((byte) (smoking ? 1 : 0));
        dest.writeByte((byte) (baby ? 1 : 0));
        dest.writeByte((byte) (parking ? 1 : 0));
        dest.writeByte((byte) (music ? 1 : 0));
        dest.writeInt(resourceId);
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel source) {
            return new Place(source);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}
