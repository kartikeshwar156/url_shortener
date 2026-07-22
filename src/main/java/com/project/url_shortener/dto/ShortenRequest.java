package com.project.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ShortenRequest {

    @NotBlank(message = "must not be blank")
    private String url;

    @Size(min = 3, max = 32, message = "must be between 3 and 32 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "must contain only letters, digits, underscores, or hyphens")
    private String customAlias;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }
}
