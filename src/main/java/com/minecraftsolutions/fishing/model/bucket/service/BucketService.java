package com.minecraftsolutions.fishing.model.bucket.service;

import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.bucket.repository.BucketFoundationRepository;
import com.minecraftsolutions.fishing.model.bucket.repository.BucketRepository;
import com.minecraftsolutions.database.Database;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BucketService implements BucketFoundationService {

    private final Map<UUID, Bucket> cache = new ConcurrentHashMap<>();
    private final Map<UUID, Bucket> pendingUpdates = new HashMap<>();
    private final BucketFoundationRepository bucketRepository;

    public BucketService(Database database){
        this.bucketRepository = new BucketRepository(database);
        this.bucketRepository.setup();
    }

    @Override
    public void put(Bucket bucket) {
        this.cache.put(bucket.getUniqueId(), bucket);
        this.bucketRepository.insert(bucket);
    }

    @Override
    public void update(Bucket bucket) {
        this.pendingUpdates.put(bucket.getUniqueId(), bucket);
    }

    @Override
    public void update(Collection<Bucket> buckets) {
        this.bucketRepository.update(buckets);
    }

    @Override
    public void remove(Bucket bucket) {
        this.cache.remove(bucket.getUniqueId());
    }

    @Override
    public Optional<Bucket> get(UUID uniqueId) {

        Bucket bucket = this.cache.get(uniqueId);

        if (bucket != null)
            return Optional.of(bucket);

        bucket = this.bucketRepository.findOne(uniqueId);

        if (bucket != null)
            this.cache.put(bucket.getUniqueId(), bucket);

        return Optional.ofNullable(bucket);
    }

    @Override
    public Map<UUID, Bucket> getPendingUpdates() {
        return pendingUpdates;
    }

}
