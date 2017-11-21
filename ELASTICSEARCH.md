
Update throttling:
```
curl -X PUT -H "Content-Type: application/json" http://elasticsearch:9200/_cluster/settings -d '{
    "persistent" : {
        "indices.store.throttle.max_bytes_per_sec" : "200mb"
    }                                                       
}'
```

Disable throttling:
```
curl -X PUT -H "Content-Type: application/json" http://elasticsearch:9200/_cluster/settings -d '{
    "transient" : {
        "indices.store.throttle.type" : "none"
    }
}'
```

Update refresh interval:
```
curl -X PUT -H "Content-Type: application/json" http://elasticsearch:9200/flow-2017-11-21-14/_settings -d '{
    "index" : {
        "refresh_interval" : "-1"
    }
}'
```

Show shard distribution:
```
curl http://elasticsearch:9200/_cat/shards
```

Show flow count:
```
curl http://elasticsearch:9200/_all/flow/_count
```
