package com.nowcoder.communtiy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hotel")
public class HotelConfig {
    private String originUrl;
    private String region = "广州";

    public String getOriginUrl() {
        return originUrl;
    }

    public String getRegion() {
        return region;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
