Fusepool P3 Batchrefine Transformer Docker
============

The provided image simplifies the deployment of the Batchrefine transformer,
while retaining the flexibility of configuring batchrefine.
 
 The image can be obtained in two ways:
 
 * Build using the provieded [Dockerfile](Dockerfile)
 
 * Download a pre-built image from DockerHub
 
 
 ### Building the image
 Cd to the directory with a Dockerfile and run:
 
 ```
 docker build -t fusepool/p3-batchrefine .
 ```
 
 ### Get the image from DockerHub
 
 ```
 docker pull fusepool/p3-batchrefine  
 ```
 
 ## Running examples
 
 ####Run it in the foreground
 
 ```
 docker run --rm -it --name batchrefine fusepool/p3-batchrefine  
 ```
 
 This will run Batchrefine transformer with default configurations and
 attach both STDIN and STDOUT, such that when you press Ctrl+C the container will exit and remove itself.
 
 #### Run in the background and expose logs to the host
 
 ```
 docker run -d --name batchrefine -v /tmp/:/home/user/log/ fusepool/p3-batchrefine 
 ```
 
 The logs from OpenRefine, Batchrefine and supervisor will be written to the /tmp/ folder.
 
 To stop and remove the container:
 
 ```
 docker stop batchrefine
 
 docker rm batchrefine
 ```
 
 #### Pass options and parameters
 
 You can have different configurations for BatchRefine and Openrefine, which can be passed to the docker container, when you `run` it:
 
 ##### Configure OpenRefine memory
 
 ```
 docker run --rm -it --name batchrefine -v /tmp/:/home/user/log/ -e REFINE_MEMORY=2g fusepool/p3-batchrefine
 ```
 
 ##### Pass command line options to the Batchrefine transformer
 The arguments and options that can be passed to the docker container are the same as running it from
 the command line.
 
 To start an asynchronous transformer with verbose logging and `remote` backend (defaults to: localhost:3333)
 
 ```
 docker run --rm -it --name batchrefine -v /tmp/:/home/user/log/ fusepool/p3-batchrefine -v -t async remote
 ```
 
To start a synchronous transformer with verbose logging and `split` backend which will be distributing workload to two 
refine instances by splitting the input in 2 pieces.

 ```
 docker run --rm -it --name batchrefine -v /tmp/:/home/user/log/ fusepool/p3-batchrefine -v -t sync split -l localhost:3333,refine.example.com:3333 -s CHUNK:2
 ```
 