#!/usr/bin/env bash

BATCHREFINE_ROOT=/home/user/code/batchrefine

VERSION=`cat ${BATCHREFINE_ROOT}/version`
echo "transformer version is $VERSION"
echo "transformer options are $@"

# ... and the P3 transformer.
java -jar ${BATCHREFINE_ROOT}/clients/clients-transformer/target/clients-transformer-${VERSION}-jar-with-dependencies.jar $@