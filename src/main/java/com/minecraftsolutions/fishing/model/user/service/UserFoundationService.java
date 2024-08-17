package com.minecraftsolutions.fishing.model.user.service;

import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.model.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserFoundationService {

    void put(User user);

    void putData(User user);

    void update(User user);

    void update(Collection<User> users);

    void updateBoosters(User user);

    void remove(User user);

    void removeBooster(User user, Booster booster);

    List<User> getTopByTime();

    List<User> getTopByFishes();

    Optional<User> get(String nickname);

    List<User> getAll();

    Map<String, User> getPendingUpdates();

}
