#!/bin/bash


if [ $# -lt 1 ] 
  then 
    echo "usage: $0 [write/read/remove]"
    exit 1
fi

echo "Percentile cal started..."

./percentile.sh log-${1}-1t.txt 1 p90_${1}.txt
./percentile.sh log-${1}-5t.txt 1 p90_${1}.txt

for i in {10..50..10}
do

	./percentile.sh log-${1}-${i}t.txt 90 p90_${1}.txt

	echo "Worker $i data finished..." 	
done

echo "Finished!"
