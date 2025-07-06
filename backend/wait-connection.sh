#!/usr/bin/env bash
# wait-for-it.sh - Espera um host:porta ficar disponível para conexão TCP.

set -e

host="$1"
port="$2"
shift 2
cmd="$@"

until nc -z "$host" "$port"; do
  echo "Waiting for $host:$port to be available..."
  sleep 2
done

echo "$host:$port is available, executing command..."
exec $cmd