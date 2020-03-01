#!/bin/bash
docker run -d --name neo4j --publish=7474:7474 --publish=7687:7687 --volume=$HOME/containers/data/neo4j:/data --network=spark-network neo4j
