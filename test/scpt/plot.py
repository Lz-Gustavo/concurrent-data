import matplotlib.pyplot as plt 
import sys

def load_file(jvm, testID, threads, operation):
	"""
		reads the data from result file and returns the summ of all values on it
	"""
	value = 0

	file = open("jvm"+str(jvm)+"G/Test"+str(testID)+"/log-"+operation+"-"+str(threads)+"t.txt", "r")

	data = file.readlines()
	for d in data:
		value += int(d)
	
	file.close()

	return value

def plot(x_data, y_data, name):
	"""
		plots the graph for read/write/remove operations and saves it
	"""
	
	# naming the x axis 
	plt.xlabel('Num workers')
	# naming the y axis 
	plt.ylabel('Throughput (cmds/min)')

	# write
	plt.plot(x_data, y_data[0], "b", label="write")

	# read 
	plt.plot(x_data, y_data[1], "r", label="read")

	# remove
	#plt.plot(x_data, y_data[2], "y") 

	# giving a title to my graph 
	plt.title(name)

	plt.legend(loc='best')

	plt.savefig(name+'.png')


if len(sys.argv) < 4:
	print "Must insert jvm heap size(GB), Test ID and structure name as command line arguments, ex: 'python plot.py 6 1 CowAL'"
	sys.exit(1)

jvm = sys.argv[1]
testID = sys.argv[2]
struct_name = sys.argv[3]

x = [1, 2, 4, 8, 16, 32]
aux_y = []
y = []

# capture all data from log-write files
for i in x:
	aux_y.append(load_file(jvm, testID, i, "write"))

y.append(aux_y)
aux_y = []

# log-read files
for i in x:
	aux_y.append(load_file(jvm, testID, i, "read"))

y.append(aux_y)
aux_y = []

# log-remove files
#for i in x:
#	aux_y.append(load_file(jvm, testID, i, "remove"))

y.append(aux_y)
aux_y = []

plot(x, y, struct_name)