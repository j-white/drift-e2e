# End-to-end tests for OpenNMS Drift


## Notes

Start up Minikube:
```
minikube start --cpus 4 --memory 16384
```

Build the Docker images:
```
cd docker
eval $(minikube docker-env)
./build-docker-images.sh
```

# Services

## Kafka

List the topics:

```
kubectl -n gizmo-b45644ff-7833-4928-bbbd-3548d8e96717 exec -ti kafka-0 -- ./bin/kafka-topics.sh --zookeeper zk-cs:2181 --list
```


## Flowgen

```
kubectl -n gizmo-6f023f87-6a6b-484c-a5a3-b7e3c996020c create -f src/main/resources/flowgen.yaml
```
