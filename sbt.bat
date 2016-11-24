set SCRIPT_DIR=%~dp0
java -Dsbt.log.noformat=true -XX:+CMSClassUnloadingEnabled -Xmx512M -Xss2M -Xdebug -jar "%SCRIPT_DIR%\sbt-launch-0.13.13.jar" %*
