#!/bin/sh
set -eu
# Caller holds the resource guard and bounded Maven heap; no release/publishing profile.
mvn -q -o -DskipTests -Dmaven.javadoc.skip=true package
sdk_temp=$(mktemp -d "${TMPDIR:-/tmp}/swfte-java-packed.XXXXXX")
trap 'rm -rf "$sdk_temp"' EXIT HUP INT TERM
cp target/swfte-sdk-1.1.1.jar "$sdk_temp/swfte-sdk.jar"
sdk_deps="$HOME/.m2/repository/com/fasterxml/jackson/core"
sdk_cp="$sdk_temp/swfte-sdk.jar:$sdk_deps/jackson-databind/2.15.3/jackson-databind-2.15.3.jar:$sdk_deps/jackson-core/2.15.3/jackson-core-2.15.3.jar:$sdk_deps/jackson-annotations/2.15.3/jackson-annotations-2.15.3.jar"
"$JAVA_HOME/bin/javac" -cp "$sdk_cp" -d "$sdk_temp" scripts/CallsitePackedConsumer.java
"$JAVA_HOME/bin/java" -Xms256m -Xmx512m -cp "$sdk_temp:$sdk_cp" CallsitePackedConsumer "$sdk_temp/swfte-sdk.jar"
