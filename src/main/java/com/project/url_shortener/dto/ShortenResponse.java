package com.project.url_shortener.dto;

public class ShortenResponse {

    private String shortCode;
    private String shortUrl;
    private String longUrl;
    private boolean reused;

    public ShortenResponse() {
    }

    public ShortenResponse(String shortCode, String shortUrl, String longUrl, boolean reused) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
        this.reused = reused;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public boolean isReused() {
        return reused;
    }

    public void setReused(boolean reused) {
        this.reused = reused;
    }
}
