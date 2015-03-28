#!/bin/bash

BATCHREFINE_ROOT=${BATCHREFINE_ROOT:-$PWD}
OPENREFINE_ROOT=${OPENREFINE_ROOT:-${PWD}/../OpenRefine-2.6-beta.1}
VERSION=`cat ${BATCHREFINE_ROOT}/version`

java -Drefine.root=${OPENREFINE_ROOT} -jar ./clients/clients-cli/target/clients-cli-${VERSION}-jar-with-dependencies.jar "$@"
