#!/usr/bin/env bash

# Starts OpenRefine ...
/OpenRefine/refine &

# ... and the P3 transformer.
java -jar /batchrefine/clients/clients-transformer/target/clients-transformer-1.0.0-SNAPSHOT-jar-with-dependencies.jar -t async
