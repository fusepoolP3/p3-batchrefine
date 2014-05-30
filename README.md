BatchRefine
===========

BatchRefine is an effort to run  [OpenRefine](http://openrefine.org) effectively in batch workloads. Goals include:

1. providing APIs for embedding and/or otherwise driving the engine programmatically;
2. scaling the engine to run over huge data sets. 

This is a work in progress, and so is this documentation.

1. How to build
---------------
Unfortunately, there is no simple way to build this as OpenRefine does not use Maven (and [won't be switching to it](https://code.google.com/p/google-refine/issues/detail?id=71)). 

To build:
1. create a base folder and clone both OpenRefine and batchrefine in it;
2. switch the OpenRefine repo to 2.6.1, which is the only one we tested against;
3. switch to batchrefine's folder and run:

   ant compile
   
3. to run, use the ./batchrefine script.


2. Caveats with Embedded Engine
-------------------------------
The embedded engine is currently a hack. It won't initialize Buttefly (the OpenRefine server) and, at the same time, does not provide a mechanism for isolating modules (extensions), relying on the system classloader for everything. 

This work is partially funded by [Fusepool P3](http://www.fusepool.eu/p3) project, under FP7 grant 609696.

