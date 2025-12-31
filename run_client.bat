@echo off
echo Starting Jebena-chatapp Client...

:: Run the Jebena-chatapp client (assumes server is already running)
java -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" --enable-native-access=ALL-UNNAMED ChatClient

pause