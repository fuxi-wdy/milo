@echo off
echo.
echo [信息] 使用Jar命令行运行Web工程。
echo.

set JAVA_OPTS=-Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m

:: 直接使用基于脚本位置的绝对路径运行 jar
java %JAVA_OPTS% -jar "%~dp0..\target\milo.jar"

pause