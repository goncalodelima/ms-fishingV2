package com.minecraftsolutions.fishing.model.user.service;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.model.bucket.service.BucketFoundationService;
import com.minecraftsolutions.fishing.model.fishingrod.service.FishingRodFoundationService;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.repository.UserFoundationRepository;
import com.minecraftsolutions.fishing.model.user.repository.UserRepository;
import com.minecraftsolutions.database.Database;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserService implements UserFoundationService{

    private final FishingRodFoundationService fishingRodService = FishingPlugin.getInstance().getFishingRodService();
    private final BucketFoundationService bucketService = FishingPlugin.getInstance().getBucketService();
    private final Map<String, User> cache = new ConcurrentHashMap<>();
    private final Map<String, User> pendingUpdates = new HashMap<>();
    private final UserFoundationRepository userRepository;

    public UserService(Database database){
        this.userRepository = new UserRepository(database);
        this.userRepository.setup();
    }

    @Override
    public void put(User user) {
        this.cache.put(user.getNickname(), user);
    }

    @Override
    public void putData(User user) {
        this.userRepository.insert(user);
    }

    @Override
    public void update(User user) {
        this.pendingUpdates.put(user.getNickname(), user);
    }

    @Override
    public CompletableFuture<Void> update(Collection<User> users) {
        return this.userRepository.update(users);
    }

    @Override
    public void updateBoosters(User user){
        this.userRepository.updateBoosters(user);
    }

    @Override
    public List<User> getTopByFishes() {
        return new ArrayList<>(this.userRepository.getTopByFishes());
    }

    @Override
    public List<User> getTopByTime() {
        return new ArrayList<>(this.userRepository.getTopByTime());
    }

    @Override
    public void remove(User user) {
        this.bucketService.remove(user.getBucket());
        this.fishingRodService.remove(user.getFishingRod());
        this.cache.remove(user.getNickname());
    }

    @Override
    public void removeBooster(User user, Booster booster) {
        user.removeBooster(booster);
        userRepository.deleteBooster(user, booster);
    }

    @Override
    public Optional<User> get(String nickname) {
        User user = this.cache.get(nickname);
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> getAll() {
        return this.cache
                .keySet()
                .stream()
                .map(this::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, User> getPendingUpdates() {
        return pendingUpdates;
    }

}
