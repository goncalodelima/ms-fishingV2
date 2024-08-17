package com.minecraftsolutions.fishing.model.bucket.repository;

import com.minecraftsolutions.fishing.model.bucket.Bucket;

import java.util.Collection;
import java.util.UUID;

public interface BucketFoundationRepository {

    void setup();

    void insert(Bucket bucket);

    void update(Collection<Bucket> buckets);

    Bucket findOne(UUID uniqueId);

}
