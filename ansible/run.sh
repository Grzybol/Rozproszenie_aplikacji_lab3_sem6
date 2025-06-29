#!/bin/bash

# 🚀 Ansible Runner dla URL Shortener
# Skrypt do łatwego uruchamiania operacji Ansible

set -e

# Kolory dla output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funkcje pomocnicze
print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  🚀 URL Shortener Ansible${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Sprawdzenie czy jesteśmy w katalogu ansible
if [ ! -f "ansible.cfg" ]; then
    print_error "Uruchom ten skrypt z katalogu ansible/"
    exit 1
fi

# Sprawdzenie czy Ansible jest zainstalowany
if ! command -v ansible &> /dev/null; then
    print_error "Ansible nie jest zainstalowany!"
    print_info "Zainstaluj: sudo apt install ansible"
    exit 1
fi

# Funkcja wyświetlająca menu
show_menu() {
    print_header
    echo
    echo "Wybierz operację:"
    echo "1) 🚀 Deploy aplikacji (dev)"
    echo "2) 🚀 Deploy aplikacji (staging)"
    echo "3) 🚀 Deploy aplikacji (production)"
    echo "4) 🔍 Health check"
    echo "5) 💾 Backup aplikacji"
    echo "6) 🔄 Restart aplikacji"
    echo "7) 📊 Status systemu"
    echo "8) 🧹 Cleanup starych backupów"
    echo "9) 📝 Pokaż logi"
    echo "0) ❌ Wyjście"
    echo
    read -p "Twój wybór (0-9): " choice
}

# Funkcja deploy
deploy_app() {
    local env=$1
    print_info "Rozpoczynam deployment na środowisko: $env"
    
    case $env in
        "dev")
            ansible-playbook playbooks/deploy.yml -i inventory/hosts.yml -l dev
            ;;
        "staging")
            ansible-playbook playbooks/deploy.yml -i inventory/hosts.yml -l staging
            ;;
        "prod")
            print_warning "Deployment na PRODUCTION! Czy na pewno? (y/N)"
            read -p "" confirm
            if [[ $confirm =~ ^[Yy]$ ]]; then
                ansible-playbook playbooks/deploy.yml -i inventory/hosts.yml -l prod
            else
                print_info "Deployment anulowany"
            fi
            ;;
    esac
}

# Funkcja health check
health_check() {
    print_info "Sprawdzam stan aplikacji..."
    ansible-playbook playbooks/health-check.yml -i inventory/hosts.yml -l dev
}

# Funkcja backup
backup_app() {
    print_info "Tworzę backup aplikacji..."
    ansible-playbook playbooks/backup.yml -i inventory/hosts.yml -l dev
}

# Funkcja restart
restart_app() {
    print_info "Restartuję aplikację..."
    ansible -i inventory/hosts.yml -l dev -m docker_compose -a "project_src=/opt/url-shortener restarted=yes"
    print_success "Aplikacja zrestartowana"
}

# Funkcja status
show_status() {
    print_info "Status systemu:"
    ansible -i inventory/hosts.yml -l dev -m shell -a "docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'"
}

# Funkcja cleanup
cleanup_backups() {
    print_info "Usuwam stare backupy..."
    ansible -i inventory/hosts.yml -l dev -m shell -a "find /opt/url-shortener/backups -name '*.tar.gz' -mtime +7 -delete"
    print_success "Stare backupy usunięte"
}

# Funkcja logi
show_logs() {
    echo "Wybierz logi do wyświetlenia:"
    echo "1) Health check logs"
    echo "2) Backup logs"
    echo "3) Docker logs (url-shortener)"
    echo "4) Docker logs (cassandra)"
    read -p "Twój wybór (1-4): " log_choice
    
    case $log_choice in
        1)
            ansible -i inventory/hosts.yml -l dev -m shell -a "tail -n 50 /opt/url-shortener/logs/health-check.log"
            ;;
        2)
            ansible -i inventory/hosts.yml -l dev -m shell -a "tail -n 50 /opt/url-shortener/logs/backup.log"
            ;;
        3)
            ansible -i inventory/hosts.yml -l dev -m shell -a "docker logs --tail 50 url-shortener"
            ;;
        4)
            ansible -i inventory/hosts.yml -l dev -m shell -a "docker logs --tail 50 cass1"
            ;;
        *)
            print_error "Nieprawidłowy wybór"
            ;;
    esac
}

# Główna pętla
while true; do
    show_menu
    
    case $choice in
        1)
            deploy_app "dev"
            ;;
        2)
            deploy_app "staging"
            ;;
        3)
            deploy_app "prod"
            ;;
        4)
            health_check
            ;;
        5)
            backup_app
            ;;
        6)
            restart_app
            ;;
        7)
            show_status
            ;;
        8)
            cleanup_backups
            ;;
        9)
            show_logs
            ;;
        0)
            print_info "Do widzenia! 👋"
            exit 0
            ;;
        *)
            print_error "Nieprawidłowy wybór. Spróbuj ponownie."
            ;;
    esac
    
    echo
    read -p "Naciśnij Enter aby kontynuować..."
    echo
done 