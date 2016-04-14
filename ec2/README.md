# Deploy SparkRefine on EMR

To provision a SparkRefine enabled cluster on EMR you have to follow the following
configurations:

* Release-label `emr-4.5.0` with Spark 1.6.1
* Software configuration from `s3://shared.spaziodati.eu/fusepool/emr-configuration.json`
* Bootstrap action `s3://shared.spaziodati.eu/fusepool/bootstrap-refine.sh`

## Apply transformation to files on s3

1. Download the latest BatchRefine jar version from [releases](https://github.com/fusepoolP3/p3-batchrefine/releases/latest) to
the master instance of the cluster.
2
