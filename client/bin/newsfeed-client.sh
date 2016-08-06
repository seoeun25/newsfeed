#!/bin/bash
#

PRG="${0}"
BASEDIR=`dirname ${PRG}`
BIN_DIR=$BASEDIR

baseUrl=$1
echo "baseUrl : ${baseUrl}"

source ${BIN_DIR}/env.sh

if [ "$NEWSFEED_HOME" == "" ]; then
    NEWSFEED_HOME=`cd ${BIN_DIR}/..;pwd`
fi

LIB=${NEWSFEED_HOME}/lib
if [ "$NEWSFEED_LOG_DIR" == "" ]; then
    NEWSFEED_LOG_DIR=${NEWSFEED_HOME}/logs
fi

if [ "$NEWSFEED_CONF_DIR" == "" ]; then
    NEWSFEED_CONF_DIR=$NEWSFEED_HOME/conf
fi

# prepend conf dir to classpath
if [ -n "$NEWSFEED_CONF_DIR" ]; then
  CLASS_PATH="$NEWSFEED_CONF_DIR:$CLASS_PATH"
fi

CLASS_PATH=${CLASS_PATH}:${LIB}/'*'

if [ "$NEWSFEED_JAVA_OPT" == "" ]; then
    NEWSFEED_JAVA_OPT="-Xms2048m -Xmx4096m"
fi

JAVA=$JAVA_HOME/bin/java
exec "$JAVA" ${NEWSFEED_JAVA_OPT} -cp ${CLASS_PATH} -Dlog.dir=${NEWSFEED_LOG_DIR} com.nexr.newsfeed.server.NewsfeedServer start

