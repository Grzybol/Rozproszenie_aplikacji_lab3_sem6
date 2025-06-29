# 🚀 Ansible Automation dla URL Shortener

Kompleksowy system automatyzacji deploymentu i zarządzania aplikacją URL Shortener przy użyciu Ansible.

## 📁 Struktura projektu

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
└── README.md                  # Ta dokumentacja
```

## 🎯 Funkcjonalności

### ✅ **Automatyzacja deploymentu**
- Jednoklikowy deployment na różne środowiska
- Automatyczna instalacja zależności (Docker, Java)
- Konfiguracja systemu i użytkowników
- Health checks i monitoring

### 🔧 **Zarządzanie infrastrukturą**
- Setup klastra Cassandra
- Konfiguracja Kafka/Zookeeper
- Docker networking
- System limits i optymalizacje

### 📊 **Monitoring i maintenance**
- Automatyczne health checks co 5 minut
- Backup Cassandra i konfiguracji
- Log rotation i cleanup
- System resource monitoring

### 🔄 **Backup i recovery**
- Automatyczne backupy codziennie
- Retention policy (7-30 dni)
- Backup Cassandra snapshots
- Compressed archives

## 🚀 Szybki start

### 1. **Przygotowanie środowiska**

```bash
# Instalacja Ansible (Ubuntu/Debian)
sudo apt update
sudo apt install ansible

# Sprawdzenie wersji
ansible --version
```

### 2. **Konfiguracja inventory**

Edytuj `inventory/hosts.yml` i dostosuj adresy IP serwerów:

```yaml
dev:
  hosts:
    dev-server:
      ansible_host: localhost  # Dla lokalnego testowania
      ansible_connection: local
```

### 3. **Deployment na środowisko dev**

```bash
# Przejdź do katalogu ansible
cd ansible

# Uruchom deployment
ansible-playbook playbooks/deploy.yml -i inventory/hosts.yml -l dev
```

### 4. **Sprawdzenie stanu aplikacji**

```bash
# Health check
ansible-playbook playbooks/health-check.yml -i inventory/hosts.yml -l dev

# Backup
ansible-playbook playbooks/backup.yml -i inventory/hosts.yml -l dev
```

## 🌍 Środowiska

### **Development (dev)**
- 1 węzeł Cassandra
- 1 broker Kafka
- 1 instancja aplikacji
- Debug mode włączony
- Backup co 3 dni

### **Staging**
- 2 węzły Cassandra
- 2 brokery Kafka
- 1 instancja aplikacji
- Testowanie przed prod

### **Production (prod)**
- 3 węzły Cassandra
- 3 brokery Kafka
- 2 instancje aplikacji
- Pełny monitoring
- Backup codziennie
- SSL i firewall

## 📋 Komendy

### **Deployment**
```bash
# Dev
ansible-playbook playbooks/deploy.yml -i inventory/hosts.yml -l dev

# Staging
ansible-playbook playbooks/deploy.yml -i inventory/hosts.yml -l staging

# Production
ansible-playbook playbooks/deploy.yml -i inventory/hosts.yml -l prod
```

### **Monitoring**
```bash
# Health check
ansible-playbook playbooks/health-check.yml -i inventory/hosts.yml -l dev

# Backup
ansible-playbook playbooks/backup.yml -i inventory/hosts.yml -l dev

# Sprawdzenie logów
ansible -i inventory/hosts.yml -l dev -m shell -a "tail -f /opt/url-shortener/logs/health-check.log"
```

### **Maintenance**
```bash
# Restart aplikacji
ansible -i inventory/hosts.yml -l dev -m docker_compose -a "project_src=/opt/url-shortener restarted=yes"

# Update aplikacji
ansible-playbook playbooks/deploy.yml -i inventory/hosts.yml -l dev --tags update

# Cleanup starych backupów
ansible -i inventory/hosts.yml -l dev -m shell -a "find /opt/url-shortener/backups -name '*.tar.gz' -mtime +7 -delete"
```

## 🔧 Konfiguracja

### **Zmienne środowiskowe**
Edytuj pliki w `inventory/group_vars/`:

- `all.yml` - zmienne globalne
- `dev.yml` - zmienne dla dev
- `prod.yml` - zmienne dla production

### **Porty aplikacji**
```yaml
url_shortener_port: 8080
url_redirector_port: 8081
cassandra_port: 9042
kafka_port: 9092
```

### **Backup settings**
```yaml
backup_retention_days: 7  # Dla dev
backup_schedule: "0 2 * * *"  # Codziennie o 2:00
```

## 🐛 Troubleshooting

### **Problem: Docker nie startuje**
```bash
# Sprawdź status Docker
sudo systemctl status docker

# Restart Docker
sudo systemctl restart docker

# Sprawdź logi
sudo journalctl -u docker -f
```

### **Problem: Cassandra nie łączy się**
```bash
# Sprawdź status kontenerów
docker ps | grep cassandra

# Sprawdź logi Cassandra
docker logs cass1

# Sprawdź połączenie
docker exec cass1 cqlsh -e "DESCRIBE KEYSPACES;"
```

### **Problem: Aplikacja nie odpowiada**
```bash
# Sprawdź health endpoint
curl http://localhost:8080/actuator/health

# Sprawdź logi aplikacji
docker logs url-shortener

# Sprawdź zasoby systemu
htop
```

## 📈 Monitoring

### **Health Checks**
- Automatyczne co 5 minut
- Sprawdza wszystkie serwisy
- Logi w `/opt/url-shortener/logs/health-check.log`

### **Backup**
- Automatyczne codziennie
- Backup Cassandra snapshots
- Compressed archives
- Retention policy

### **Logs**
- Docker logs z rotation
- Application logs
- Health check logs
- Backup logs

## 🔐 Security

### **Firewall (Production)**
- Porty 22 (SSH), 80, 443, 8080-8082
- Cassandra porty tylko lokalnie
- Kafka porty tylko lokalnie

### **SSL (Production)**
- Certyfikaty SSL dla aplikacji
- HTTPS endpoints
- Secure communication

### **Users**
- Dedykowany użytkownik aplikacji
- SSH key authentication
- Sudo access tylko dla maintenance

## 🚀 Rozszerzenia

### **Możliwe dodatki:**
- Prometheus + Grafana monitoring
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Kubernetes deployment
- CI/CD pipeline integration
- Load balancer configuration
- Auto-scaling rules

---

**🎉 Gotowe!** Twój system URL Shortener jest teraz w pełni zautomatyzowany z Ansible! 