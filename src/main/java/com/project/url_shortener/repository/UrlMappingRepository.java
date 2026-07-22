package com.project.url_shortener.repository;

import com.project.url_shortener.model.UrlMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UrlMappingRepository extends MongoRepository<UrlMapping, String> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    Optional<UrlMapping> findByLongUrlAndCustomAliasFalse(String longUrl);
}
