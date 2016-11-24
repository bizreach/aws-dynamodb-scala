#!/bin/sh
java -Dsbt.log.noformat=true -XX:+CMSClassUnloadingEnabled -Xmx512M -Xss2M -jar `dirname $0`/sbt-launch-0.13.13.jar "$@"
