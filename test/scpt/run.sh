#!/bin/bash

if [[ $# -ne 1 ]]
then
	echo "usage: $0 jvm_gigs_maxheap"
	exit 1
fi

echo "started...."
id_test=(13C 14C 15C)
num_threads=(1 2 4 8 16 32)

for i in ${id_test[*]}
do	
	for j in ${num_threads[*]}
	do
		java -Xms${1}G -Xmx${1}G -jar concurrent-data.jar jvm${1}G/Test${i}/config.txt $j
		echo "$j threads data finished..."
	done

	mv test/*.txt jvm${1}G/Test${i}/
	echo "---------------------"
	echo "Finished data for Test index $i."
	echo ""

done
echo "Finished!"
