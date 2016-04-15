# Deploy SparkRefine on EMR

To create a SparkRefine enabled cluster on EMR you have to provision the cluster with
the following configurations:

* EMR release-label `emr-4.5.0` (or newer) with Spark
* Software configuration from `s3://shared.spaziodati.eu/fusepool/emr-configuration.json`
* Bootstrap action `s3://shared.spaziodati.eu/fusepool/bootstrap-refine.sh`
The configuration files on s3 are available for *EU (Ireland)* region only.
For other regions you can use the configuration files from this repository.

## Apply OpenRefine transformation to files on s3

1. Download the latest version of `clients-cli-jar-with-dependencies.jar` from [releases](https://github.com/fusepoolP3/p3-batchrefine/releases/latest) to
the master instance of the cluster.

2. Run the command line client with `spark-submit`

```
spark-submit clients-cli-jar-with-dependencies.jar spark -p 4 s3://input.csv s3://transform.json s3://output
```
Where `-p` is the minimum number of partitions for the input file.
