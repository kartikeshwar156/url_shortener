package com.project.url_shortener.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "url_mappings")
public class UrlMapping {

    @Id
    private String id;

    @Indexed(unique = true)
    private String shortCode;

    @Indexed
    private String longUrl;

    private boolean customAlias;

    private Instant createdAt;

    private long clickCount;

    public UrlMapping() {
    }

    public UrlMapping(String shortCode, String longUrl, boolean customAlias) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.customAlias = customAlias;
        this.createdAt = Instant.now();
        this.clickCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public boolean isCustomAlias() {
        return customAlias;
    }

    public void setCustomAlias(boolean customAlias) {
        this.customAlias = customAlias;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public long getClickCount() {
        return clickCount;
    }

    public void setClickCount(long clickCount) {
        this.clickCount = clickCount;
    }
}
