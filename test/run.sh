#!/bin/bash

echo "Experiment started...."

for i in 0 3 4
do
	for j in 1 10 25 50
	do
		java -jar concurrent-data.jar Test${i}/config.txt $j
		echo "Worker $j data finished..."
	done

	mv test/*.txt Test${i}/
	echo "---------------------"
	echo "Finished data for index $i data structure"

done
echo "Finished!"
