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
mvn -Dtest=MinionStackTest test
```

If the test was successful, you should be left with a running environment in your Kubernetes cluster.
This alias can be used to fetch the corresponding namespace for the stack:
```
alias gizmo-ns='kubectl get namespaces | grep Active | grep gizmo | awk -F" " "{print \$1}" | head -n 1'
```

You can then enumerate the pods with:
```
kubectl -n $(gizmo-ns) get pods
```

If the tests failed, you can delete the namespace, and then try the setup again:
```
kubectl delete namespace $(gizmo-ns)
mvn -Dtest=MinionStackTest test
```

## Performance testing

Start by access the OpenNMS Web UI:

```
kubectl -n $(gizmo-ns) opennms port-forward 18980:8980
```

Access Hawtio using the port from above:
```
http://127.0.0.1:18980/hawtio/login
```

## Services Notes

### Kafka

List the topics:

```
kubectl -n $(gizmo-ns) exec -ti kafka-0 -- ./bin/kafka-topics.sh --zookeeper zk-cs:2181 --list
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

Lag monitoring:
```
http://127.0.0.1:46635/v2/kafka/local/consumer/OpenNMS/lag
```
