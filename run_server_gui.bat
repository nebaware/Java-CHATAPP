@echo off
echo Compiling Chat Application with GUI...

:: Compile all Java files with required JARs in classpath
javac -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" *.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo Starting Chat Server GUI...

:: Run the server GUI
java -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" --enable-native-access=ALL-UNNAMED ChatServerGUI

pause