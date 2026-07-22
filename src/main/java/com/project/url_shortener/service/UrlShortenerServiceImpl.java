package com.project.url_shortener.service;

import com.project.url_shortener.exception.AliasAlreadyTakenException;
import com.project.url_shortener.exception.ShortCodeNotFoundException;
import com.project.url_shortener.model.UrlMapping;
import com.project.url_shortener.repository.UrlMappingRepository;
import com.project.url_shortener.util.UrlValidator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private final UrlMappingRepository urlMappingRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final MongoOperations mongoOperations;

    public UrlShortenerServiceImpl(
            UrlMappingRepository urlMappingRepository,
            SequenceGeneratorService sequenceGeneratorService,
            MongoOperations mongoOperations) {
        this.urlMappingRepository = urlMappingRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public ShortenResult shorten(String url, String customAlias) {
        UrlValidator.validate(url);
        String normalizedUrl = url.trim();

        if (customAlias != null && !customAlias.isBlank()) {
            return createCustomAliasMapping(normalizedUrl, customAlias.trim());
        }

        return urlMappingRepository.findByLongUrlAndCustomAliasFalse(normalizedUrl)
                .map(mapping -> new ShortenResult(mapping, true))
                .orElseGet(() -> createGeneratedMapping(normalizedUrl));
    }

    private ShortenResult createCustomAliasMapping(String longUrl, String alias) {
        if (urlMappingRepository.existsByShortCode(alias)) {
            throw new AliasAlreadyTakenException(alias);
        }

        UrlMapping mapping = new UrlMapping(alias, longUrl, true);
        try {
            UrlMapping saved = urlMappingRepository.save(mapping);
            return new ShortenResult(saved, false);
        } catch (DuplicateKeyException ex) {
            throw new AliasAlreadyTakenException(alias);
        }
    }

    private ShortenResult createGeneratedMapping(String longUrl) {
        String shortCode = sequenceGeneratorService.nextShortCode();
        UrlMapping mapping = new UrlMapping(shortCode, longUrl, false);
        UrlMapping saved = urlMappingRepository.save(mapping);
        return new ShortenResult(saved, false);
    }

    @Override
    public String resolve(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        Query query = new Query(Criteria.where("_id").is(mapping.getId()));
        Update update = new Update().inc("clickCount", 1);
        mongoOperations.updateFirst(query, update, UrlMapping.class);

        return mapping.getLongUrl();
    }
}
