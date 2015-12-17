@echo off
attrib -H pid.txt
set /p mYpid=<pid.txt
taskkill /f /PID %mYpid%
del /Q /F pid.txt
echo Server is down