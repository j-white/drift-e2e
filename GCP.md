Create the cluster:

```
gcloud container clusters create drifte2e --machine-type n1-standard-16 --num-nodes 6 --zone us-east1-d
```

NOTE: n1-standard-16 has 16 vCPUs and 60GB RAM

Setup for kubectl:
```
gcloud container clusters get-credentials drifte2e --zone us-east1-d
```

Publish images to GCP:
```
cd docker && ./gcp.sh
```

SSD storage:
```
kubectl apply --filename gcp.yaml
```

NOTE: See https://cloud.google.com/compute/docs/disks/performance

Scale:
```
gcloud container clusters resize drifte2e --node-pool default-pool --size 6
```

Add zone:
```
gcloud beta container clusters update  drifte2e --zone us-east1-d --additional-zones us-east1-c
```

Tear down:
```
gcloud container clusters delete drifte2e --zone us-east1-d
```
