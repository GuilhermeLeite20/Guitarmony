#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

if ! command -v java >/dev/null 2>&1; then
  printf '%s\n' 'Instale o JDK 17 ou superior e inclua java no PATH.' >&2
  exit 1
fi
if command -v javac >/dev/null 2>&1; then
  compiler=(javac)
else
  compiler=(java -m jdk.compiler/com.sun.tools.javac.Main)
fi
if command -v jar >/dev/null 2>&1; then
  archiver=(jar)
else
  archiver=(java -m jdk.jartool/sun.tools.jar.Main)
fi

mkdir -p build dist
rm -rf -- build/classes
mkdir -p build/classes
find src/main/java -type f -name '*.java' -print | LC_ALL=C sort > build/main-sources.txt
if [[ ! -s build/main-sources.txt ]]; then
  printf '%s\n' 'Nenhum código Java encontrado em src/main/java.' >&2
  exit 1
fi
"${compiler[@]}" --release 17 -encoding UTF-8 -d build/classes @build/main-sources.txt
"${archiver[@]}" --create --file dist/Guitarmony.jar --main-class com.guitarmony.GuitarmonyApp -C build/classes .
printf '%s\n' 'Build concluído: dist/Guitarmony.jar' 'Execute: java -jar dist/Guitarmony.jar'
