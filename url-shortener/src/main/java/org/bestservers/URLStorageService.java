package org.bestservers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.datastax.oss.driver.api.core.CqlIdentifier;


import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.UUID;

public class URLStorageService {

    private CqlSession session;


    public URLStorageService() {
        int attempts = 0;
        while (attempts < 10) {
            try {
                // Najpierw łączymy się BEZ keyspace, żeby go ewentualnie stworzyć
                CqlSession tempSession = CqlSession.builder()
                        .addContactPoint(new InetSocketAddress("cass1", 9042))
                        .withLocalDatacenter("dc1")
                        .build();

                // Sprawdzamy, czy keyspace istnieje
                boolean keyspaceExists = tempSession.getMetadata()
                        .getKeyspaces()
                        .containsKey(CqlIdentifier.fromCql("url_shortener"));

                if (!keyspaceExists) {
                    System.out.println("⚠️ Keyspace 'url_shortener' nie istnieje. Tworzymy go...");
                    String createKeyspace = "CREATE KEYSPACE IF NOT EXISTS url_shortener WITH replication = " +
                            "{'class': 'SimpleStrategy', 'replication_factor': 1};";
                    tempSession.execute(createKeyspace);
                    System.out.println("✅ Keyspace 'url_shortener' utworzony.");
                } else {
                    System.out.println("✅ Keyspace 'url_shortener' już istnieje.");
                }

                tempSession.close();

                // Teraz łączymy się już z właściwym keyspace
                this.session = CqlSession.builder()
                        .addContactPoint(new InetSocketAddress("cass1", 9042))
                        .withLocalDatacenter("dc1")
                        .withKeyspace("url_shortener")
                        .build();

                ensureTableExists(); // <- sprawdzenie i tworzenie tabeli
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

        if (this.session == null) {
            throw new RuntimeException("Couldn't connect to Cassandra after retries.");
        }
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

        String query = "INSERT INTO links (id, short_code, original_url, created_at) " +
                "VALUES (?, ?, ?, ?)";

        session.execute(
                SimpleStatement.newInstance(query, id, shortUrl, originalUrl, createdAt)
        );

        System.out.println("✅ Inserted: " + shortUrl + " → " + originalUrl);
    }

}
