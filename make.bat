set RUNTIME=runtime
set RUNTIME=%RUNTIME%;runtime\json.jar
set RUNTIME=%RUNTIME%;runtime\mysql.jar
set RUNTIME=%RUNTIME%;runtime\bobcat26.jar
set RUNTIME=%RUNTIME%;runtime\rockstar.jar
set RUNTIME=%RUNTIME%;runtime\freemarker.jar
set RUNTIME=%RUNTIME%;runtime\jakarta-mail.jar
set RUNTIME=%RUNTIME%;runtime\jakarta-activation.jar
set RUNTIME=%RUNTIME%;runtime\tomcat-embed-jasper.jar

if not exist runtime mkdir runtime
erase runtime\*.class

javac --class-path %RUNTIME% -Xlint:unchecked ^
--source-path modules\sample\   modules\sample\code\*.java ^
--source-path code code\*.java

move modules\sample\code\*.class runtime
move code\*.class runtime

java --class-path %RUNTIME% Bobcat --home web --port 7501 ^
--deployment-descriptor web.xml
