package com.project.url_shortener.service;

import com.project.url_shortener.model.UrlMapping;

public record ShortenResult(UrlMapping mapping, boolean reused) {
}
