#!/bin/bash
ARG0=$0
DIRNAME="`dirname $ARG0`"

export APP_HOME=${DIRNAME}/..

#[ -z "$JAVA_HOME" ] && JAVA_HOME="/usr/java/jdk1.7.0_21"
JAVA_OPTS="-Xmx512m -Duser.timezone=Asia/Shanghai"

##CLASSPATH="$APP_HOME/lib/*.jar;$APP_HOME/plugins/*.jar"

for i in $APP_HOME/lib/*.jar; do     
        CLASSPATH="${CLASSPATH}:${i}"     
done

"${JAVA_HOME:-/usr}/bin/java" -cp ${CLASSPATH} -server -Dlogging.config=${APP_HOME}/conf/logback.xml -Dspring.config.location=${APP_HOME}/conf/application.properties -Dname=com.troila.cloud.mail.file ${JAVA_OPTS} -DAPP_HOME=${APP_HOME} com.troila.cloud.mail.file.Application 2>&1   &
echo $! > "${APP_HOME}/pid"
      

