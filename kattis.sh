#!/bin/bash
FILES=./maps/kattis/*
time(
for f in $FILES
do
	echo -e "\n$f"
	(java -cp bin/ Main < "$f" ) & sleep 11 ; kill -9 `ps aux | grep java | awk '{print $2}'` > /dev/null && echo "Execution terminated!"
done
)
