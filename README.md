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
