@echo off
:loop
cls
echo Checking power requests...
powercfg /requests
timeout /t 1 >nul
goto loop