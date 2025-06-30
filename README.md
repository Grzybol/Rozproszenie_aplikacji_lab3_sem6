# 🔗 Rozproszone Aplikacje - URL Shortener

Projekt stworzony na potrzeby zajęć z **Aplikacji Rozproszonych** oraz **Automatyzacja procesów w inżynierii oprogramowania** – system do skracania, przekierowywania i czyszczenia URL-i przy użyciu **Spring Boot + Cassandra + Docker**.

## 🧱 Architektura

- `url-shortener` – mikroserwis do generowania skróconych linków, zapisywania ich w Cassandra oraz wysyłania alertów do Kafki, jeśli URL zawiera zakazane słowa
- `url-redirector` – mikroserwis do przekierowywania użytkownika na oryginalny link na podstawie skrótu
- `url-cleaner` – mikroserwis do czyszczenia bazy z przeterminowanych lub wszystkich linków (ręcznie lub automatycznie)
- `Cassandra` – baza danych NoSQL (klaster 3-węzłowy)
- `Kafka` – system kolejkowania do alertów o zakazanych URL-ach

## ▶️ Jak uruchomić

1. Sklonuj repo:
```
git clone https://github.com/Grzybol/Rozproszenie_aplikacji_lab3_sem6.git
cd Rozproszenie_aplikacji_lab3_sem6
```

Zbuduj obrazy aplikacji (jeśli nie zostały zbudowane):
```
docker-compose build
```

Uruchom wszystko:
```
docker-compose up
```

Poczekaj, aż Cassandra wystartuje i aplikacje się połączą (może to chwilę potrwać – logi pomogą).

## 🌐 API

### url-shortener (port 8080)
- Skracanie linku (parametr `ttl` w sekundach, opcjonalny, domyślnie 60):
```
GET http://localhost:8080/shorten?url=https://example.com&ttl=60
```
Zwraca skrócony link, np. `http://localhost:8080/abc123`

### url-redirector (port 8081)
- Przekierowanie:
```
GET http://localhost:8081/{shortCode}
```
Przekierowuje na oryginalny link (jeśli nie wygasł).

### url-cleaner (port 8082)
- Usuwanie wszystkich linków:
```
POST http://localhost:8082/internal/delete-all
```
- Usuwanie przeterminowanych linków (starszych niż X dni, domyślnie 365):
```
POST http://localhost:8082/internal/clean-expired
```

## ⚙️ Działanie i konfiguracja
- Linki są zapisywane w bazie Cassandra w tabeli `links`.
- Domyślny TTL (czas życia linku) to 60 sekund, można go zmienić parametrem `ttl` przy skracaniu.
- url-cleaner domyślnie czyści linki starsze niż 365 dni (`cleanup.ttl.days` w application.properties).
- Jeśli skracany URL zawiera zakazane słowo (lista w `url-shortener/src/main/resources/application.properties`), wysyłany jest alert do Kafki (`url-blacklist-alerts`).

### Testowanie alertów Kafka
Możesz sprawdzić alerty, uruchamiając konsumenta:
```
docker exec -it kafka /bin/bash
kafka-console-consumer --bootstrap-server kafka:9092 --topic url-blacklist-alerts --from-beginning
```

## 📂 Struktura katalogów
```
.
├── docker-compose.yml
├── url-shortener/     # serwis do generowania linków i alertów Kafka
├── url-redirector/    # serwis do redirectu
├── url-cleaner/       # serwis do czyszczenia bazy Cassandra
└── etc/               # konfiguracja Cassandry (3 węzły)
```

---

Każdy serwis posiada własny plik `Dockerfile` i konfigurację. Szczegóły endpointów znajdziesz w kodzie źródłowym poszczególnych mikroserwisów.

## Dokumentacja

- [Zobacz opis CI/CD](ci-cd-opis.md)
