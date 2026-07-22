package com.project.url_shortener.controller;

import com.project.url_shortener.config.AppProperties;
import com.project.url_shortener.dto.ShortenRequest;
import com.project.url_shortener.dto.ShortenResponse;
import com.project.url_shortener.model.UrlMapping;
import com.project.url_shortener.service.ShortenResult;
import com.project.url_shortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;
    private final AppProperties appProperties;

    public UrlShortenerController(UrlShortenerService urlShortenerService, AppProperties appProperties) {
        this.urlShortenerService = urlShortenerService;
        this.appProperties = appProperties;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        ShortenResult result = urlShortenerService.shorten(request.getUrl(), request.getCustomAlias());
        UrlMapping mapping = result.mapping();
        ShortenResponse response = new ShortenResponse(
                mapping.getShortCode(),
                buildShortUrl(mapping.getShortCode()),
                mapping.getLongUrl(),
                result.reused()
        );
        HttpStatus status = result.reused() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable("code") String code) {
        String longUrl = urlShortenerService.resolve(code);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(longUrl));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    private String buildShortUrl(String shortCode) {
        String baseUrl = appProperties.baseUrl();
        if (baseUrl.endsWith("/")) {
            return baseUrl + shortCode;
        }
        return baseUrl + "/" + shortCode;
    }
}
