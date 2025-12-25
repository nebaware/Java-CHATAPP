@echo off
echo Starting Chat Client...

:: Run the client (assumes server is already running)
java -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" ChatClient

pause