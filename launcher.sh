#!/bin/bash

# Change this to your netid
netid=mdr140430

# Root directory of your project
PROJDIR=/home/013/m/md/mdr140430/AOSProject

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/AOSProject/config

# Directory your java classes are in
BINDIR=$PROJDIR

# Your main project class
PROG=Controller

n=0

# sed commands to ignore lines starting with # or _
cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read -n 2 i
    echo $i
    read line
    while [[ $n -lt $i ]]
    do
    	read line
    	p=$( echo $line | awk '{ print $1 }' )
	echo $p
        host=$( echo $line | awk '{ print $2 }' )
	echo $host
	
	gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR $PROG $p; exec bash" &

        n=$(( n + 1 ))
    done
)
