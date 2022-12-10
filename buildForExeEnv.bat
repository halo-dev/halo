set JAVA_HOME="G:\Java\jdk-17.0.5"
cd build\exeEnv
%JAVA_HOME%\bin\java -Djarmode=layertools -jar ..\libs\halo-2.0.0-SNAPSHOT.jar extract
move /Y .\dependencies\* .\application
move /Y .\spring-boot-loader\* .\application
move /Y .\snapshot-dependencies\* .\application
cd ..\..\
