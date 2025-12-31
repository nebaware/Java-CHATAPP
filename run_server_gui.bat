@echo off
echo Compiling Jebena-chatapp with GUI...

:: Compile all Java files with required JARs in classpath
javac -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" *.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo Starting Jebena-chatapp Server GUI...

:: Run the Jebena-chatapp server GUI
java -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" --enable-native-access=ALL-UNNAMED ChatServerGUI

pause