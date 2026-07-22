package com.project.url_shortener.service;

public interface UrlShortenerService {

    ShortenResult shorten(String url, String customAlias);

    String resolve(String shortCode);
}
