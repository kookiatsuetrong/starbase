export RUNTIME=runtime
export RUNTIME=$RUNTIME:runtime/json.jar
export RUNTIME=$RUNTIME:runtime/mysql.jar
export RUNTIME=$RUNTIME:runtime/bobcat26.jar
export RUNTIME=$RUNTIME:runtime/rockstar.jar
export RUNTIME=$RUNTIME:runtime/freemarker.jar
export RUNTIME=$RUNTIME:runtime/jakarta-mail.jar
export RUNTIME=$RUNTIME:runtime/jakarta-activation.jar
export RUNTIME=$RUNTIME:runtime/tomcat-embed-jasper.jar

mkdir -p runtime
rm runtime/*.class

# compile additional modules
javac -Xlint:unchecked --class-path $RUNTIME \
--source-path modules/sample modules/sample/code/*.java \
--source-path code code/*.java

mv modules/sample/code/*.class runtime
mv code/*.class runtime

# run
java --class-path $RUNTIME Bobcat --home web --port 7601 \
--deployment-descriptor web.xml
