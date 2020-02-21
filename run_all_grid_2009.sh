#!/bin/sh

need sgegrid

NUM_RUNS=50

for i in {1..5}; do
  qsub -t 1-$NUM_RUNS:1 variable_length_ga.sh ~/workspace/wsc2009/Testset0${i} 2009-variable-length-ga${i} variable-length-ga.params;
done
