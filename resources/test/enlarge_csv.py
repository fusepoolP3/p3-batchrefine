#!/usr/bin/python
from __future__ import print_function
import sys
'''
   Simple python script for enlarging a CSV file. It loops
   the file and appends itself at the end until a target 
   number of rows is reached.
'''

if len(sys.argv) < 3:
    print >> sys.stderr, 'Usage: enlarge.py input_csv [target number of rows] [separator]'

input_file = sys.argv[1]
target_rows = int(sys.argv[2])
separator = sys.argv[3]
row = 0

while row < target_rows:
    with open(input_file, "r") as f:
        # Prints header only once.
        header = f.readline()
        if row == 0:
            print(header, end='')
            row += 1
        
        for line in f:            
            print(row, separator, line.split(separator, 1)[1], sep='', end='')
            row += 1
      
