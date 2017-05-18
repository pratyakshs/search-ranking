#!/bin/bash

cat param-tuning/cosinetemplate.java | sed "s/tune1/$1/g" | sed "s/tune2/$2/g" | sed "s/tune3/$3/g" | sed "s/tune4/$4/g" | sed "s/tune5/$5/g" > src/edu/stanford/cs276/CosineSimilarityScorer.java;
ant &> /dev/null;
./flow.sh pa3-data/pa3.signal.train cosine idfs false pa3-data/pa3.rel.train /tmp/cosine$1.txt
