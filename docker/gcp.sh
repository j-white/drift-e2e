#!/bin/sh -e
[ ! -d minion ] && echo "The script must be invoked from the docker subdirectory of the project." && exit 1

run() {
	echo "Running: $@"
	"$@"
}

run docker tag opennms/minion us.gcr.io/opennms-drift/minion:test
gcloud docker -- push us.gcr.io/opennms-drift/minion:test

run docker tag opennms/opennms us.gcr.io/opennms-drift/opennms:test
gcloud docker -- push us.gcr.io/opennms-drift/opennms:test

run docker tag opennms/udpgen us.gcr.io/opennms-drift/udpgen:test
gcloud docker -- push us.gcr.io/opennms-drift/udpgen:test

run docker tag opennms/burrow us.gcr.io/opennms-drift/burrow:test
gcloud docker -- push us.gcr.io/opennms-drift/burrow:test
