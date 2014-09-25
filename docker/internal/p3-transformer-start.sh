#!/usr/bin/env bash

# Starts OpenRefine ...
/OpenRefine/refine &

# ... and the P3 transformer.
java -jar /batchrefine/clients/clients-transformer/target/clients-transformer-0.1-jar-with-dependencies.jar -t async
