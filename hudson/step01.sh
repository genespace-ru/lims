#!/bin/bash

if [ $FRONTEND = true ]
then
  #npm update
  #npm install
  npm run build-min
fi

echo 'dockerRun' > src/profile.local

echo -e 'paths:\n   lims: /hotreload' > dev.yaml

EXTRA_FLAGS=""

if [ $UPDATE_SHAPSHOTS = true ]
then
   EXTRA_FLAGS="--update-snapshots"
fi

mvn $EXTRA_FLAGS -DBE5_PROFILE=dockerRun -DskipTests clean install -Pwar 
