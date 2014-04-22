#!/bin/bash

export DISPLAY=127.0.0.1:5
export JAVA_HOME=/usr/local/java/jdk1.6.0
bin/webtest.sh "$@"
