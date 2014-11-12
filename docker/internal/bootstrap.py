#!/usr/bin/env python
'''Simple script for "bootstrapping" the batchrefine Docker image.
It basically parses arguments and expands them into variables.
'''
from optparse import OptionParser

parser = OptionParser(add_help_option=False)

parser.add_option('-h', '--help', dest='show_help', action='store_true',
                  help='Prints this help message and exits',
                  default=False)

parser.add_option('-m', '--memory', dest='memory',
                  help='how much RAM to give the refine backend (defaults to 1400M)',
                  default='1400M', metavar='M')

parser.add_option('-t', '--transformer-mode',
                  dest='mode',
                  metavar='MODE',
                  help='what kind of P3 transformer to start (sync or async, defaults to async)',
                  choices=['sync', 'async'], default='async')

(options, args) = parser.parse_args()

if options.show_help:
    parser.print_help()
    exit(-1) 

refine_options = '"-m {0}","-t {1}"'.format(options.memory, options.mode)

with open('./internal/Dockerfile.template', 'r') as dockerfile_template:
    dockerfile_contents = dockerfile_template.read().format(refine_options)

with open('./Dockerfile', 'w') as dockerfile_output:
    dockerfile_output.write(dockerfile_contents)

exit(0)





