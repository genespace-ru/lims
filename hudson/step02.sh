#!/bin/bash

ssh iap "docker-compose -f /servers/lims/docker-compose.yaml stop lims"

if [ "$RM_CONTAINERS" = "true" ]
then
    ssh iap "docker-compose -f /servers/lims/docker-compose.yaml rm -f lims"
fi

scp $WORKSPACE/target/lims.war iap:/servers/lims/docker.in/lims.war
ssh iap 'sudo rm -rf /servers/lims/docker.in/src'
ssh iap 'mkdir -p /servers/lims/docker.in/src'
scp -r $WORKSPACE/src iap:/servers/lims/docker.in
scp -r $WORKSPACE/project.yaml iap:/servers/lims/docker.in

ssh iap 'sudo chgrp -R docker /servers/lims/docker.in/src'
ssh iap 'sudo chmod -R g+w /servers/lims/docker.in/src'

ssh iap 'sudo rm -rf /servers/lims/docker.out/logs/*'

ssh iap "docker-compose -f /servers/lims/docker-compose.yaml up -d"
ssh iap "cd /servers/sequencer; docker-compose -f /servers/sequencer/docker-compose.yaml restart nginx"

sleep 5

ssh iap 'sudo chmod 777 /servers/lims/docker.out/logs/*'