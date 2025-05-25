package org.bestservers;

import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import org.bestservers.URLStorageService;
import java.sql.*;
import java.time.Instant;

@RestController
public class URLShortenerController {

    private final URLShortenerService urlShortenerService = new URLShortenerService();
    //private final URLStorageService urlStorageService = new URLStorageService();
    private final URLStorageService urlStorageService;

    public URLShortenerController(URLStorageService urlStorageService) {
        this.urlStorageService = urlStorageService;
    }


    @GetMapping("/shorten")
    public String shortenURL(@RequestParam String url, @RequestParam(required = false, defaultValue = "600") int ttl) {
        String shortUrl = urlShortenerService.generateShortUrl();
        System.out.println("Storing URL: " + shortUrl + " → " + url);
        urlStorageService.storeUrl(shortUrl, url, ttl);
        return "http://localhost:8081/" + shortUrl;
    }

    private static class URLShortenerService {
        private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static final int SHORT_URL_LENGTH = 6;
        private final SecureRandom random = new SecureRandom();

        public String generateShortUrl() {
            StringBuilder sb = new StringBuilder(SHORT_URL_LENGTH);
            for (int i = 0; i < SHORT_URL_LENGTH; i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            return sb.toString();
        }
    }
}