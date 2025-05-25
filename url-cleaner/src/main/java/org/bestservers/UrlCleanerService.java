package org.bestservers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class UrlCleanerService {

    private final CqlSession session;
    private final CleanupConfig config;

    public UrlCleanerService(CleanupConfig config) {
        this.config = config;
        this.session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("cass1", 9042))
                .withLocalDatacenter("dc1")
                .withKeyspace("url_shortener")
                .build();
    }

    @PostConstruct
    public void cleanOldLinks() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(config.getDays()));
        System.out.println("🧹 Usuwanie wpisów starszych niż: " + cutoff);

        String selectQuery = "SELECT id, short_code, created_at FROM links;";
        ResultSet rows = session.execute(selectQuery);

        for (Row row : rows) {
            Instant created = row.getInstant("created_at");
            UUID id = row.getUuid("id");
            String shortCode = row.getString("short_code");

            if (created.isBefore(cutoff)) {
                String deleteQuery = "DELETE FROM links WHERE id = ?;";
                session.execute(SimpleStatement.newInstance(deleteQuery, id));
                System.out.println("🗑️ Usunięto link: " + shortCode + " (utworzony: " + created + ")");
            }
        }

        System.out.println("✅ Czyszczenie zakończone.");
    }
}
