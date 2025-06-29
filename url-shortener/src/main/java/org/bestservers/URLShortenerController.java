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
        // Generuje krótki URL składający się z 6 znaków
        private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        // Length krótkiego URL
        private static final int SHORT_URL_LENGTH = 6;
        // SecureRandom do generowania losowych znaków
        private final SecureRandom random = new SecureRandom();

        public String generateShortUrl() {
            // Generuje losowy krótki URL
            StringBuilder sb = new StringBuilder(SHORT_URL_LENGTH);
            // używamy SecureRandom dla lepszej losowości
            for (int i = 0; i < SHORT_URL_LENGTH; i++) {
                // dodajemy losowy znak z alfabetu
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            // zwracamy wygenerowany krótki URL
            return sb.toString();
        }
    }
}