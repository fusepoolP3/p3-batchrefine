#!/usr/bin/env bash
set -eo pipefail
BATCHREFINE_ROOT=/home/user/code/batchrefine

# Start Httpry to log accesses
httpry -f source-ip,request-uri -d -i eth0 'tcp port 8310' -o /var/log/httpry.log


# Start OpenRefine using the supervisor daemon
echo "starting supervisor"
supervisord -c /home/user/supervisor/supervisord.conf

VERSION=`cat ${BATCHREFINE_ROOT}/version`
echo "transformer version is $VERSION"
echo "transformer options are $@"

# ... and the P3 transformer.
java -jar ${BATCHREFINE_ROOT}/clients/clients-transformer/target/clients-transformer-${VERSION}-jar-with-dependencies.jar $@ 2>&1 | tee /var/log/transformer.log
