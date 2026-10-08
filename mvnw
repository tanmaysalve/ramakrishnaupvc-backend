#!/usr/bin/env sh
set -eu
MVN_VERSION=3.9.11
BASE="$HOME/.m2/wrapper/dists/apache-maven-$MVN_VERSION"
MAVEN_HOME="$BASE/apache-maven-$MVN_VERSION"
if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$BASE"
  ARCHIVE="$BASE/apache-maven-$MVN_VERSION-bin.tar.gz"
  if command -v curl >/dev/null 2>&1; then curl -fsSL "https://archive.apache.org/dist/maven/maven-3/$MVN_VERSION/binaries/apache-maven-$MVN_VERSION-bin.tar.gz" -o "$ARCHIVE"; else wget -q "https://archive.apache.org/dist/maven/maven-3/$MVN_VERSION/binaries/apache-maven-$MVN_VERSION-bin.tar.gz" -O "$ARCHIVE"; fi
  tar -xzf "$ARCHIVE" -C "$BASE"
fi
exec "$MAVEN_HOME/bin/mvn" "$@"
