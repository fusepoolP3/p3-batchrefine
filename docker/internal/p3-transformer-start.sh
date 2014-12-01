#!/usr/bin/env bash

BATCHREFINE_ROOT=${BATCHREFINE_ROOT:-/batchrefine}
OPENREFINE_ROOT=${OPENREFINE_ROOT:-/OpenRefine}

REFINE_OPTIONS=${1:-''}
TRANSFORMER_OPTIONS=${2:-''}
VERSION=`cat ${BATCHREFINE_ROOT}/version`

echo "refine options are ${REFINE_OPTIONS}"
echo "transformer options are ${TRANSFORMER_OPTIONS}"

# Starts OpenRefine ...
${OPENREFINE_ROOT}/refine ${REFINE_OPTIONS} &

# ... and the P3 transformer.
java -jar ${BATCHREFINE_ROOT}/clients/clients-transformer/target/clients-transformer-${VERSION}-jar-with-dependencies.jar ${TRANSFORMER_OPTIONS}
