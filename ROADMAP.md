# V2.0.0 Roadmap
BatchRefine 2.0.0 will see two main changes:

* focus on increasing usability of the simple Command Line Client;
* introduction of the _splitting backend_ for simple, practical distributed transformations;
* introduction of supporting scripts for easily performing distributed computations on EC2.

## Command Line Client 
The most well-developed of the access methods to BatchRefine is currently the P3 transformer which, unfortunately, is not very practical for casual users or users who do not want an HTTP interface.

The command line client will:
* be further developed to work hassle-free with the embedded engine;
* be given extra capabilities to work with backends that perform distributed computations.

## Splitting backend
The splitting backend is a practical, simpler alternative to RefineOnSpark. Our realization was that the real-world files we met so far were too small to justify a Spark deployment, and that something simpler could suffice.

This backend works by performing splits at the client, and submitting chunks to a collection of unmodified, stock OpenRefine instances running on, say, EC2. It works only for simple, embarrassingly parallel transforms. It can process multi-file jobs, and provides basic fault-tolerance (i.e., retry-on-failure).

Deploying a collection of OpenRefine instances should be easy with the SpazioDati OpenRefine AMI and boto. We intend to roll it out in two stages:

1. Basic implementation of backend with rollout of FP3 transformer and command line client bindings (mid-February 2015);
2. development of support scripts for on-demand materialization of OpenRefine instances in EC2 (end of February 2015);
3. revamp of documentation with a discussion of all options and lots of examples (mid-March 2015).

