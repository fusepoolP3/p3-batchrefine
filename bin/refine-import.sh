#!/usr/bin/env bash
#
# Simple script for importing OpenRefine jar files 
# into a local Maven repository.
#

OPENREFINE_ROOT=${OPENREFINE_ROOT:-../OpenRefine-2.6-beta.1}

function import_jar {
    source=$1
    group=$2
    id=$3
    version=$4

    mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file\
 -Dfile=${OPENREFINE_ROOT}/${source}\
 -DgroupId=${group}\
 -DartifactId=${id}\
 -Dversion=${version}\
 -Dpackaging=jar\
 -DlocalRepositoryPath=${PWD}/local-repository
}

rm -rf ${PWD}/local-repository
mkdir ${PWD}/local-repository

import_jar "main/webapp/WEB-INF/lib/ant-tools-1.8.0.jar" "ant-tools" "ant-tools" "1.8.0" 
import_jar "main/webapp/WEB-INF/lib/butterfly-1.0.1.jar" "edu.mit.simile" "butterfly" "1.0.1"
import_jar "main/webapp/WEB-INF/lib/opencsv-2.4-SNAPSHOT.jar" "net.sf.opencsv" "opencsv" "2.4-SNAPSHOT"

import_jar "build/openrefine-trunk.jar" "org.openrefine" "openrefine-core" "2.6.1"
import_jar "build/openrefine-trunk-server.jar" "org.openrefine" "openrefine-server" "2.6.1"

mvn org.apache.maven.plugins:maven-resources-plugin:2.7:copy-resources