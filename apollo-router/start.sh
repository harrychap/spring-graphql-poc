#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# GraphOS credentials — required for federated subscriptions (Developer plan or above).
# Replace these placeholder values with your own before running.
# See the README for instructions on obtaining them.
export APOLLO_KEY="${APOLLO_KEY:-service:your-graph-name:xxxxxxxxxxxxxxxxxxxxxxxxxxxx}"
export APOLLO_GRAPH_REF="${APOLLO_GRAPH_REF:-your-graph-name@current}"

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
