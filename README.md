BatchRefine
===========

BatchRefine is an effort to run  [OpenRefine](http://openrefine.org) effectively in batch workloads. Goals include:

1. providing APIs for embedding and/or otherwise driving the engine programmatically;
2. scaling the engine to run over huge data sets. 

This is a work in progress, and so is this documentation.

1. How to build
---------------
The build procedure is still, unfortunately, somewhat complicated, but should work reliably now.

1. Download OpenRefine 2.6-beta.1 from:

   https://github.com/OpenRefine/OpenRefine/releases/tag/2.6-beta.1

2. Download and install the OpenRefine RDF extension alpha 0.9.0:

   https://github.com/fadmaa/grefine-rdf-extension/releases/tag/v0.9.0

3. Install the P3 extractor library by following the instructions at:

   https://github.com/fusepoolP3/p3-extractor-library/blob/master/README.md

4. Start the OpenRefine instance downloaded in step (1)

5. Clone the BatchRefine repository in a sibling folder to OpenRefine, and run:
   
   ./import-refine.sh
   mvn install

2. Caveats with Embedded Engine
-------------------------------
The embedded engine is currently a hack. It won't initialize Buttefly (the OpenRefine server) and, at the same time, does not provide a mechanism for isolating modules (extensions), relying on the system classloader for everything. 

This work is partially funded by [Fusepool P3](http://www.fusepool.eu/p3) project, under FP7 grant 609696.

