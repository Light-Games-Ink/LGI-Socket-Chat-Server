@echo off
set "exe=javaw.exe"
set "arg1=-jar"
set "arg2=Server.jar"
set "arg3=5674"

for /f "tokens=2 delims==; " %%A in (
  'wmic process call create '"%exe%" "%arg1%" "%~dp0%arg2%" "%arg3%"' ^| find "ProcessId"'
) do set "PID=%%A"
echo %PID% > pid.txt
attrib +H pid.txt
echo Server is up