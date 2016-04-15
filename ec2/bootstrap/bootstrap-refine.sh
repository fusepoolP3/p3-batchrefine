#!/bin/bash
set -eo pipefail
REFINE_MEMORY=${1:-2g}

(sudo yum install -y docker;sudo service docker start) || true

sudo docker pull fusepoolp3/openrefine
sudo sh -c 'echo "description \"OpenRefine docker container\"
start on filesystem and started docker
stop on runlevel [!2345]
respawn

pre-start script
  /usr/bin/docker rm -f openrefine || true
end script

script
  /usr/bin/docker run -e REFINE_MEMORY='$REFINE_MEMORY' --name=openrefine --rm -p 3333:3333 fusepoolp3/openrefine
end script
" > /etc/init/openrefine.conf'

sudo initctl start openrefine
