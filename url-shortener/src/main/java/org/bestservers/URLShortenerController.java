package org.bestservers;

import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import org.bestservers.URLStorageService;
import java.sql.*;
import java.time.Instant;

@RestController
public class URLShortenerController {

    private final URLShortenerService urlShortenerService = new URLShortenerService();
    private final URLStorageService urlStorageService = new URLStorageService();

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

    /*
    private static class URLStorageService {
        private final String url = "jdbc:postgresql://postgres:5432/url_db";
        private final String user = "user";
        private final String password = "password";
        private final URLShortenerService urlShortenerService = new URLShortenerService();
        private final URLStorageService urlStorageService = new URLStorageService();

        public URLStorageService() {
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement()) {

                String sql = "CREATE TABLE IF NOT EXISTS urls (" +
                        "short_url VARCHAR(10) PRIMARY KEY," +
                        "original_url TEXT NOT NULL," +
                        "expiry_timestamp TIMESTAMP NOT NULL)";
                stmt.execute(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }



        public void storeUrl(String shortUrl, String originalUrl, int ttl) {
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                String insert = "INSERT INTO urls (short_url, original_url, expiry_timestamp) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insert)) {
                    pstmt.setString(1, shortUrl);
                    pstmt.setString(2, originalUrl);
                    pstmt.setTimestamp(3, Timestamp.from(Instant.now().plusSeconds(ttl)));
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        @GetMapping("/shorten")
        public String shortenURL(@RequestParam String url, @RequestParam(defaultValue = "60") int ttl) {
            String shortUrl = urlShortenerService.generateShortUrl();
            urlStorageService.storeUrl(shortUrl, url, ttl);
            return "http://localhost:8080/" + shortUrl;
        }
    }
    */
}