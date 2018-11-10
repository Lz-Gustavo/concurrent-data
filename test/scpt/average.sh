#!/bin/bash


# it calculates the average of a column in a given file.
# the output is printed in the screen but it can be also saved in a file [output_file]

  if [ $# -lt 2 ] 
  then 
    echo "usage: $0 data_file num_column [output_file]"
    exit 1
  elif [ $# -le 3 ] 
  then
    data_file=$1
    num_column="\$$2"
    
	#awk_cmd=`awk "{ total += $num_column } END { print total/NR }" $data_file > out.txt`
	awk_cmd=`awk "{ total += $num_column } END { print total/NR }" $data_file > tmp.tmp`

	cat tmp.tmp
	
	if [ $# -eq 3 ] 
	then 
		#output=$3

		cat tmp.tmp >> $3
	fi
	
	rm tmp.tmp

#echo num column = $num_column 
#echo cmd  = $awk_cmd 

fi

#$awk_cmd

#awk '{ total += $2 } END { print total/NR }' $data_file > out.txt
#awk '{ total += $'$num_column '} END { print total/NR }' $data_file


