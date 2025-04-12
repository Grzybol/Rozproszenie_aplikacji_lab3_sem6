package org.bestservers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.UUID;

public class URLStorageService {

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


    public void storeUrl(String shortUrl, String originalUrl, int ttl) {
        System.out.println("Storing URL: " + shortUrl + " → " + originalUrl);
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();

        String query = "INSERT INTO links (id, short_code, original_url, created_at) " +
                "VALUES (?, ?, ?, ?)";

        session.execute(
                SimpleStatement.newInstance(query, id, shortUrl, originalUrl, createdAt)
        );

        System.out.println("✅ Inserted: " + shortUrl + " → " + originalUrl);
    }

}
