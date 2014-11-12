#!/usr/bin/env bash

function print_usage {
    cat <<EOF
Usage: batchrefine-docker.sh {bootstrap|run|clean}

   bootstrap        boostraps a docker image for BatchRefine. Has specialized 
                      parameters and help (batch-refinedocker.sh --help).
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
	#TODO support more than one mode simultaneously
        if ./internal/bootstrap.py "$@"
	then 
	    docker build ${DOCKER_OPTIONS} -t spaziodati/batchrefine .
	else
	    exit -1
	fi
	;;
    run)
	if [ ! -f ./Dockerfile ] 
	then
	    echo "No Dockerfile found, you have to bootstrap first."
	    echo "Try 'batchrefine-docker.sh' without arguments to see usage information."
	    exit -1
	fi
	port=${2:-7100}
	exec docker run ${DOCKER_OPTIONS} -p 0.0.0.0:${port}:7100 -t spaziodati/batchrefine
	;;
    clean)
	to_kill=`docker ps -a | grep 'batchrefine' | cut -d' ' -f1 | xargs`
	docker kill ${to_kill}
	docker rm ${to_kill}
	;;
    *)
	print_usage
	exit -1
	;;
esac
