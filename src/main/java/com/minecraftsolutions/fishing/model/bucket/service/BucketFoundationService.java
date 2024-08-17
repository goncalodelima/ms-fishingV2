package com.minecraftsolutions.fishing.model.bucket.service;

import com.minecraftsolutions.fishing.model.bucket.Bucket;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface BucketFoundationService {

    void put(Bucket bucket);

    void update(Bucket bucket);

    void update(Collection<Bucket> buckets);

    void remove(Bucket bucket);

    Optional<Bucket> get(UUID uniqueId);

    Map<UUID, Bucket> getPendingUpdates();

}
