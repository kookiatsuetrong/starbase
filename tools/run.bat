set EMAIL=..\runtime
set EMAIL=%EMAIL%;..\runtime\json.jar
set EMAIL=%EMAIL%;..\runtime\mysql.jar
set EMAIL=%EMAIL%;..\runtime\jakarta-mail.jar
set EMAIL=%EMAIL%;..\runtime\jakarta-activation.jar

java --class-path %EMAIL% EmailManager.java
