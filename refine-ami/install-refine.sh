#!/bin/bash

echo debconf shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | /usr/bin/debconf-set-selections

DEBIAN_FRONTEND=noninteractive apt-get -y -qq update
DEBIAN_FRONTEND=noninteractive apt-get -y -qq install unzip wget software-properties-common

apt-add-repository -y ppa:webupd8team/java

DEBIAN_FRONTEND=noninteractive apt-get -y -qq update
DEBIAN_FRONTEND=noninteractive apt-get install -y -qq oracle-java7-installer
DEBIAN_FRONTEND=noninteractive apt-get -y --force-yes -qq install ant && rm -Rf /var/cache/apt/*

mkdir /openrefine

# Download OpenRefine from gitub repository and compile
wget --no-check-certificate https://github.com/OpenRefine/OpenRefine/archive/2.6-beta.1.tar.gz -O ./2.6-beta.1.tar.gz; \
    tar xzvf 2.6-beta.1.tar.gz; rm 2.6-beta.1.tar.gz

mv OpenRefine-2.6-beta.1/* /openrefine; rm -rf OpenRefine-2.6-beta.1

cd /openrefine; ant clean build

# Download and compile rdf-extension
cd extensions; wget https://github.com/fadmaa/grefine-rdf-extension/archive/v0.9.0.tar.gz -O v0.9.0.tar.gz; \
    tar xzvf v0.9.0.tar.gz && rm v0.9.0.tar.gz; \
    mv grefine-rdf-extension-0.9.0 rdf-extension; \
    cd rdf-extension; \
    JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8' ant build

# OpenRefine start script, adjusts the JVM memory according to the machines available memory

echo "#!/bin/bash

if [ -z "\$REFINE_MEMORY" ] ; then
    TOTAL_MEMORY=\`free -b | grep Mem | awk '{print \$2}'\`
    MIN_REFINE_MEMORY=\$(( $TOTAL_MEMORY - 3 * 1024 * 1024 * 1024 ))
    REFINE_MEMORY=\$(( \$TOTAL_MEMORY * 6 / 10 ))

    if [ "\$REFINE_MEMORY" -lt "\$MIN_REFINE_MEMORY" ]; then
        REFINE_MEMORY=\"$MIN_REFINE_MEMORY\"
    fi
fi
exec /openrefine/refine -i 0.0.0.0 -m \$REFINE_MEMORY" > /start.sh

chmod +x /start.sh

# Add OpenRefine to upstart

echo 'exec /start.sh
start on (local-filesystems and net-device-up IFACE!=lo)
stop on runlevel [!2345]
limit nofile 524288 1048576
limit nproc 524288 1048576
respawn' > /etc/init/openrefine.conf