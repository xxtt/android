package com.example.xml;

import com.google.android.gms.maps.model.Marker;

public class Place {

    private String title;
    private String text;
    private double x;
    private double y;
    private String youTubeId;
    private String category;
    private Marker marker =null;

    public Place(String title, String text, double x, double y, String youTubeId, String category) {
        this.title = title;
        this.text = text;
        this.x = x;
        this.y = y;
        this.youTubeId = youTubeId;
        this.category = category;
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

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}
