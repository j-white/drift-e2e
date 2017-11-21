#!/bin/sh -e
[ ! -d minion ] && echo "The script must be invoked from the docker subdirectory of the project." && exit 1

run() {
	echo "Running: $@"
	"$@"
}

echo "Pulling referenced images..."
#docker pull postgres:10.0
#docker pull solsson/kafka:0.11.0.0
#docker pull gcr.io/google_containers/kubernetes-zookeeper:1.0-3.4.10
#docker pull quay.io/pires/docker-elasticsearch-kubernetes:5.6.0

echo "Building Minion image"
run docker build -t opennms/minion ./minion

echo "Building OpenNMS image"
run docker build -t opennms/opennms ./opennms

echo "Building udpgen"
run docker build -t opennms/udpgen ./udpgen

echo "Building Burrow image"
run docker build -t opennms/burrow ./burrow

echo "Building esstress image"
run docker build -t opennms/esstress ./esstress

