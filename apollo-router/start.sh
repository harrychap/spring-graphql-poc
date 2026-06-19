#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Compose the supergraph schema from subgraph SDL files
echo "Composing supergraph..."
APOLLO_ELV2_LICENSE=accept rover supergraph compose --config supergraph.yaml > supergraph.graphql
echo "Supergraph written to supergraph.graphql"

# Download Apollo Router binary if not present
if [ ! -f ./router ]; then
  echo "Downloading Apollo Router..."
  curl -sSL https://router.apollo.dev/download/nix/latest | sh
fi

echo "Starting Apollo Router on http://localhost:4000"
./router --supergraph supergraph.graphql --config router.yaml
