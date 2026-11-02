#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
if [[ ! -f dist/Guitarmony.jar ]]; then
  bash build.sh
fi
exec java -jar dist/Guitarmony.jar "$@"
