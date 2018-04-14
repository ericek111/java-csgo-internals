#!/bin/bash
ROOTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JVM_ROOT="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which javac))))}"
JAVA_SRC_DIR="$ROOTDIR/src"
BUILD_DIR="$ROOTDIR/bin"
JAVA_RES_JARS=$(JARS=("$ROOTDIR/res"/*.jar); IFS=:; echo "${JARS[*]}")

javac -cp "$JAVA_SRC_DIR:$ROOTDIR/res/*" -d "$BUILD_DIR/java/" "$JAVA_SRC_DIR/eu/lixko/csgointernals/Main.java" -Xlint:deprecation
gcc -fPIC -I"$JVM_ROOT/include" -I"$JVM_ROOT/include/linux" -DJAVA_CP="\"$ROOTDIR/res/:$JAVA_RES_JARS\"" -shared -o "$BUILD_DIR/libjvmloader.so" "$ROOTDIR/jvm_loader.cpp"