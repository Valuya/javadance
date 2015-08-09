#!/bin/sh
#set -x
if [ $# -lt 1 ] ; then
    echo "Usage: testsrv.sh <start|stop> ..." >&2
    exit 1
fi
case "$1" in
    start)
        LOCALSHAREPATH=$2/scratch
        JAR=$3
        rm -rf ${LOCALSHAREPATH} && mkdir -p ${LOCALSHAREPATH}
        echo "Starting test server"
        java -jar ${JAR} target/test-classes/testsrv.xml >/tmp/testsrv.out 2>&1  &
        echo $! > testsrv.pid
        ;;
    stop)
        if [ -f testsrv.pid ] ; then
            echo "Stopping test server"
            kill `cat testsrv.pid` || true
            rm -f testsrv.pid
        fi
        ;;
    *)
        echo "Usage: testsrv.sh <start|stop> ..." >&2
        ;;
esac
