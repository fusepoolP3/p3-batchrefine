FROM ubuntu:trusty

RUN groupadd user
RUN useradd -ms /bin/bash -g user user
ENV LC_ALL=C.UTF-8
# Disable Oracle Java 7's prompts.
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | /usr/bin/debconf-set-selections

# Adds repositories for, and installs, Java and Maven 3.
RUN DEBIAN_FRONTEND=noninteractive apt-get -y -qq update
RUN DEBIAN_FRONTEND=noninteractive apt-get -y -qq install software-properties-common unzip supervisor

RUN add-apt-repository 'deb http://ppa.launchpad.net/natecarlson/maven3/ubuntu precise main'
RUN apt-add-repository -y ppa:webupd8team/java

RUN DEBIAN_FRONTEND=noninteractive apt-get -y -qq update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y -qq oracle-java7-installer
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y -qq httpry
RUN DEBIAN_FRONTEND=noninteractive apt-get -y --force-yes -qq install ant maven3 && rm -Rf /var/cache/apt/*; \
    ln -s /usr/bin/mvn3 /bin/mvn

#USER user #more secure option
RUN mkdir -p /home/user/code; mkdir /home/user/supervisor/; mkdir /home/user/log
WORKDIR /home/user/code

# Download and assemble OpenRefine 2.6-beta.1 and the RDF extension.
RUN wget --no-check-certificate https://github.com/OpenRefine/OpenRefine/archive/2.6-beta.1.tar.gz -O ./2.6-beta.1.tar.gz; \
    tar xzvf 2.6-beta.1.tar.gz; rm 2.6-beta.1.tar.gz

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
RUN wget https://github.com/fusepoolP3/batchrefine/archive/master.tar.gz
RUN tar xzvf master.tar.gz; rm master.tar.gz; \
    mv p3-batchrefine-master batchrefine; \
    cd batchrefine; \
    OPENREFINE_ROOT='/home/user/code/OpenRefine' ./bin/refine-import.sh

RUN cd batchrefine; \
    mvn package -DskipTests && rm -rf $HOME/.m2

RUN mv /home/user/code/batchrefine/docker/internal/config/openrefine \
       /home/user/refinedata
RUN cp /home/user/code/batchrefine/docker/internal/p3-transformer-start.sh \
       /home/user/code/
RUN cp /home/user/code/batchrefine/docker/internal/supervisord.conf \
       /home/user/supervisor/

EXPOSE 8310

ENV REFINE_MEMORY=1400M

RUN wget --no-check-certificate https://github.com/papertrail/remote_syslog2/releases/download/v0.14/remote_syslog_linux_amd64.tar.gz \
-O remote_syslog_linux_amd64.tar.gz; tar xzf remote_syslog_linux_amd64.tar.gz; rm remote_syslog_linux_amd64.tar.gz
RUN cp /home/user/code/batchrefine/docker/internal/papertrail.yml \
       /home/user/code/remote_syslog/

#ADD ./internal/supervisord.conf /home/user/supervisor/ # if you want to override it.

CMD ["-v", "-C", "-t sync", "remote"]
ENTRYPOINT ["./p3-transformer-start.sh"]