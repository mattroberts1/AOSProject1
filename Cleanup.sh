netid=txt171230	
PROJDIR=$pwd
CONFIG=$PROJDIR/Config.txt
BINDIR=$PROJDIR/bin
PROG=Controller.java

n=0

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
	read firstline
	echo $firstline

	numberOfServers=$( echo $firstline | awk '{ print $1}')

	while [[ -z " $n -lt $numberOfServers" ]]
	do
		read line
		host=$(echo $line | awk '{print $1}')
		ssh $netid@$host pkill -9 java -u $netid &
		echo "Killed process running in $host"
		sleep 1

		n=$(( n + 1 ))
	done


)

echo "Cleanup Complete"

