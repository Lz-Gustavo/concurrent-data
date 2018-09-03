#!/bin/bash

# calculates the nth percentile of a sample. 
# The sample is ordered according to a column explicitly indicated as a parameter

#if [ $# -eq 4 ]
if [ $# -eq 3 ]
	then
		input_file=$1
#		column=$2
		nth_percentile=$2
		output=$3
#	elif [ $# -eq 3 ] 
	elif [ $# -eq 2 ] 
		then
		input_file=$1
#		column=$2
		nth_percentile=$2
	else
#		echo "usage: $0 sample_file column nth_percentile [output_file]"
		echo "usage: $0 sample_file nth_percentile [output_file]"
		echo "ex.: $0 sample.txt 1 0.95"
		exit 1	
fi

cat "${input_file}" | awk '{print $2}' > "${input_file}_tmp"

total=$(cat "${input_file}" | wc -l)
ordered=$(sort -n "${input_file}_tmp" > tmp_ordered.txt)
#echo "Total ${total}"
# (n + 99) / 100 with integers is effectively ceil(n/100) with floats
count=$(((total * nth_percentile + 99) / 100))
if [ $# -eq 3 ]
	then
		head -n $count "tmp_ordered.txt" | tail -n 1 > $output
	else	
		head -n $count "tmp_ordered.txt" | tail -n 1
fi		
rm ${input_file}_tmp
rm tmp_ordered.txt
