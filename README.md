Jenastop
========

An android application to show the scheduled stops of trams and buses at most stations in Jena (Germany).

Building
--------

```
$ sbt android:package
```

should build the debug version of this app.

Issues
------

OpenJDK (on Fedora 23 at least) seems to ignore the Java 7 compatibility requirement. For this reason
i use the OracleJDK version 7 to compile this app. One can use the `-java-home` switch when starting
sbt.

Switching between debug and release builds requires a `git clean -fxd` beforehand. This is due to
a bug in the android-sdk-plugin.

License
-------

This project is licensed under the GPLv3.
