#!/bin/bash
# ============================================================
# mvnw.sh — Git Bash 下的 Maven wrapper
# 解决 MSYS 路径自动转换失效导致 Windows java.exe 找不到
# classworlds jar 的问题（/d/... 不会自动转为 D:\...）。
# 用法：./mvnw.sh <maven-args>   （等价于 mvn <maven-args>）
# ============================================================
set -e

# Maven 安装目录（Unix 路径，可按需覆盖）
M2_HOME_UNIX="${M2_HOME_UNIX:-/d/apache-maven-3.9.14}"
# JDK 的 java 可执行文件（Unix 路径）
JAVA_UNIX="${JAVA_UNIX:-/c/Program Files/Microsoft/jdk-21.0.11.10-hotspot/bin/java}"

# 定位 classworlds jar（自动匹配版本号）
CW_JAR_UNIX=$(ls "$M2_HOME_UNIX"/boot/plexus-classworlds-*.jar 2>/dev/null | head -1)
if [ -z "$CW_JAR_UNIX" ]; then
  echo "错误：未找到 plexus-classworlds jar，请检查 M2_HOME_UNIX=$M2_HOME_UNIX" >&2
  exit 1
fi

# 关键路径转为 Windows 格式
CW_JAR=$(cygpath -w "$CW_JAR_UNIX")
M2_CONF=$(cygpath -w "$M2_HOME_UNIX/bin/m2.conf")
M2_HOME_W=$(cygpath -w "$M2_HOME_UNIX")
JANSI=$(cygpath -w "$M2_HOME_UNIX/lib/jansi-native")
PROJECT_BASE=$(cygpath -w "$PWD")

exec "$JAVA_UNIX" \
  -classpath "$CW_JAR" \
  "-Dclassworlds.conf=$M2_CONF" \
  "-Dmaven.home=$M2_HOME_W" \
  "-Dlibrary.jansi.path=$JANSI" \
  "-Dmaven.multiModuleProjectDirectory=$PROJECT_BASE" \
  org.codehaus.plexus.classworlds.launcher.Launcher "$@"
