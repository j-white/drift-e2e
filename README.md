# End-to-end tests for OpenNMS Drift

## Overview

This project creates an environment suitable for testing OpenNMS Drift and all of the related using Kubernetes.

## Setup

Start up Minikube:
```
minikube start --cpus 4 --memory 16384
```

Build the custom Docker images:
```
cd docker
eval $(minikube docker-env)
./build-docker-images.sh
```

Compile the project:
```
mvn clean compile
```

NOTE: You may also need to compile [Gizmo](https://github.com/OpenNMS/gizmo) from source

Setup the stack using:
```
mvn -Dtest=DriftStackTest test
```

This alias can be used to fetch the corresponding namespace for the stack:
```
alias gizmo-ns='kubectl get namespaces | grep Active | grep gizmo | awk -F" " "{print \$1}" | head -n 1'
```

If the test was successful, you should be left with a running environment in your Kubernetes cluster.
You can then enumerate the pods with:
```
kubectl delete namespace $(gizmo-ns)
```

If the tests failed, you can delete the namespace, and then try the setup again:
```
kubectl -n $(gizmo-ns) get pods
```

## Performance testing

Start by access the OpenNMS Web UI:

```
kubectl -n $(gizmo-ns) port-forward opennms 18980:8980
```

Access Hawtio using the port from above:
```
http://127.0.0.1:18980/hawtio/login
```

Start the load generator:
```
kubectl -n $(gizmo-ns) scale --replicas=1 deployment/udpgen
```

## Services Notes

### Kafka

List the topics:

```
kubectl -n $(gizmo-ns) exec -ti kafka-0 -- ./bin/kafka-topics.sh --zookeeper zk-cs:2181 --list
```

Increase the number of partitions:

```
kubectl -n $(gizmo-ns) exec -ti kafka-0 -- ./bin/kafka-topics.sh --alter --zookeeper zk-cs:2181 --topic OpenNMS.Sink.Telemetry.Netflow-5 --partitions 16
```

Topic details:

```
kubectl -n $(gizmo-ns) exec -ti kafka-0 -- ./bin/kafka-topics.sh --describe --zookeeper zk-cs:2181 --topic OpenNMS.Sink.Telemetry.Netflow-5
```

### OpenNMS

Restart:

```
kubectl -n $(gizmo-ns) exec opennms -- /opt/opennms/bin/opennms stop
```


## Udpgen

```
kubectl -n $(gizmo-ns) create -f src/main/resources/udpgen.yaml
```

## Burrow

kubectl -n $(gizmo-ns) port-forward burrow-3923507572-mhbc9 18000:8000


Lag monitoring:
```
http://127.0.0.1:46635/v2/kafka/local/consumer/OpenNMS/lag
```
