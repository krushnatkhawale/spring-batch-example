#!/bin/sh
set -eu

DOCKER_BIN=${DOCKER_BIN:-}
if [ -z "$DOCKER_BIN" ]; then
    DOCKER_BIN=$(command -v docker || true)
fi
if [ -z "$DOCKER_BIN" ] && [ -x /Applications/Docker.app/Contents/Resources/bin/docker ]; then
    DOCKER_BIN=/Applications/Docker.app/Contents/Resources/bin/docker
fi
if [ -z "$DOCKER_BIN" ]; then
    echo "Docker CLI not found. Add Docker to PATH or set DOCKER_BIN to its executable path." >&2
    exit 1
fi

CONTAINER_NAME=spring-batch-db
POSTGRES_DB=${POSTGRES_DB:-springbatch}
POSTGRES_USER=${POSTGRES_USER:-springbatch}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-springbatch}
POSTGRES_PORT=${POSTGRES_PORT:-5432}

if "$DOCKER_BIN" container inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
    RUNNING=$("$DOCKER_BIN" inspect --format '{{.State.Running}}' "$CONTAINER_NAME")
    if [ "$RUNNING" != "true" ]; then
        "$DOCKER_BIN" start "$CONTAINER_NAME" >/dev/null
    fi
else
    "$DOCKER_BIN" run --detach \
        --name "$CONTAINER_NAME" \
        --restart unless-stopped \
        --publish "$POSTGRES_PORT:5432" \
        --volume spring-batch-db-data:/var/lib/postgresql/data \
        --env POSTGRES_DB="$POSTGRES_DB" \
        --env POSTGRES_USER="$POSTGRES_USER" \
        --env POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
        postgres:16
fi

attempt=0
until "$DOCKER_BIN" exec "$CONTAINER_NAME" pg_isready \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" >/dev/null 2>&1; do
    attempt=$((attempt + 1))
    if [ "$attempt" -ge 60 ]; then
        echo "PostgreSQL container '$CONTAINER_NAME' did not become ready." >&2
        echo "If the container already existed, check that its database and credentials match this script." >&2
        exit 1
    fi
    sleep 1
done

printf "PostgreSQL is ready: container=%s database=%s user=%s port=%s\n" \
    "$CONTAINER_NAME" "$POSTGRES_DB" "$POSTGRES_USER" "$POSTGRES_PORT"
