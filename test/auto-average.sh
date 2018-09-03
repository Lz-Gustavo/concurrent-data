#!/bin/bash


echo "Average calc started..."

for i in {5..50..5}
do
	./average.sh log-read-${i}t.txt 1 average_read.txt
	./average.sh log-remove-${i}t.txt 1 average_remove.txt
	./average.sh log-write-${i}t.txt 1 average_write.txt

	echo "Worker $i data finished..." 	
done

echo "Finished!"
