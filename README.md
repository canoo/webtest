webtest
=======

The most effective way to test your webapp - http://webtest.canoo.com/


Building webtest
----------------

The build is an ant-based build, but uses maven for dependency resolution.

The *lib* directory contains a minimal ant and maven runtime capable of building webtest.

If you don't have ant installed, build everything using

  $ ./bin/webtest.sh full

If you do have ant and maven installed, build evrything using

  $ ant full -nouserlib


