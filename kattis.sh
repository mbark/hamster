#!/bin/bash
FILES=./maps/test100/*
for f in $FILES
do
	echo -e "\n$f"
	time (java -cp bin/ Main < "$f" ) & sleep 11 ; kill -9 `ps aux | grep java | awk '{print $2}'` > /dev/null && echo "Execution terminated!"
done
