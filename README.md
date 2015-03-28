BatchRefine [![Build Status](https://travis-ci.org/fusepoolP3/p3-batchrefine.svg?branch=master)](https://travis-ci.org/fusepoolP3/p3-batchrefine)
==========

BatchRefine is an effort to run [OpenRefine](http://openrefine.org)
effectively in batch workloads and ETL pipelines. Goals include:

1. providing APIs for embedding and/or otherwise driving the engine
   programmatically;
2. scaling the engine to run over huge data sets.

This is a work in progress, and so is this documentation.

### Introduction


BatchRefine currently works by providing a collection of wrappers
(referred to as _backends_) and, in some modes, a distribution layer,
on top of OpenRefine.  It provides two main ways (access clients) to
access these backends: a command line client, and an HTTP API based on
the
[Fusepool P3 transformer API](https://github.com/fusepoolP3/overall-architecture/blob/master/transformer-api.md). The
latter allows BatchRefine to take part in P3 pipelines where it can be
chained with other transformers.

Backends and access clients can be combined to tailor the needs of the
application. We will discuss two simple combinations that provide
basic functionality: [Command Line Tool](#command-line-tool) and a [Fusepool P3 transformer](#p3-transformer) (accessible via HTTP).

Whatever way you choose to use BatchRefine, you will need two things:

1. a CSV to use as input file;
2. an OpenRefine command history (referred to as a _transform
   script_), packaged as a JSON file.

### Try it out

To try BatchRefine right away, use the pre-built docker image

```sh
docker run --rm -it -p 7100:7100 fusepool/batchrefine
```

This will start the [P3 Batchrefine transformer](#p3t) with default configurations,
which can be accessed as follows:

```sh
curl -XPOST -H 'Content-Type:text/csv' --data-binary @input.csv 'localhost:7100/?refinejson=http://url.to/transform.json'
```

Compiling and Running
--------------

### Building from Sources

Building BatchRefine from sources requires Maven 3 and Apache ant (for
building OpenRefine). The procedure, which is somewhat complex because
OpenRefine is not meant to be used as a library, is as follows. On a
clean folder:

1. Download the OpenRefine 2.6-beta.1 source distribution from:

    https://github.com/OpenRefine/OpenRefine/archive/2.6-beta.1.tar.gz

2. Unzip, untar, and then build OpenRefine, the server and web app
   JARs by running:
   
    ```sh
    ant build jar_server jar_webapp
    ```
    
3. Switch to the `./extensions` folder under the OpenRefine root and
   then download the OpenRefine RDF extension alpha 0.9.0 source
   distribution:

   https://github.com/fadmaa/grefine-rdf-extension/archive/v0.9.0.tar.gz

   Unzip, untar, and then rename the folder it extracts into
   `rdf-extension` and build it as follows:

    ```sh
    mv grefine-rdf-extension-0.9.0 rdf-extension
    cd rdf-extension
   
    JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8' ant build
    ```
   
4. After that, switch back to the OpenRefine root and start it (```./refine```). A
   running instance is required for the tests that BatchRefine will
   run during the build.

5. Download BatchRefine from:
    
    https://github.com/fusepoolP3/p3-batchrefine/releases/latest

   into a sibling folder to OpenRefine (i.e. both OpenRefine and
   BatchRefine should share the same parent folder). As usual, unzip
   and untar. Switch to the `p3-batchrefine-v1.x.x` folder, and run:

    ```sh
    ./bin/refine-import.sh

    mvn package
    ```

The JAR for starting the P3 transformer will be located under:

`./clients/clients-transformer/target/clients-transformer-{project.version}-jar-with-dependencies.jar`

whereas the JAR for starting the command line client will be under:

`./clients/clients-cli/target/clients-cli-{project.version}-jar-with-dependencies.jar`



### Running

This section describes how to run the tools, for more details refer to [Usage](#usage) section.  

**Run the [Command Line Tool](#command-line-tool)**

```sh
./bin/batchrefine [--verbose] BACKEND_TYPE [backend_specific_options] INPUTFILE TRANSFORM [OUTPUTFILE]
```

If no `OUTPUTFILE` is specified, writes to `STDOUT`

Available backends:
* remote    - simple http client that connects to an OpenRefine instance
* embedded  - built-in OpenRefine version [(currently has limited functionality)](#running-with-the-embedded-backend)
* split     - distributed backend able to connect to multiple OpenRefine instances and improve
 performance by splitting input file.
* spark     - distributed backend based on [Apache Spark](http://spark.apache.org) aimed at very large workloads 
(currently has limited functionality)

To list the `backend_specific_options`:

```
./bin/batchrefine BACKEND_TYPE --help
```

**Run the [P3 Transformer](#p3-transformer)**

```sh
./bin/transformer.sh [TRANSFORMER_OPTIONS] BACKEND_TYPE [backend_specific_options]
```

`TRANSOFRMER_OPTIONS` are:


```
-v                -- verbose logging
-p [PORT]         -- port to which transformer listens (defaults: 7100)
-t [sync|async]   -- transformer type: synchronous or asynchronous (defaults to sync)
```

Available backends:
* remote
* split
* spark

`backend_specific_options` are the same as for the command line client and can be listed with
a help option.

To start the most common configuration of the transformer that
connects to a locally running instance of OpenRefine

```sh
./bin/transformer remote

#which is equivalent to:

./bin/transformer -v -t sync remote -l localhost:3333
```



Usage
-----

## P3 Transformer


The BatchRefine P3 transformer wraps (multiple instances of)
OpenRefine under the Fusepool P3 HTTP API. We will show how to build a
transformer that operates over a single instance, running locally.

### Building with Docker

Building and deploying the P3 transformer with
[Docker](https://www.docker.com/) is easy. Assuming you have Docker
[already installed](https://docs.docker.com/installation/#installation),
there are two main options, depending on your mileage:

1. use the
   [dockerfile](https://github.com/fusepoolP3/batchrefine/blob/master/docker/Dockerfile)
   we provide;

2. use our wrapper script. At the BatchRefine source root, run:

```sh
cd docker
./batchrefine-docker.sh bootstrap -t sync
```

### Running

After running the bootstrap step, you just have to run:

```sh
./batchrefine-docker.sh run
```

and this will expose a synchronous BatchRefine [P3 transformer]() on
port 7100. To access the transformer, you have to make a POST request
to it.

As per the P3 transformer API, the input file goes in the body of the
POST request, whereas the transform script goes as an URI passed as a
query parameter called `refinejson` in our case. Assuming our input
file is called `input.csv` and is available locally, and our transform
script is called `transform.json` and is available at
`http://www.example.org/transform.json`, we could do a request like:

```sh
curl -XPOST --data-binary @input.csv --H 'Content-Type:text/csv' -H 'Accept:text/csv'
	'http://localhost:7100?refinejson=http://www.example.org/transform.json'
```

to which the transformer will reply with a CSV file that has been
transformed according to what is described in `transform.json`.

*NB:* Although transform scripts can be taken from local URIs such as
 `file://tmp/transform.json`, BatchRefine won't be able to access them
 when running inside Docker. If you want to post `file` URIs, it's
 best to build and run the transformer from sources (see the section
 on building BatchRefine from sources).


Command Line Tool
=================

The HTTP API is convenient for integrating BatchRefine as a service,
but clumsy for manual usage. The command line tool works better in
these cases, as you can simply do:

```sh
./bin/batchrefine remote input.csv transform.json > output.csv
```

where, as before, `input.csv` is the input file, `transform.json` is
the transform script and `output.csv` is the output file to which to
write the transformed data.

## Building

Unfortunately, the command line tool has to be built from
sources. Read the section on building BatchRefine from sources for
instructions on how to do it.

## Running With the Embedded Backend

We ship a prepackaged script to start the command line tool under
`./bin`. We will show an example using the _embedded_ backend so that
you do not need to start OpenRefine to actually use it. You do need,
however, to have OpenRefine around on your system, as BatchRefine will
import some initialization scripts from it (this limitation will be
removed in future versions).

To run, first set the `OPENREFINE_ROOT` and `BATCHREFINE_ROOT`
environment variables so that they point to the paths of OpenRefine
and BatchRefine, respectively. Then, to transform a file, from the
BatchRefine root, do:

```sh
./bin/batchrefine embedded input.csv transform.json
```

this will produce a CSV file on stdout with the transform applied to
it.

### Limitations

The embedded engine cannot currently do reconciliation, and extensions
require customization to work (i.e. the RDF extension won't work out
of the box). Further, it is likely that it has to be altered or
rewritten  to work with newer versions of OpenRefine.

## Accessing a running OpenRefine instance

The command line tool can also act as a direct client to a running
OpenRefine instance. If you have OpenRefine running on
`refine.example.com`, you can use the command line client as follows:

```sh
./bin/batchrefine -e remote -h refine.example.com input.csv transform.json
```


Miscellaneous
=============

This work is partially funded by
[Fusepool P3](http://www.fusepool.eu/p3) project, under FP7 grant
609696.