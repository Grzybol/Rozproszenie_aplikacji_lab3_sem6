# 🔗 Rozproszone Aplikacje - URL Shortener

Projekt stworzony na potrzeby zajęć z **Aplikacji Rozproszonych** – prosty system do skracania i przekierowywania URL-i przy użyciu **Spring Boot + Cassandra + Docker + Ansible**.

## 🧱 Architektura

- `url-shortener` – mikroserwis do generowania skróconych linków i zapisywania ich w Cassandra
- `url-redirector` – mikroserwis do przekierowywania użytkownika na oryginalny link
- `url-cleaner` – serwis do czyszczenia starych wpisów
- `Cassandra` – baza danych NoSQL (w klastrze)
- `Kafka` – system messaging dla alertów
- `Ansible` – automatyzacja deploymentu i zarządzania

## 🚀 Automatyzacja z Ansible

Projekt zawiera kompleksowy system automatyzacji z Ansible:

### 📁 Struktura Ansible
```
ansible/
├── ansible.cfg                 # Konfiguracja Ansible
├── inventory/
│   ├── hosts.yml              # Definicje serwerów
│   └── group_vars/            # Zmienne środowiskowe
├── roles/                     # Role Ansible
│   ├── common/                # Podstawowa konfiguracja
│   ├── docker/                # Instalacja Docker
│   ├── java/                  # Instalacja Java
│   └── url-shortener/         # Deployment aplikacji
├── playbooks/                 # Playbooki
│   ├── deploy.yml             # Główny deployment
│   ├── health-check.yml       # Sprawdzanie stanu
│   └── backup.yml             # Backup aplikacji
├── run.sh                     # Skrypt do łatwego uruchamiania
└── README.md                  # Dokumentacja Ansible
```

### 🎯 Funkcjonalności Ansible
- ✅ **Automatyzacja deploymentu** - jednoklikowy deployment na różne środowiska
- 🔧 **Zarządzanie infrastrukturą** - setup Docker, Java, Cassandra, Kafka
- 📊 **Monitoring i maintenance** - health checks, log rotation, cleanup
- 🔄 **Backup i recovery** - automatyczne backupy z retention policy

### 🚀 Szybki start z Ansible

```bash
# Przejdź do katalogu ansible
cd ansible

# Uruchom interaktywny skrypt
./run.sh

# Lub uruchom bezpośrednio
ansible-playbook playbooks/deploy.yml -i inventory/hosts.yml -l dev
```

**📖 Pełna dokumentacja Ansible:** [ansible/README.md](ansible/README.md)

## ▶️ Jak uruchomić (tradycyjnie)

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

⚠️ Uwaga
TTL (czas życia linku) domyślnie to 60 sekund.
Linki zapisywane są w bazie Cassandra (musisz poczekać, aż klaster w pełni się zsynchronizuje).

📂 Struktura katalogów
```
.
├── docker-compose.yml
├── url-shortener/     # serwis do generowania linków
├── url-redirector/     # serwis do redirectu
├── url-cleaner/        # serwis do czyszczenia
├── etc/               # konfiguracja Cassandry
└── ansible/           # automatyzacja deploymentu
```

## 🔧 Środowiska

### **Development**
- 1 węzeł Cassandra
- 1 broker Kafka
- Debug mode włączony
- Backup co 3 dni

### **Production**
- 3 węzły Cassandra
- 3 brokery Kafka
- Pełny monitoring
- Backup codziennie
- SSL i firewall

## 📊 Monitoring

### **Health Checks**
- Automatyczne co 5 minut
- Sprawdza wszystkie serwisy
- Logi w `/opt/url-shortener/logs/health-check.log`

### **Backup**
- Automatyczne codziennie
- Backup Cassandra snapshots
- Compressed archives
- Retention policy

## 🚀 Rozszerzenia

### **Możliwe dodatki:**
- Prometheus + Grafana monitoring
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Kubernetes deployment
- CI/CD pipeline integration
- Load balancer configuration
- Auto-scaling rules

---

**🎉 Projekt zawiera zarówno tradycyjny Docker Compose setup jak i zaawansowaną automatyzację z Ansible!**
