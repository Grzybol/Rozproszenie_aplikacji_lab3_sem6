package org.bestservers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetSocketAddress;
import java.time.Instant;

@RestController
public class URLRedirectController {

    private final URLStorageService urlStorageService = new URLStorageService();

    @GetMapping("/{shortUrl}")
    public Object redirectUrl(@PathVariable String shortUrl) {
        String originalUrl = urlStorageService.getOriginalUrl(shortUrl);
        if (originalUrl == null || originalUrl.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("URL not found or expired");
        }
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header("Location", originalUrl)
                .build();
    }

    private static class URLStorageService {
        private CqlSession session;

        public URLStorageService() {
            int attempts = 0;
            while (attempts < 10) {
                try {
                    this.session = CqlSession.builder()
                            .addContactPoint(new InetSocketAddress("cass1", 9042))
                            .withLocalDatacenter("dc1")
                            .withKeyspace("url_shortener")
                            .build();
                    break; // sukces!
                } catch (Exception e) {
                    attempts++;
                    System.out.println("Cassandra not ready, retrying... (" + attempts + "/10)");
                    try {
                        Thread.sleep(5000); // 5 sekund przerwy
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }

            if (this.session == null) {
                throw new RuntimeException("Couldn't connect to Cassandra after retries.");
            }
        }


        public String getOriginalUrl(String shortUrl) {
            String query = "SELECT original_url, created_at FROM links WHERE short_code = ?  LIMIT 1 ALLOW FILTERING";

            Row row = session.execute(
                    SimpleStatement.newInstance(query, shortUrl)
            ).one();

            if (row == null) return null;

            Instant createdAt = row.getInstant("created_at");
            if (createdAt == null) return null;

            // Zakładamy TTL = 60 sekund (domyślny z shortenera)
            if (createdAt.plusSeconds(60).isBefore(Instant.now())) {
                return null; // expired
            }

            return row.getString("original_url");
        }
    }
}
