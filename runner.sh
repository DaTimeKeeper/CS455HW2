#!/bin/bash


USERNAME=ewest01

# can add any number of 120 machines to scale your solution
NODE_HOSTS="cheyenne.cs.colostate.edu cheyenne.cs.colostate.edu"
# the port and ip of the registry
# Note: this assumes that the registry is already running before the script is run. 
REGISTRY_HOST="loveland.cs.colostate.edu"
PORT=9999

RATE=3
JAR_PATH="~/CS455/CS455HW2/build/libs/CS455HW2.jar"

# the following command runs the nodes that connect to the registry and send messages
# java -cp ${JAR_PATH} cs455.overlay.Main node ${HOSTNAME} ${REGISTRY_HOST} ${PORT} ${NUM_MSGS} &

for HOSTNAME in ${NODE_HOSTS} ; do
    ssh -l ${USERNAME} ${HOSTNAME} java -jar ${JAR_PATH} cs455.scaling.Main client ${REGISTRY_HOST}  ${PORT} ${RATE} &
done