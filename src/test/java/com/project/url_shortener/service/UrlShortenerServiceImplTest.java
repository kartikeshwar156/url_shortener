package com.project.url_shortener.service;

import com.project.url_shortener.exception.AliasAlreadyTakenException;
import com.project.url_shortener.exception.InvalidUrlException;
import com.project.url_shortener.exception.ShortCodeNotFoundException;
import com.project.url_shortener.model.UrlMapping;
import com.project.url_shortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceImplTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private UrlShortenerServiceImpl urlShortenerService;

    private UrlMapping existingMapping;

    @BeforeEach
    void setUp() {
        existingMapping = new UrlMapping("abc123", "https://example.com", false);
        existingMapping.setId("mapping-id");
    }

    @Test
    void shortenGeneratesNewCodeForNewUrl() {
        when(urlMappingRepository.findByLongUrlAndCustomAliasFalse("https://example.com"))
                .thenReturn(Optional.empty());
        when(sequenceGeneratorService.nextShortCode()).thenReturn("000001");
        when(urlMappingRepository.save(any(UrlMapping.class))).thenAnswer(invocation -> {
            UrlMapping mapping = invocation.getArgument(0);
            mapping.setId("new-id");
            return mapping;
        });

        ShortenResult result = urlShortenerService.shorten("https://example.com", null);

        assertFalse(result.reused());
        assertEquals("000001", result.mapping().getShortCode());
        verify(sequenceGeneratorService).nextShortCode();
    }

    @Test
    void shortenReusesExistingNonAliasMapping() {
        when(urlMappingRepository.findByLongUrlAndCustomAliasFalse("https://example.com"))
                .thenReturn(Optional.of(existingMapping));

        ShortenResult result = urlShortenerService.shorten("https://example.com", null);

        assertTrue(result.reused());
        assertEquals("abc123", result.mapping().getShortCode());
        verify(sequenceGeneratorService, never()).nextShortCode();
        verify(urlMappingRepository, never()).save(any());
    }

    @Test
    void shortenCreatesCustomAlias() {
        when(urlMappingRepository.existsByShortCode("my-link")).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenAnswer(invocation -> {
            UrlMapping mapping = invocation.getArgument(0);
            mapping.setId("alias-id");
            return mapping;
        });

        ShortenResult result = urlShortenerService.shorten("https://example.com", "my-link");

        assertFalse(result.reused());
        assertEquals("my-link", result.mapping().getShortCode());
        assertTrue(result.mapping().isCustomAlias());
    }

    @Test
    void shortenRejectsTakenCustomAlias() {
        when(urlMappingRepository.existsByShortCode("taken")).thenReturn(true);

        assertThrows(AliasAlreadyTakenException.class,
                () -> urlShortenerService.shorten("https://example.com", "taken"));
    }

    @Test
    void shortenHandlesDuplicateKeyRaceForCustomAlias() {
        when(urlMappingRepository.existsByShortCode("race")).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenThrow(new DuplicateKeyException("duplicate"));

        assertThrows(AliasAlreadyTakenException.class,
                () -> urlShortenerService.shorten("https://example.com", "race"));
    }

    @Test
    void shortenRejectsInvalidUrl() {
        assertThrows(InvalidUrlException.class,
                () -> urlShortenerService.shorten("not-a-url", null));
    }

    @Test
    void resolveReturnsLongUrlAndIncrementsClickCount() {
        when(urlMappingRepository.findByShortCode("abc123")).thenReturn(Optional.of(existingMapping));

        String longUrl = urlShortenerService.resolve("abc123");

        assertEquals("https://example.com", longUrl);
        verify(mongoOperations).updateFirst(any(), any(), eq(UrlMapping.class));
    }

    @Test
    void resolveThrowsWhenCodeNotFound() {
        when(urlMappingRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThrows(ShortCodeNotFoundException.class, () -> urlShortenerService.resolve("missing"));
    }
}
