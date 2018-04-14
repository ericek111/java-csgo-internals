# Java CS:GO Internals
Internal Linux Java cheat for CS:GO - very WIP.

Heavily based on my [external Java cheat](https://github.com/ericek111/java-csgo-externals).

### TO-DO:
- more modules
- proper documentation
- optimisation (Callback methods spam threads?)
- fix JVM? The cheat can't be un/reloaded because of [JDK-4093633](https://bugs.openjdk.java.net/browse/JDK-4093633)
- port [BufferStruct](https://github.com/ericek111/java-csgo-internals/blob/master/src/eu/lixko/csgoshared/util/BufferStruct.java) to Java 9 - *newInstance() in Class has been deprecated*

### Credits:
- [aixxe](https://aixxe.net/) for his very educative tutorials, [VMThook](https://github.com/aixxe/vmthook/blob/master/vmthook.h) and [CS:GO internal bhop](https://github.com/aixxe/csgo-bhop-linux) which I based my cheat on.
- [Jonatino](https://github.com/Jonatino) for his great work on Java cheats and [Java-Memory-Manipulation](https://github.com/Jonatino/Java-Memory-Manipulation) library.