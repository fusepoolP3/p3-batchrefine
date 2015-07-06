BatchRefine [![Build Status](https://travis-ci.org/fusepoolP3/p3-batchrefine.svg?branch=master)](https://travis-ci.org/fusepoolP3/p3-batchrefine)
===========

P3-BatchRefine provides methods to run [OpenRefine](http://openrefine.org) in batch mode.
It does so by providing a collection of wrappers (called _backends_) and a distribution layer on top of OpenRefine.

Clients can access the backends by two ways: using a commandline client or using an HTTP API based on
the
[Fusepool P3 transformer API](https://github.com/fusepoolP3/overall-architecture/blob/master/transformer-api.md). 
The latter allows BatchRefine to take part in P3 pipelines where it can be chained with other transformers.

In either case, two things are needed to run BatchRefine:

1. a CSV to use as input file;
2. an OpenRefine command history (referred to as a _transform
   script_), packaged as a JSON file.

## Try it out

To try BatchRefine right away, use the pre-built docker image

```sh
docker run --rm -it -p 8310:8310 fusepool/p3-batchrefine
```

This will start the [P3 Batchrefine transformer](#p3-transformer) with default configurations,
which can be accessed as follows:

```sh
curl -XPOST -H 'Content-Type:text/csv' --data-binary @input.csv 'localhost:8310/?refinejson=http://url.to/transform.json'
```

## Compiling and Running

### Building from Sources

Building BatchRefine from sources requires Maven 3 and Apache ant (for
building OpenRefine). The procedure, which is somewhat complex because
OpenRefine is not meant to be used as a library, is as follows. In a
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

```
Available backends:
remote    - simple http client that connects to an OpenRefine instance
split     - distributed backend able to connect to multiple OpenRefine instances and improve
            performance by splitting input file.
embedded  - built-in OpenRefine allows to run transforms without starting
            an external OpenRefine instance (currently has limited functionality)
spark     - distributed backend based on Apache Spark aimed at very large workloads 
            (currently has limited functionality)
```

To list the `backend_specific_options`:

```
./bin/batchrefine BACKEND_TYPE --help
```

**Run the [P3 Transformer](#p3-transformer)**

```sh
./bin/transformer [TRANSFORMER_OPTIONS] BACKEND_TYPE [backend_specific_options]
```

`TRANSOFRMER_OPTIONS` are:


```
-v                -- verbose logging
-p [PORT]         -- port to which transformer listens (defaults: 8310)
-t [sync|async]   -- transformer type: synchronous or asynchronous (defaults to sync)
```

Available backends for the transformer are: remote, split, spark

`backend_specific_options` are the same as for the command line client and can be listed with
a `--help` option or, consult the [Usage](#usage) section

To start the most common configuration of the transformer (running synchronously on port 8310 and
connecting to a locally running instance of OpenRefine):

```sh
./bin/transformer remote

#which is equivalent to:

./bin/transformer -v -t sync -p 8310 remote -l localhost:3333
```

## Usage

This section provides usage examples for both [Command Line Tool](#command-line-tool)
and [P3 Transformer](#p3-transformer)

### Command Line Tool

Unfortunately, the command line tool has to be built from sources. 
Read the section on [building BatchRefine from sources](#building-from-sources) for
instructions on how to do it. 


The HTTP API is convenient for integrating BatchRefine as a service,
but clumsy for manual usage. The command line tool works better in
these cases, as you can simply do:

```sh
./bin/batchrefine remote input.csv transform.json > output.csv
```

where, as before, `input.csv` is the input file, `transform.json` is
the transform script and `output.csv` is the output file to which to
write the transformed data.

#### Running With the Embedded Backend

We ship a prepackaged script to start the command line tool under
`./bin`. We will show an example using the _embedded_ backend so that
you do not need to start OpenRefine to actually use it.

```sh
./bin/batchrefine embedded input.csv transform.json
```

this will produce a CSV file on stdout with the transform applied to
it.

##### Limitations of the embedded engine

The embedded engine cannot currently do reconciliation, and extensions
require customization to work (i.e. the RDF extension won't work out
of the box). Further, it is likely that it has to be altered or
rewritten  to work with newer versions of OpenRefine.

#### Accessing a running OpenRefine instance

The command line tool can also act as a direct client to a running
OpenRefine instance. If you have OpenRefine running on
`refine.example.com:3333`, you can use the command line client as follows:

```sh
./bin/batchrefine remote -l refine.example.com:3333 input.csv transform.json
```

#### Simple distributed backend, accesing multiple OpenRefine instances

The command line tool can also split a large file for you and submit it
to multiple OpenRefine instances. For example, you have two OpenRefine instances and you want
to split your file in half:

```sh
./bin/batchrefine split -l refine.example.com:3333,refine1.example.com:3333 -s CHUNK:2 input.csv transform.json
```

the Batchrefine `split` backend will split an input file in 2 chunks, upload them to available OpenRefine
instances and handle the reassembling of the result.
 
##### Command line options of split backend:
To get the list of available options, use `--help` option.

```
./bin/batchrefine split --help
```

```
 --help                              : Prints usage information
 -c (--config) config.properties     : Load batchrefine config from properties
                                       file
 -f (--format) [csv | rdf | turtle]  : The format in which to output the
                                       transformed data
 -h (--hosts) localhost              : OpenRefine instances hosts
 -s (--split) [LINE:int | CHUNK:int] : Set default split logic
```

##### Split logic
Two split strategies are supported:
* CHUNK:N - splits a file into N equal pieces
* LINE:N1,N2,N3 - split the file on the specified line numbers, such that `LINE:30,50,80` will split a file into 4 pieces on 
exectly specified lines.


### P3 Transformer

The BatchRefine P3 transformer wraps (multiple instances of)
OpenRefine under the Fusepool P3 HTTP API. We will show how to build a
transformer that operates over a single instance, running locally.

#### Building with Docker

Building and deploying the P3 transformer with
[Docker](https://www.docker.com/) is easy. Assuming you have Docker
[already installed](https://docs.docker.com/installation/#installation),
there are two main options, depending on your mileage:

1. use the
   [Dockerfile](https://github.com/fusepoolP3/batchrefine/blob/master/docker/Dockerfile)
   we provide;

2. use our wrapper script. At the BatchRefine source root, run:

```sh
cd docker
./batchrefine-docker.sh bootstrap
```

After running the bootstrap step, you just have to run:

```sh
./batchrefine-docker.sh run
```

**For more information regarding docker, refer to the docker [README](docker/README.md)**

and this will expose a synchronous BatchRefine [P3 transformer]() on
port 8310. To access the transformer, you have to make a POST request
to it.

Docker image provides a running OpenRefine instance together with the transformer
so you don't have to care about running your own.

#### Running with your OpenRefine instance
 
```
./bin/transformer -v -t sync remote -l refine.example.com:3333
```

Will start a synchronous P3 Transformer which will connect to the specified OpenRefine instance.
If no URI is specified, defaults to: `localhost:3333`.

#### Running

As per the P3 transformer API, the input file goes in the body of the
POST request, whereas the transform script goes as an URI passed as a
query parameter called `refinejson` in our case. Assuming our input
file is called `input.csv` and is available locally, and our transform
script is called `transform.json` and is available at
`http://www.example.org/transform.json`, we could do a request like:

```sh
curl -XPOST --data-binary @input.csv --H 'Content-Type:text/csv' -H 'Accept:text/csv'
	'http://localhost:8310?refinejson=http://www.example.org/transform.json'
```

to which the transformer will reply with a CSV file that has been
transformed according to what is described in `transform.json`.

*NB:* Although transform scripts can be taken from local URIs such as
 `file://tmp/transform.json`, BatchRefine won't be able to access them
 when running inside Docker. If you want to post `file` URIs, it's
 best to build and run the transformer from sources (see the section
 on building BatchRefine from sources).
 

## Miscellaneous

This work is partially funded by
[Fusepool P3](http://www.fusepool.eu/p3) project, under FP7 grant
609696.
