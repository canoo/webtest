webtest
=======

The most effective way to test your webapp - http://webtest.canoo.com/

Building webtest
----------------

The build is an ant-based build, but uses maven for dependency resolution.

The *lib* directory contains a minimal ant and maven runtime capable of building webtest.

If you don't have ant installed, build everything using

```
$ ./bin/webtest.sh full
```

If you do have ant, groovy, and maven installed, build evrything using

```
$ ant full -nouserlib
```

Either of the above steps produces 

- deploy/webtest.zip (webtest executable release)
- deploy/doc.zip (webtest manual)
- deploy/javadoc.zip (webtest api docs)

and some other artifacts as well.

Interestingly, the files *webtest.[bat|sh]* are the same scripts used to run webtest after installation. 


