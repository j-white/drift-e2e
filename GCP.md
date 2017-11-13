Create the cluster:

```
gcloud container clusters create drifte2e --machine-type n1-standard-8 --num-nodes 3 --zone us-east1-d
```

Setup for kubectl:
```
gcloud container clusters get-credentials drifte2e --zone us-east1-d
```

Publish images to GCP:
```
cd docker && ./gcp.sh
```

Tear down:
```
gcloud container clusters delete drifte2e --zone us-east1-d
```
