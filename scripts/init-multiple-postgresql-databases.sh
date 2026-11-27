#!/bin/bash
set -e

function create_database() {
    local database=$1
    echo "  Verificando/criando banco: $database"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        SELECT 'CREATE DATABASE "' || '$database' || '"'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Inicializando multiplos bancos de dados PostgreSQL: $POSTGRES_MULTIPLE_DATABASES"
    for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
        create_database "$db"
    done
    echo "Bancos de dados adicionais configurados com sucesso."
fi
