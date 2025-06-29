package org.bestservers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class URLStorageService {

    private final CqlSession session;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String[] blacklist;

    // Aplikacja główna
    @Autowired
    public URLStorageService(
            // Wstrzykiwanie KafkaTemplate do wysyłania alertów
            KafkaTemplate<String, String> kafkaTemplate,
            // Wstrzykiwanie blacklisty z pliku konfiguracyjnego
            @Value("${url.blacklist}") String blacklistString
    ) {
        // Inicjalizacja KafkaTemplate i blacklisty
        this.kafkaTemplate = kafkaTemplate;
        // Rozdzielenie blacklisty na tablicę słów
        this.blacklist = blacklistString.split(",");
        // Próba połączenia z bazą danych Cassandra
        int attempts = 0;
        // Tworzymy sesję Cassandra
        CqlSession tempSession = null;
        // Pętla próbująca połączyć się z bazą danych
        while (attempts < 10) {
            try {
                // Najpierw łączymy się BEZ keyspace, żeby go ewentualnie stworzyć
                tempSession = CqlSession.builder()
                        .addContactPoint(new InetSocketAddress("cass1", 9042))
                        .withLocalDatacenter("dc1")
                        .build();

                // Sprawdzamy, czy keyspace istnieje
                boolean keyspaceExists = tempSession.getMetadata()
                        .getKeyspaces()
                        .containsKey(CqlIdentifier.fromCql("url_shortener"));

                // Jeśli keyspace nie istnieje, tworzymy go
                if (!keyspaceExists) {
                    System.out.println("⚠️ Keyspace 'url_shortener' nie istnieje. Tworzymy go...");
                    String createKeyspace = "CREATE KEYSPACE IF NOT EXISTS url_shortener WITH replication = " +
                            "{'class': 'SimpleStrategy', 'replication_factor': 1};";
                    tempSession.execute(createKeyspace);
                    System.out.println("✅ Keyspace 'url_shortener' utworzony.");
                } else {
                    System.out.println("✅ Keyspace 'url_shortener' już istnieje.");
                }

                // Zamykamy tymczasową sesję bez keyspace
                tempSession.close();

                // Teraz łączymy się już z właściwym keyspace
                tempSession = CqlSession.builder()
                        .addContactPoint(new InetSocketAddress("cass1", 9042))
                        .withLocalDatacenter("dc1")
                        .withKeyspace("url_shortener")
                        .build();
                System.out.println("✅ Połączono z Cassandra w keyspace 'url_shortener'.");
                break; // sukces!
            } catch (Exception e) {
                attempts++;
                System.out.println("Cassandra not ready, retrying... (" + attempts + "/10)");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        if (tempSession  == null) {
            throw new RuntimeException("Couldn't connect to Cassandra after retries.");
        }
        this.session = tempSession;
        ensureTableExists();
    }


    private void ensureTableExists() {
        String checkTableQuery = "SELECT table_name FROM system_schema.tables WHERE keyspace_name = 'url_shortener' AND table_name = 'links';";
        ResultSet resultSet = session.execute(checkTableQuery);
        boolean exists = resultSet.one() != null;

        if (!exists) {
            System.out.println("⚠️ Tabela 'links' nie istnieje. Tworzymy ją...");
            String createTableQuery = "CREATE TABLE IF NOT EXISTS links (" +
                    "id UUID PRIMARY KEY, " +
                    "short_code text, " +
                    "original_url text, " +
                    "created_at timestamp" +
                    ");";
            session.execute(createTableQuery);
            System.out.println("✅ Tabela 'links' została utworzona.");
        } else {
            System.out.println("✅ Tabela 'links' już istnieje.");
        }
    }


    public void storeUrl(String shortUrl, String originalUrl, int ttl) {
        System.out.println("Storing URL: " + shortUrl + " → " + originalUrl);
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();

        // 🔍 Blacklist check
        for (String word : blacklist) {
            if (originalUrl.toLowerCase().contains(word.trim().toLowerCase())) {
                String msg = String.format("{\"timestamp\":\"%s\",\"url\":\"%s\",\"matched\":\"%s\"}",
                        Instant.now(), originalUrl, word.trim());
                try {
                    kafkaTemplate.send("url-blacklist-alerts", msg);
                    System.out.println("🚨 Wysłano alert na Kafkę: " + msg);
                } catch (Exception e) {
                    System.out.println("Kafka failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        String query = "INSERT INTO links (id, short_code, original_url, created_at) " +
                "VALUES (?, ?, ?, ?)";

        session.execute(
                SimpleStatement.newInstance(query, id, shortUrl, originalUrl, createdAt)
        );
        System.out.println("✅ Inserted: " + shortUrl + " → " + originalUrl);
    }
    @PreDestroy
    public void shutdown() {
        if (session != null && !session.isClosed()) {
            System.out.println("🛑 Zamykam sesję Cassandra...");
            session.close();
        }
    }

}
