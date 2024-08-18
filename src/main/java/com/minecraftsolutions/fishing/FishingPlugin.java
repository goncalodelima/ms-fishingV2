package com.minecraftsolutions.fishing;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.database.DatabaseType;
import com.minecraftsolutions.database.connection.DatabaseConnection;
import com.minecraftsolutions.database.credentials.impl.DatabaseCredentialsImpl;
import com.minecraftsolutions.fishing.command.BoosterCommand;
import com.minecraftsolutions.fishing.command.FishingCommand;
import com.minecraftsolutions.fishing.controller.booster.BoosterController;
import com.minecraftsolutions.fishing.controller.user.UserController;
import com.minecraftsolutions.fishing.hook.EconomyHook;
import com.minecraftsolutions.fishing.hook.PlaceholderAPIHook;
import com.minecraftsolutions.fishing.listener.PlayerListener;
import com.minecraftsolutions.fishing.model.booster.loader.BoosterLoader;
import com.minecraftsolutions.fishing.model.booster.service.BoosterFoundationService;
import com.minecraftsolutions.fishing.model.booster.service.BoosterService;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.bucket.service.BucketFoundationService;
import com.minecraftsolutions.fishing.model.bucket.service.BucketService;
import com.minecraftsolutions.fishing.model.enchant.loader.EnchantLoader;
import com.minecraftsolutions.fishing.model.enchant.service.EnchantFoundationService;
import com.minecraftsolutions.fishing.model.enchant.service.EnchantService;
import com.minecraftsolutions.fishing.model.fish.loader.FishLoader;
import com.minecraftsolutions.fishing.model.fish.service.FishFoundationService;
import com.minecraftsolutions.fishing.model.fish.service.FishService;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.fishingrod.service.FishingRodFoundationService;
import com.minecraftsolutions.fishing.model.fishingrod.service.FishingRodService;
import com.minecraftsolutions.fishing.model.reward.loader.RewardLoader;
import com.minecraftsolutions.fishing.model.reward.service.RewardFoundationService;
import com.minecraftsolutions.fishing.model.reward.service.RewardService;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.service.UserFoundationService;
import com.minecraftsolutions.fishing.model.user.service.UserService;
import com.minecraftsolutions.fishing.util.Formatter;
import com.minecraftsolutions.fishing.util.configuration.Configuration;
import com.minecraftsolutions.fishing.view.*;
import lombok.Getter;
import me.devnatan.inventoryframework.ViewFrame;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Getter
public class FishingPlugin extends JavaPlugin {

    public static Formatter FORMATTER = new Formatter();

    private Configuration locations;
    private Configuration reward;
    private Configuration ranking;
    private Configuration bucketMenu;
    private Configuration fishingRodSlot;
    private Configuration fishingRodEnchants;
    private Configuration fishingRodManager;
    private Configuration fishingExit;
    private Configuration fishingJoin;
    private Configuration messages;
    private Configuration fish;
    private Configuration booster;
    private Configuration enchant;
    private Configuration fishingRod;
    private Configuration bucket;

    private RewardFoundationService rewardService;
    private FishFoundationService fishService;
    private BoosterFoundationService boosterService;
    private EnchantFoundationService enchantService;
    private FishingRodFoundationService fishingRodService;
    private BucketFoundationService bucketService;
    private UserFoundationService userService;

    private BoosterController boosterController;
    private UserController userController;

    private ViewFrame viewFrame;
    
    private Database datacenter;

    private EconomyHook economyHook;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Getter
    private static FishingPlugin instance;

    @Override
    public void onEnable() {

        instance = this;

        setupConfigs();

        setupServices();
        setupControllers();

        viewFrame = ViewFrame.create(this)
                        .with(new FishingJoinView(), new FishingExitView(), new FishingRodManagerView(),
                                new FishingRodEnchantsView(), new FishingRodSlotView(), new BucketView(fishService.getAll()), new RankingView())
                        .register();

        getCommand("booster").setExecutor(new BoosterCommand());
        getCommand("fishing").setExecutor(new FishingCommand());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new PlaceholderAPIHook().register();

        new UpdateRunnable().runTaskTimer(this,20 * getConfig().getLong("runnable", 36), 20 * getConfig().getLong("runnable", 36)); // feito para timed out de 30 segundos no mysql para evitar problemas de corrida, Margem de erro de 6 segundos

    }

    @Override
    public void onDisable() {

        Map<UUID, Bucket> buckets = FishingPlugin.getInstance().getBucketService().getPendingUpdates();
        Map<UUID, FishingRod> fishingRods = FishingPlugin.getInstance().getFishingRodService().getPendingUpdates();
        Map<String, User> users = FishingPlugin.getInstance().getUserService().getPendingUpdates();

        FishingPlugin.getInstance().getBucketService().update(buckets.values());
        FishingPlugin.getInstance().getFishingRodService().update(fishingRods.values());
        FishingPlugin.getInstance().getUserService().update(users.values());

        userService.getAll().forEach(userController::exitFishingArea);
        datacenter.close();
        executor.shutdown();

        try {

            if (!executor.awaitTermination(getConfig().getLong("runnable", 36), TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(getConfig().getLong("runnable", 36), TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate in the specified time.");
                }
            }

        } catch (InterruptedException ie) {
            executor.shutdownNow();
        }

    }

    private void setupConfigs(){

        saveDefaultConfig();
        reward = new Configuration("reward.yml", this);
        reward.saveFile();
        ranking = new Configuration("menu/ranking.yml", this);
        ranking.saveFile();
        bucketMenu = new Configuration("menu/bucket.yml", this);
        bucketMenu.saveFile();
        locations = new Configuration("locations.yml", this);
        locations.saveFile();
        fishingRodSlot = new Configuration("menu/fishingrod-slot.yml", this);
        fishingRodSlot.saveFile();
        fishingRodEnchants = new Configuration("menu/fishingrod-enchants.yml", this);
        fishingRodEnchants.saveFile();
        fishingRodManager = new Configuration("menu/fishingrod-manager.yml", this);
        fishingRodManager.saveFile();
        fishingExit = new Configuration("menu/fishing-exit.yml", this);
        fishingExit.saveFile();
        fishingJoin = new Configuration("menu/fishing-join.yml", this);
        fishingJoin.saveFile();
        messages = new Configuration("messages.yml", this);
        messages.saveFile();
        fish = new Configuration("fish.yml", this);
        fish.saveFile();
        booster = new Configuration("booster.yml", this);
        booster.saveFile();
        enchant = new Configuration("enchant.yml", this);
        enchant.saveFile();
        fishingRod = new Configuration("fishingrod.yml", this);
        fishingRod.saveFile();
        bucket = new Configuration("bucket.yml", this);
        bucket.saveFile();

    }

    private void setupServices(){

        datacenter = new DatabaseConnection(
                new DatabaseCredentialsImpl(
                        DatabaseType.valueOf(getConfig().getString("database.type")),
                        getConfig().getString("database.host"),
                        getConfig().getString("database.port"),
                        getConfig().getString("database.database"),
                        getConfig().getString("database.user"),
                        getConfig().getString("database.password"),
                        getConfig().getString("database.type")
                )
        ).setup();

        economyHook = new EconomyHook();

        rewardService = new RewardService();
        new RewardLoader().setup().forEach(rewardService::put);

        fishService = new FishService();
        new FishLoader().setup().forEach(fishService::put);

        boosterService = new BoosterService();
        new BoosterLoader().setup().forEach(boosterService::put);

        enchantService = new EnchantService();
        new EnchantLoader().setup().forEach(enchantService::put);

        fishingRodService = new FishingRodService(datacenter);

        bucketService = new BucketService(datacenter);

        userService = new UserService(datacenter);

    }

    private void setupControllers(){

        boosterController = new BoosterController();
        userController = new UserController();

    }

}
