#!/bin/bash

BATCHREFINE_ROOT=${BATCHREFINE_ROOT:-$PWD}
OPENREFINE_ROOT=${OPENREFINE_ROOT:-${PWD}../OpenRefine-2.6-beta.1}

java -Drefine.root=${OPENREFINE_ROOT} -jar ./clients/clients-cli/target/clients-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar "$@"
