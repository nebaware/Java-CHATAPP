@echo off
echo Compiling Chat Application...

:: Compile all Java files with required JARs in classpath
javac -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" *.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo Starting Chat Server...

:: Run the server
java -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" ChatServer

pause