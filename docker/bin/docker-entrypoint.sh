#!/bin/bash
ls 
DEFAULT_SEARCH_LOCATIONS="classpath:/,classpath:/config/,file:./,file:./config/"
CUSTOM_SEARCH_LOCATIONS=${DEFAULT_SEARCH_LOCATIONS},file:${BASE_DIR}/conf/,${BASE_DIR}/init.d/
CUSTOM_SEARCH_NAMES="application"
#================================
#JAVA Configuration
#================================
JAVA_OPT="${JAVA_OPT} -server -Xms${JVM_XMS} -Xmx${JVM_XMX} -Xmn${JVM_XMN} -XX:MetaspaceSize=${JVM_MS} -XX:MaxMetaspaceSize=${JVM_MMS}"
JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${BASE_DIR}/logs/java_heapdump.hprof"
JAVA_OPT="${JAVA_OPT} -XX:-UseLargePages"
#================================
# Setting system properties
#================================
JAVA_MAJOR_VERSION=$(JAVA -version 2>&1 | sed -E -n 's/.* version "([0-9]*).*$/\1/p')
echo "$JAVA_MAJOR_VERSION"
if [[ "$JAVA_MAJOR_VERSION" -ge "9" ]] ; then
    JAVA_OPT="${JAVA_OPT} -cp .:${BASE_DIR}/plugins/cmdb/*.jar:${BASE_DIR}/plugins/mysql/*.jar"
    JAVA_OPT="${JAVA_OPT} -Xlog:gc*:file=${BASE_DIR}/logs/halo_gc.log:time,tags:filecount=10,filesize=102400"
else
    JAVA_OPT="${JAVA_OPT} -Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${JAVA_HOME}/lib/ext:${BASE_DIR}/plugins/cmdb:${BASE_DIR}/plugins/mysql"
    JAVA_OPT="${JAVA_OPT} -Xloggc:${BASE_DIR}/logs/halo_gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
fi
#============================
# JAR Configuration
#============================
JAVA_OPT="${JAVA_OPT} -jar ${BASE_DIR}/halo.jar"
JAVA_OPT="${JAVA_OPT} ${JAVA_OPT_EXT}"
JAVA_OPT="${JAVA_OPT} --spring.config.location=${CUSTOM_SEARCH_LOCATIONS}"
JAVA_OPT="${JAVA_OPT} --spring.config.name=${CUSTOM_SEARCH_NAMES}"
#JAVA_OPT="${JAVA_OPT} --logging.config=${BASE_DIR}/conf/halo-logback.xml"
JAVA_OPT="${JAVA_OPT} --server.max-http-header-size=524288"

RUN_CMD="java $JAVA_OPT"

echo $RUN_CMD
eval $RUN_CMD