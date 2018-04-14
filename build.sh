#!/bin/bash
ROOTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JVM_ROOT="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which javac))))}"
JAVA_SRC_DIR="$ROOTDIR/src"
BUILD_DIR="$ROOTDIR/bin"
JAVA_CLASS_DIR="$BUILD_DIR/java"
JAVA_RES_JARS=$(JARS=("$ROOTDIR/res"/*.jar); IFS=:; echo "${JARS[*]}")

# Allow Eclipse hot-swap:
JAVA_CLASS_DIR="$ROOTDIR/../eclipse/CSGOInternals/bin"

javac -cp "$JAVA_SRC_DIR:$ROOTDIR/res/*" -d "$BUILD_DIR/java/" "$JAVA_SRC_DIR/eu/lixko/csgointernals/Main.java"
g++ -fPIC -shared -Wl,--no-as-needed,-rpath,"$JVM_ROOT/lib/server/" -I"$JVM_ROOT/include/" -I"$JVM_ROOT/include/linux/" -L"$JVM_ROOT/lib/server/" -DJAVA_CP="\"$ROOTDIR/res/:$JAVA_RES_JARS\"" -lpthread -ldl -ljvm -o "$BUILD_DIR/libjvmloader.so" "$ROOTDIR/jvm_loader.cpp"