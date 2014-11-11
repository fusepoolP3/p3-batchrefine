FROM ubuntu:trusty

EXPOSE      7100

# Disable Oracle Java 7's prompts.
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | /usr/bin/debconf-set-selections

# Adds repositories for, and installs, Java and Maven 3.
RUN DEBIAN_FRONTEND=noninteractive apt-get -y -q update
RUN DEBIAN_FRONTEND=noninteractive apt-get -y -q install software-properties-common unzip

RUN add-apt-repository 'deb http://ppa.launchpad.net/natecarlson/maven3/ubuntu precise main'
RUN apt-add-repository -y ppa:webupd8team/java

RUN DEBIAN_FRONTEND=noninteractive apt-get -y -q update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y -q oracle-java7-installer
RUN DEBIAN_FRONTEND=noninteractive apt-get -y --force-yes -q install ant maven3; \
    ln -s /usr/bin/mvn3 /bin/mvn 

# Download and assemble OpenRefine 2.6-beta.1 and the RDF extension.
RUN wget -O - --no-check-certificate https://github.com/OpenRefine/OpenRefine/archive/2.6-beta.1.tar.gz -O ./2.6-beta.1.tar.gz; \  
    tar xzvf 2.6-beta.1.tar.gz

RUN mv OpenRefine-2.6-beta.1 OpenRefine; \
    echo 'JAVA_OPTIONS=-Drefine.headless=true' >> ./OpenRefine/refine.ini

RUN cd ./OpenRefine; ant clean build jar_server jar_webapp

RUN cd ./OpenRefine/extensions; \
    wget https://github.com/fadmaa/grefine-rdf-extension/archive/v0.9.0.tar.gz -O v0.9.0.tar.gz; \
    tar xzvf v0.9.0.tar.gz && rm v0.9.0.tar.gz; \
    mv grefine-rdf-extension-0.9.0 rdf-extension; \
    cd rdf-extension; \
    JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8' ant build

# Downloads and builds batchrefine.
ADD https://github.com/fusepoolP3/p3-batchrefine/archive/master.tar.gz /
RUN tar xzvf master.tar.gz; \
    mv p3-batchrefine-master batchrefine; \
    cd batchrefine; \
    ./refine-import.sh

RUN cd batchrefine; \
    mvn package -DskipTests

ENTRYPOINT ["/batchrefine/docker/internal/p3-transformer-start.sh"]
