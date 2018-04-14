#!/bin/bash
ROOTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JVM_ROOT="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which javac))))}"
JAVA_SRC_DIR="$ROOTDIR/src"
BUILD_DIR="$ROOTDIR/bin"
JAVA_RES_JARS=$(JARS=("$ROOTDIR/res"/*.jar); IFS=:; echo "${JARS[*]}")

# Allow Eclipse hot-swap:
BUILD_DIR="$ROOTDIR/../eclipse/CSGOInternals/bin"

javac -cp "$JAVA_SRC_DIR:$ROOTDIR/res/*" -d "$BUILD_DIR/java/" "$JAVA_SRC_DIR/eu/lixko/csgointernals/Main.java" -Xlint:deprecation

# link directly to file - CS:GO doesn't know where to look for libjvm.so without /etc/ld.so.conf.d/ entry or LD_LIBRARY_PATH
# if you're okay with that, append -L"$JVM_ROOT/lib/server/" and -ljvm to below command, since it's a better practice anyway
gcc -fPIC -I"$JVM_ROOT/include/" -I"$JVM_ROOT/include/linux/" -DJAVA_CP="\"$ROOTDIR/res/:$JAVA_RES_JARS\"" -shared -o "$BUILD_DIR/libjvmloader.so" "$ROOTDIR/jvm_loader.cpp" "$JVM_ROOT/lib/server/libjvm.so"