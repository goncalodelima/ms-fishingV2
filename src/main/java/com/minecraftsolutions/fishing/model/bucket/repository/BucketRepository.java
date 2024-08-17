package com.minecraftsolutions.fishing.model.bucket.repository;

import com.minecraftsolutions.database.executor.DatabaseExecutor;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.bucket.adapter.BucketAdapter;
import lombok.AllArgsConstructor;
import com.minecraftsolutions.database.Database;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class BucketRepository implements BucketFoundationRepository {

    private final Database database;
    private final BucketAdapter adapter = new BucketAdapter();

    @Override
    public void setup() {

        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("CREATE TABLE IF NOT EXISTS fishing_bucket (uniqueId CHAR(36) PRIMARY KEY, level INTEGER)")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS fishing_bucket_fishes (uniqueId CHAR(36) REFERENCES fishing_bucket(uniqueId), fish VARCHAR(36), amount INTEGER, PRIMARY KEY(uniqueId, fish))")
                    .write();
        }

    }

    @Override
    public void insert(Bucket bucket) {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("INSERT INTO fishing_bucket VALUES(?,?)")
                    .write(statement -> {
                        statement.set(1, bucket.getUniqueId().toString());
                        statement.set(2, bucket.getLevel());
                    });
        }
    }

    @Override
    public void update(Collection<Bucket> buckets) {
        try (DatabaseExecutor executor = database.execute()) {

            executor
                    .query("UPDATE fishing_bucket SET level = ? WHERE uniqueId = ?")
                    .batch(buckets, (bucket, statement) -> {
                        statement.set(1, bucket.getLevel());
                        statement.set(2, bucket.getUniqueId().toString());
                    });

            executor
                    .query("INSERT INTO fishing_bucket_fishes (uniqueId, fish, amount) VALUES(?,?,?) ON DUPLICATE KEY UPDATE amount = VALUES(amount)")
                    .batch(buckets.stream()
                                    .flatMap(bucket -> bucket.getFishes().entrySet().stream()
                                            .map(entry -> new Object[]{
                                                    bucket.getUniqueId().toString(),
                                                    entry.getKey().getId(),
                                                    entry.getValue()
                                            }))
                                    .collect(Collectors.toList()),
                            (params, statement) -> {
                                statement.set(1, params[0]);
                                statement.set(2, params[1]);
                                statement.set(3, params[2]);
                            });
        }
    }

    @Override
    public Bucket findOne(UUID uniqueId) {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT * FROM fishing_bucket A LEFT JOIN fishing_bucket_fishes B ON A.uniqueId = B.uniqueId WHERE A.uniqueId = ?")
                    .readOne(statement -> statement.set(1, uniqueId.toString()), this.adapter).orElse(null);
        }
    }

}
