webtest
=======

The most effective way to test your webapp - http://webtest.canoo.com/

Build Instructions with ant
===========================

Ensure that you have Apache Ant, Groovy and Maven installed and configured.

To build a clean build:

```
ant full
```

To build the release and bundle it into a .zip file:

```
ant manuals
ant apidoc
ant assembleRuntime
ant zip
```

These steps produce the file deploy/build.zip.
=======

Building webtest
----------------

The build is an ant-based build, but uses maven for dependency resolution.

The *lib* directory contains a minimal ant and maven runtime capable of building webtest.

If you don't have ant installed, build everything using

  $ ./bin/webtest.sh full

If you do have ant and maven installed, build evrything using

  $ ant full -nouserlib


