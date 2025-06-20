# 🔗 Rozproszone Aplikacje - URL Shortener

Projekt stworzony na potrzeby zajęć z **Aplikacji Rozproszonych** – prosty system do skracania i przekierowywania URL-i przy użyciu **Spring Boot + Cassandra + Docker**.

## 🧱 Architektura

- `url-shortener` – mikroserwis do generowania skróconych linków i zapisywania ich w Cassandra
- `url-redirector` – mikroserwis do przekierowywania użytkownika na oryginalny link
- `Cassandra` – baza danych NoSQL (w klastrze)

## ▶️ Jak uruchomić

1. Sklonuj repo:
```
git clone https://github.com/Grzybol/Rozproszenie_aplikacji_lab3_sem6.git
cd Rozproszenie_aplikacji_lab3_sem6
```

Zbuduj obraz aplikacji (jeśli nie został zbudowany):
```
docker-compose build
```

Uruchom wszystko:
```
docker-compose up
```

Poczekaj, aż Cassandra wystartuje i aplikacje się połączą (może to chwilę potrwać – logi pomogą).

🌐 API
Skracanie linku - parametr ttl jest opcjonalny:
```
GET
http://localhost:8080/shorten?url=https://example.com&ttl=60
```
Zwraca skrócony link, np. 
```http://localhost:8080/abc123```

Przekierowanie:
Wejdź w ```http://localhost:8080/abc123```, a zostaniesz przekierowany na oryginalny link (jeśli nie wygasł).

Jak to sprawdzić?
Możesz bardzo prosto to przetestować — np. odpalić kafka-console-consumer, żeby ręcznie zobaczyć, czy wiadomości są w topicu:

bash
```
docker exec -it kafka /bin/bash
```
potem w środku kontenera:
```
kafka-console-consumer --bootstrap-server kafka:9092 --topic url-blacklist-alerts --from-beginning
```
⚠️ Uwaga
TTL (czas życia linku) domyślnie to 60 sekund.
Linki zapisywane są w bazie Cassandra (musisz poczekać, aż klaster w pełni się zsynchronizuje).

📂 Struktura katalogów
```
.
├── docker-compose.yml
├── url-shortener/     # serwis do generowania linków
├── url-redirector/     # serwis do redirectu
└── etc/               # konfiguracja Cassandry
```
