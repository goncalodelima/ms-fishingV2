package com.minecraftsolutions.fishing.model.user.repository;

import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.model.user.User;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationRepository {

    void setup();

    void insert(User user);

    CompletableFuture<Void> update(Collection<User> users);

    void updateBoosters(User user);

    void deleteBooster(User user, Booster booster);

    Set<User> getTopByTime();

    Set<User> getTopByFishes();

    User findOne(String nickname);

}
