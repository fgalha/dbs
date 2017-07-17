cd dbs-core
CALL mvn clean install
cd ..
cd dbs-dist-redis
CALL mvn clean install
cd ..
cd dbs-rtc-load
CALL mvn clean install
cd ..
cd dbs-server
CALL mvn clean install
cd ..
cd dbs-client
CALL mvn clean install
cd ..
