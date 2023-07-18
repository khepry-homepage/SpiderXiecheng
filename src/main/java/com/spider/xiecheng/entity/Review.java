package com.spider.xiecheng.entity;

import java.util.Date;

public class Review {
    private String reviewer;
    private String content;
    private Date date;

    public String getReviewer() {
        return reviewer;
    }

    public Review setReviewer(String reviewer) {
        this.reviewer = reviewer;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Review setContent(String content) {
        this.content = content;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public Review setDate(Date date) {
        this.date = date;
        return this;
    }
}
