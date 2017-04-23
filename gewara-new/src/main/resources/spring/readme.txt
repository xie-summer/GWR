1. WEB-INF/classes/ehcache_new.xml-->ehcache.xml
2. WEB-INF/classes/log4j.properties.new-->log4j.properties-->server.log
4. WEB-INF/appContext-service-task.remote-->.xml(only manage.gewara.com)
5. WEB-INF/classes/gewara.properties-->ip --> host ip
6. resin/lib + oracle jdbc lib, MorzillaParse, dom4j
7. WEB-INF/classes/appContext-config.remote-->appContext-config.xml
8. WEB-INF/classes/appContext-jms.remote-->appContext-jms.xml
9. WEB-INF/classes/appContext-camel.remote-->appContext-camel.xml(only manage.gewara.com)
10.WEB-INF/classes/com/gewara/pay/payurl-remote.properties-->payurl.properties
cp /usr/local/dist/x86_64/libMozillaParser.so /usr/java/jdk1.6.0_22/jre/lib/amd64/
cp /usr/local/dist/x86_64/parserWorker /usr/java/jdk1.6.0_22/jre/lib/amd64/
chmod +x /usr/java/jdk1.6.0_22/jre/lib/amd64/parserWorker
cp cacerts to jdk/jre/lib/security/
mkdir -p /opt/data/paoding/indexes