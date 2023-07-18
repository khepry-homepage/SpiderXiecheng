package com.spider.xiecheng.entity;

import java.util.List;

public class Hotel {
    private int id;
    private String name;
    private String address;
    private List<Review> reviews;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Hotel setName(String name) {
        this.name = name;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Hotel setAddress(String address) {
        this.address = address;
        return this;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public Hotel setReviews(List<Review> reviews) {
        this.reviews = reviews;
        return this;
    }
}
