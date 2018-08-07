#!/bin/bash
ARG0=$0
DIRNAME="`dirname $ARG0`"

export APP_HOME=${DIRNAME}/..
if [ -f ${APP_HOME}/pid ]; then
	PID=`cat ${APP_HOME}/pid | tr -d '\n'`
	kill $PID
fi
