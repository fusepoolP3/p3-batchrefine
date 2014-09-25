#!/usr/bin/env bash

function print_usage {
    cat <<EOF
Usage: batchrefine-docker.sh {bootstrap|run|clean}

   bootstrap        boostraps a docker image for BatchRefine
   run [-p PORT]    starts a container with the BatchRefine P3 
                      transformer and binds it to PORT
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
	docker build -t spaziodati/batchrefine .
	;;
    run)
	port=${2:-7100}
	exec docker run -p 0.0.0.0:${port}:7100 -t spaziodati/batchrefine
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

PORT=${1}

docker run -p 0.0.0.0:${1}:${1} -t spaziodati/batchrefine
