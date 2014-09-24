#!/bin/bash

# Starts OpenRefine ...
../OpenRefine/refine &

# ... and the P3 transformer.
java -jar ../clients/clients-transformer/target/clients-transformer-0.1-jar-with-dependencies.jar -t async
