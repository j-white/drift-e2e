#!/bin/sh -e
[ ! -d minion ] && echo "The script must be invoked from the docker subdirectory of the project." && exit 1

run() {
	echo "Running: $@"
	"$@"
}

echo "Pulling Postgres image from public registry"
run docker pull postgres:10.0

echo "Building Minion image"
run docker build -t opennms/minion ./minion

echo "Building OpenNMS image"
run docker build -t opennms/opennms ./opennms
