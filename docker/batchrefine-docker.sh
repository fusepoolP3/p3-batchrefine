#!/usr/bin/env bash

function print_usage {
    cat <<EOF
Usage: batchrefine-docker.sh {bootstrap|run|clean}

   bootstrap        boostraps a docker image for BatchRefine.

   run [PORT]       starts a container with the BatchRefine P3 
                      transformer and binds it to PORT (defaults 
                      to 7100)
   clean            destroys and wipes all BatchRefine containers
    
EOF

}

if [ -z ${1+x} ] 
then
    print_usage
    exit -1
fi

case "$1" in
    bootstrap)
	    if [ ! -f ./Dockerfile ]
        	then
        	    echo "No Dockerfile found, you have to bootstrap first."
        	    echo "Try 'batchrefine-docker.sh' without arguments to see usage information."
        	    exit -1
        	fi
	    docker build ${DOCKER_OPTIONS} --force-rm=true -t spaziodati/batchrefine .
	 ;;
    run)
	port=${2:-7100}
	type=${3:-sync}
	exec docker run -d ${DOCKER_OPTIONS} -p 0.0.0.0:${port}:7100 --name batchrefine spaziodati/batchrefine -t ${type} remote
	if [ $? -ne 0 ]; then
        echo "Couldn't start batchrefine image, try bootstrapping first"
    fi
	;;
    clean)
	docker kill batchrefine
	docker rm batchrefine
	docker rmi spaziodati/batchrefine
	;;
    *)
	print_usage
	exit -1
	;;
esac