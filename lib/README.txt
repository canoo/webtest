This directory contains the small ant runtime required to build webtest.
The jar files in this directories are organized according to following principles:

lib/build/
    for jar files that belongs to a developer's run-time but not to an end-user's runtime.
    The files are referenced from the startup scripts (webtest.sh, webtets.cmd), if absolutely necessary,
    or from ant's build.xml.
lib/log4j/
    Various configuration for log4j.
    This is used by both developers and webtest users.
