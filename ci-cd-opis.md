## CI/CD i Wdrożenie

Projekt posiada kompletny system CI/CD oparty na GitHub Actions. Każdy mikroserwis (`url-shortener`, `url-redirector`, `url-cleaner`) ma dedykowany pipeline zapewniający automatyczne budowanie, testowanie oraz publikację kontenerów.

### Główne funkcjonalności

- Budowanie mikroserwisów przy użyciu Mavena
- Tworzenie obrazów Docker dla każdego serwisu
- Publikacja obrazów do **GitHub Container Registry (GHCR)**
- Automatyczne ustawienie widoczności obrazów jako **publiczne**
- Wykonywanie automatycznych **testów integracyjnych z Cassandra** (dla `url-shortener`)

### Pliki workflow

Pliki GitHub Actions znajdują się w katalogu `.github/workflows/`:

```
.github/workflows/
├── ci-url-shortener.yml       # Budowanie, testowanie i publikacja url-shortener
├── ci-url-redirector.yml      # Budowanie, testowanie i publikacja url-redirector
├── ci-url-cleaner.yml         # Budowanie, testowanie i publikacja url-cleaner
└── integration-test.yml       # Testy integracyjne z Cassandra dla url-shortener
```

### Publikacja obrazów

Obrazy Docker są publikowane do **GitHub Container Registry (GHCR)** pod następującymi adresami:

- `ghcr.io/<nazwa-użytkownika-GitHub>/url-shortener`
- `ghcr.io/<nazwa-użytkownika-GitHub>/url-redirector`
- `ghcr.io/<nazwa-użytkownika-GitHub>/url-cleaner`

Obrazy są automatycznie aktualizowane przy każdym wypchnięciu kodu do gałęzi `main` i są dostępne publicznie po zakończeniu działania workflow.