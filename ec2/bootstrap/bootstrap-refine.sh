#!/bin/bash
set -eo pipefail

(sudo yum install -y docker;sudo service docker start) || true

sudo docker pull fusepoolp3/openrefine

sudo echo 'description "OpenRefine docker container"
start on filesystem and started docker
stop on runlevel [!2345]
respawn

pre-start script
  /usr/bin/docker rm -f openrefine || true
end script

script
  /usr/bin/docker run --name=openrefine --rm -p 3333:3333 fusepoolp3/openrefine
end script
' > /etc/init/openrefine.conf

sudo initctl start openrefine
