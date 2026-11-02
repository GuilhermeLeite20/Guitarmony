#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
bash build.sh
if command -v javac >/dev/null 2>&1; then
  compiler=(javac)
else
  compiler=(java -m jdk.compiler/com.sun.tools.javac.Main)
fi
rm -rf -- build/test-classes
mkdir -p build/test-classes
find src/test/java -type f -name '*.java' -print | LC_ALL=C sort > build/test-sources.txt
if [[ ! -s build/test-sources.txt ]]; then
  printf '%s\n' 'Nenhum teste encontrado; a validação não foi executada.' >&2
  exit 1
fi
"${compiler[@]}" --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes @build/test-sources.txt
java -Djava.awt.headless=true -cp build/classes:build/test-classes com.guitarmony.TheoryChecks
java -Djava.awt.headless=true -cp build/classes:build/test-classes com.guitarmony.UiSmokeTest
