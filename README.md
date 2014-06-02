BatchRefine
===========

BatchRefine is an effort to run  [OpenRefine](http://openrefine.org) effectively in batch workloads. Goals include:

1. providing APIs for embedding and/or otherwise driving the engine programmatically;
2. scaling the engine to run over huge data sets. 

This is a work in progress, and so is this documentation.

1. How to build
---------------
Soon.

2. Caveats with Embedded Engine
-------------------------------
The embedded engine is currently a hack. It won't initialize Buttefly (the OpenRefine server) and, at the same time, does not provide a mechanism for isolating modules (extensions), relying on the system classloader for everything. 

This work is partially funded by [Fusepool P3](http://www.fusepool.eu/p3) project, under FP7 grant 609696.

