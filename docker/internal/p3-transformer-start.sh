#!/usr/bin/env bash

REFINE_OPTIONS=${1:-''}
TRANSFORMER_OPTIONS=${2:-''}

echo "refine options are ${REFINE_OPTIONS}"
echo "transformer options are ${TRANSFORMER_OPTIONS}"

# Starts OpenRefine ...
<<<<<<< HEAD
/OpenRefine/refine ${REFINE_OPTIONS}

# ... and the P3 transformer.
java -jar /batchrefine/clients/clients-transformer/target/clients-transformer-0.1-jar-with-dependencies.jar ${TRANSFORMER_OPTIONS}
=======
/OpenRefine/refine -m 8096M&

# ... and the P3 transformer.
java -jar /batchrefine/clients/clients-transformer/target/clients-transformer-1.0.0-SNAPSHOT-jar-with-dependencies.jar -t async
>>>>>>> 4e5559d992d9d099a725efa3947d78b6c7b24ffe
