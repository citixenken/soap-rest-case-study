#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
# ----------------------------------------------------------------------------

set -e

# OS specific support.
cygwin=false
darwin=false
mingw=false
msys=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true ;;
  MINGW*) mingw=true ;;
  MSYS*) msys=true ;;
esac

## Path to Maven home
MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$( cd "$(dirname "$0")" && pwd -P )"}

if [ -z "$JAVA_HOME" ]; then
  if [ -x "/usr/libexec/java_home" ]; then
    JAVA_HOME=$(/usr/libexec/java_home)
  else
    JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$(which java)")")")
  fi
fi

MVNW_PROPERTIES_FILE="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ -f "$MVNW_PROPERTIES_FILE" ]; then
  DISTRIBUTION_URL=$(grep distributionUrl "$MVNW_PROPERTIES_FILE" | cut -d= -f2-)
fi

MAVEN_USER_HOME=${MAVEN_USER_HOME:-"$HOME/.m2"}
WRAPPER_LAUNCHER_SOURCE=${WRAPPER_LAUNCHER_SOURCE:-"https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"}
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ ! -e "$WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper..."
  if command -v curl > /dev/null; then
    curl -o "$WRAPPER_JAR" "$WRAPPER_LAUNCHER_SOURCE" -s
  elif command -v wget > /dev/null; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_LAUNCHER_SOURCE"
  else
    echo "ERROR: Neither curl nor wget is available to download the Maven wrapper." >&2
    exit 1
  fi
fi

exec "$JAVA_HOME/bin/java" \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
