# OpenRefine image builder

The idea is to have a maintainable image of OpenRefine, that can also be built for various platforms or cloud services.

## How to use it
To achieve maintainability and platform-independence of the image, we use [Packer](https://packer.io/) - 
_a tool for creating identical machine images for multiple platforms from a single source configuration_.

To make use of it, it is required to:

* have packer installed on your machine
* packer configuration file `packer.json` (provided)
* script to install OpenRefine `install-refine.sh` (provided)

Current packer configuration file allows you to build OpenRefine image for 
Docker or to create an equivalent [amazon-ebs AMI](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ComponentsAMIs.html).

1. To create a docker image issue the following command:


  ```sh
  packer build -only=docker packer.json
  ```

  To run the created docker image:

  ```sh
  docker run --rm -p 3333:3333 openrefine /start.sh
  ```

2. Building amazon-ebs AMI

  ```sh
  packer build -only=amazon-ebs packer.json
  ```


## Community OpenRefine AMI and cluster management
 
 We provide a command line tool to launch an arbitrary number of OpenRefine instances on EC2.
 
 ```
 NOTE: it is required to have your AWS keys set as environmental variables:
 AWS_ACCESS_KEY_ID
 AWS_SECRET_ACCESS_KEY
 ```
 
 From the root of the [p3-batchrefine](https://github.com/fusepoolP3/p3-batchrefine) project type:
 
  ```sh
  bin/ec2-cluster --help
  ```
 
  this will display a list of actions and options to manage a cluster of OpenRefine instances on [Amazon EC2](http://aws.amazon.com/ec2/).
  The script has two mandatory arguments: **action**, **cluster name** and a list of options.
  
  Cluster name serves as a unique identifier of a cluster and therefore it is a required parameter together with the desired action.
  
  ```
  Actions:
  
  launch      - start a new cluster 
  
  destroy     - terminate a running cluster
  
  get-slaves  - print the ip-addresses of running instances
  
  reboot      - restart all machines in a running cluster
  
  destroy     - terminate all the machines belonging to a cluster
  
  clean       - delete security group after the cluster has been terminated
  ```
  
  ```
  Options:
  
  -i (--instances):     Number of OpenRefine instances to launch (default: 1)
  
  -r (--region):        EC2 region to launch instances in (default: eu-west-1)
                        WARNING: currently we only support eu-west-1 region.
                        
  -k (--key-pair):      Key pair name to use on instances (if you want to ssh to the instance)
  
  -t (--instance-type): Type of instance to launch (default: m3.medium)
  
  --spot-price:         If specified, launch slaves as spot instances with the
                        given maximum price (in US dollars)
                        
  --delete-groups:      When destroying a cluster, delete the security groups
                        that were created                      
  ```
  
### Example of usage:
  
  To start two _m3.medium_ instances with a maximum price of 0.04:
  
  ```sh
  bin/ec2-cluster launch myname -i 2 --spot-price 0.04
  ```
  
  To get the ip-addresses of a running `myname` cluster:
  
  ```sh
    bin/ec2-cluster get-slaves myname
  ```
  
  Terminate the running `mycluster` and remove the created security-groups:
  
  ```sh
      bin/ec2-cluster destroy myname --delete-groups
  ```
  
  
  