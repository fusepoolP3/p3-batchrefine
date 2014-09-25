BatchRefine
===========

BatchRefine is an effort to run [OpenRefine](http://openrefine.org)
effectively in batch workloads and ETL pipelines. Goals include:

1. providing APIs for embedding and/or otherwise driving the engine
   programmatically;
2. scaling the engine to run over huge data sets.

This is a work in progress, and so is this documentation. BatchRefine
can be currently deployed and built in two ways.

1. With Docker
--------------

Building and deploying BatchRefine with
[Docker](https://www.docker.com/) is easy. Assuming you have Docker
[already installed](https://docs.docker.com/installation/#installation),
there are two main options, depending on your mileage:

1. use the
   [dockerfile](https://github.com/fusepoolP3/batchrefine/blob/master/docker/Dockerfile)
   we provide;

2. use our wrapper script. At the BatchRefine source root, run:

```sh
cd docker
./batchrefine-docker.sh bootstrap
./batchrefine-docker.sh run
```

2. From Sources
---------------

Building BatchRefine from sources requires Maven 3. The procedure,
which is somewhat complex because OpenRefine is not meant to be used
as a library, is as follows:

1. Download OpenRefine 2.6-beta.1 from:

   https://github.com/OpenRefine/OpenRefine/releases/tag/2.6-beta.1

2. Build OpenRefine by running:

   `ant jar_server`

3. Download and install the OpenRefine RDF extension alpha 0.9.0:

   https://github.com/fadmaa/grefine-rdf-extension/releases/tag/v0.9.0

4. Start the OpenRefine instance downloaded in step (1)

5. Clone the BatchRefine repository in a sibling folder to OpenRefine, and run:
   
```sh
	./import-refine.sh

	mvn install
```

2. Caveats with Embedded Engine
-------------------------------
The embedded engine is currently a hack. It won't initialize Butterfly (the OpenRefine server) and, at the same time, does not provide a mechanism for isolating modules (extensions), relying on the system classloader for everything. 

This work is partially funded by [Fusepool P3](http://www.fusepool.eu/p3) project, under FP7 grant 609696.

