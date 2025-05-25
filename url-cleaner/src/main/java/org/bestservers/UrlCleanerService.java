package org.bestservers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class UrlCleanerService {

    private final CqlSession session;
    private final CleanupConfig config;

    @Autowired
    public UrlCleanerService(@Qualifier("cleanupConfig") CleanupConfig config) {
        this.config = config;

        CqlSession tempSession = null;
        int attempts = 0;
        while (attempts < 10) {
            try {
                tempSession = CqlSession.builder()
                        .addContactPoint(new InetSocketAddress("cass1", 9042))
                        .withLocalDatacenter("dc1")
                        .withKeyspace("url_shortener")
                        .build();
                System.out.println("✅ Połączono z Cassandra.");
                break;
            } catch (Exception e) {
                attempts++;
                System.out.println("❌ Cassandra not ready, retrying... (" + attempts + "/10)");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        if (tempSession == null) {
            throw new RuntimeException("💥 Nie udało się połączyć z Cassandra po 10 próbach.");
        }

        this.session = tempSession;
    }

    public void deleteAllLinks() {
        System.out.println("🔍 Lista wszystkich wpisów w tabeli `links`:");
        ResultSet all = session.execute("SELECT id, short_code, original_url, created_at FROM links;");
        int found = 0;
        for (Row row : all) {
            found++;
            System.out.println("🔸 ID: " + row.getUuid("id") +
                    " | short_code: " + row.getString("short_code") +
                    " | original_url: " + row.getString("original_url") +
                    " | created_at: " + row.getInstant("created_at"));
        }

        if (found == 0) {
            System.out.println("📭 Brak rekordów do usunięcia.");
            return;
        }

        System.out.println("💣 Usuwanie wszystkich rekordów...");
        int deleted = 0;
        ResultSet rows = session.execute("SELECT id, short_code FROM links;");
        for (Row row : rows) {
            UUID id = row.getUuid("id");
            String shortCode = row.getString("short_code");

            String deleteQuery = "DELETE FROM links WHERE id = ?;";
            session.execute(SimpleStatement.newInstance(deleteQuery, id));
            System.out.println("🗑️ Usunięto wpis: " + shortCode + " (ID: " + id + ")");
            deleted++;
        }

        System.out.println("✅ Usunięto wszystkich: " + deleted + " wpisów.");
    }

    public void cleanOldLinks() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(config.getDays()));
        System.out.println("🔍 Lista wpisów w tabeli `links`:");
        ResultSet all = session.execute("SELECT id, short_code, original_url, created_at FROM links;");
        int found = 0;
        for (Row row : all) {
            found++;
            System.out.println("🔸 ID: " + row.getUuid("id") +
                    " | short_code: " + row.getString("short_code") +
                    " | original_url: " + row.getString("original_url") +
                    " | created_at: " + row.getInstant("created_at"));
        }

        if (found == 0) {
            System.out.println("📭 Brak rekordów do sprawdzenia.");
            return;
        }

        System.out.println("🧹 Usuwanie wpisów starszych niż: " + cutoff);
        ResultSet rows = session.execute("SELECT id, short_code, created_at FROM links;");
        int deleted = 0;
        for (Row row : rows) {
            Instant created = row.getInstant("created_at");
            UUID id = row.getUuid("id");
            String shortCode = row.getString("short_code");

            if (created.isBefore(cutoff)) {
                String deleteQuery = "DELETE FROM links WHERE id = ?;";
                session.execute(SimpleStatement.newInstance(deleteQuery, id));
                System.out.println("🗑️ Usunięto przeterminowany wpis: " + shortCode + " (utworzony: " + created + ")");
                deleted++;
            }
        }

        if (deleted == 0) {
            System.out.println("✔️ Brak wpisów do usunięcia.");
        } else {
            System.out.println("✅ Usunięto przeterminowanych: " + deleted + " wpisów.");
        }
    }
    @PreDestroy
    public void shutdown() {
        if (session != null && !session.isClosed()) {
            System.out.println("🛑 Zamykam sesję Cassandra...");
            session.close();
        }
    }

}
