#!/bin/sh
cd "$(dirname "${BASH_SOURCE[0]}")"
./stop
SUSPEND="n"
while (( "$#" )); do
	case $1 in
		suspend)
			SUSPEND="y"
			;;
		*)
			echo "Unrecognized argument"
			exit 1
			;;
	esac
	shift
done
pwd
echo "building..."
if ! ./build; then
	echo "failed to build - see build.log"
	exit 1
fi
echo "running..."
JAR="./birt-simple/target/birt-simple-0.0.3-SNAPSHOT.jar"
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=$SUSPEND,address=8000 -jar "$JAR" > run.log 2> run.err.log &
echo "kill $!" > stop
chmod u+x stop
