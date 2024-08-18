package com.minecraftsolutions.fishing.model.user.repository;

import com.minecraftsolutions.database.executor.DatabaseExecutor;
import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.adapter.UserAdapter;
import com.minecraftsolutions.fishing.model.user.fishing.FishingType;
import lombok.AllArgsConstructor;
import com.minecraftsolutions.database.Database;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class UserRepository implements UserFoundationRepository {

    private final Database database;
    private final UserAdapter adapter = new UserAdapter();

    @Override
    public void setup() {

        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("CREATE TABLE IF NOT EXISTS fishing_user (nickname VARCHAR(16) PRIMARY KEY, fishingrod CHAR(36) NOT NULL, bucket CHAR(36) NOT NULL, hookedFish INTEGER, fishingTime INTEGER)")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS fishing_booster (nickname VARCHAR(16) REFERENCES fishing_user(nickname), booster CHAR(36), time INTEGER, PRIMARY KEY(nickname, booster))")
                    .write();
        }

    }

    @Override
    public void insert(User user) {
        try (DatabaseExecutor executor = database.execute()) {
            executor.query("INSERT INTO fishing_user VALUES(?,?,?,?,?)")
                    .write(statement -> {
                        statement.set(1, user.getNickname());
                        statement.set(2, user.getFishingRod().getUniqueId().toString());
                        statement.set(3, user.getBucket().getUniqueId().toString());
                        statement.set(4, user.getHookedFish());
                        statement.set(5, user.getFishingTime());
                    });
        }
    }

    @Override
    public void update(Collection<User> users) {
        CompletableFuture.runAsync(() -> {
            try (DatabaseExecutor executor = database.execute()) {
                executor
                        .query("UPDATE fishing_user SET hookedFish = ?, fishingTime = ? WHERE nickname = ?")
                        .batch(users, (user, statement) -> {
                            statement.set(1, user.getHookedFish());
                            statement.set(2, user.getFishingTime());
                            statement.set(3, user.getNickname());
                        });
            }
        }, FishingPlugin.getInstance().getExecutor());
    }

    @Override
    public void updateBoosters(User user) {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("INSERT INTO fishing_booster (nickname, booster, time) VALUES(?,?,?) ON DUPLICATE KEY UPDATE time = VALUES(time)")
                    .batch(user.getBoosters().entrySet(), (entry, statement) -> {
                        statement.set(1, user.getNickname());
                        statement.set(2, entry.getKey().getId());
                        statement.set(3, entry.getValue());
                    });
        }
    }

    @Override
    public void deleteBooster(User user, Booster booster) {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("DELETE FROM fishing_booster WHERE nickname = ? AND booster = ?")
                    .write(statement -> {
                        statement.set(1, user.getNickname());
                        statement.set(2, booster.getId());
                    });
        }
    }

    @Override
    public Set<User> getTopByTime() {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT * FROM fishing_user ORDER BY fishingTime DESC LIMIT 15")
                    .readMany(databaseQuery -> {
                        final String nickname = (String) databaseQuery.get("nickname");
                        final int hookedFish = (Integer) databaseQuery.get("hookedFish");
                        final int fishingTime = (Integer) databaseQuery.get("fishingTime");

                        return new User(nickname, null, null, hookedFish, fishingTime, FishingType.OUT, FishingRod.DEFAULT_TIME);
                    }, HashSet::new);
        }
    }

    @Override
    public Set<User> getTopByFishes() {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT * FROM fishing_user ORDER BY hookedFish DESC LIMIT 15")
                    .readMany(databaseQuery -> {
                        final String nickname = (String) databaseQuery.get("nickname");
                        final int hookedFish = (Integer) databaseQuery.get("hookedFish");
                        final int fishingTime = (Integer) databaseQuery.get("fishingTime");

                        return new User(nickname, null, null, hookedFish, fishingTime, FishingType.OUT, FishingRod.DEFAULT_TIME);
                    }, HashSet::new);
        }
    }

    @Override
    public User findOne(String nickname) {
        try (DatabaseExecutor executor = database.execute()) {
            return executor.query("SELECT * FROM fishing_user A LEFT JOIN fishing_booster B ON A.nickname = B.nickname WHERE A.nickname = ?")
                    .readOne(statement -> statement.set(1, nickname), this.adapter).orElse(null);
        }
    }

}
