#!/usr/bin/env bash

REFINE_OPTIONS=${1:-''}
TRANSFORMER_OPTIONS=${2:-''}

echo "refine options are ${REFINE_OPTIONS}"
echo "transformer options are ${TRANSFORMER_OPTIONS}"

# Starts OpenRefine ...
/OpenRefine/refine ${REFINE_OPTIONS}

# ... and the P3 transformer.
java -jar /batchrefine/clients/clients-transformer/target/clients-transformer-0.1-jar-with-dependencies.jar ${TRANSFORMER_OPTIONS}
