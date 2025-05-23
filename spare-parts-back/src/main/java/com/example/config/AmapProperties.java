package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "amap")
public class AmapProperties {
    private String apiKey;
    private String geocodeUrl;

    // 必须提供getter/setter
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getGeocodeUrl() { return geocodeUrl; }
    public void setGeocodeUrl(String geocodeUrl) { this.geocodeUrl = geocodeUrl; }
}