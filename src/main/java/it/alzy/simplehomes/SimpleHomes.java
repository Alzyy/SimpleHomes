package it.alzy.simplehomes;

import it.alzy.simplehomes.commands.HomeCommand;
import it.alzy.simplehomes.configurations.LanguageConfiguration;
import it.alzy.simplehomes.configurations.SettingsConfiguration;
import it.alzy.simplehomes.events.PlayerListeners;
import it.alzy.simplehomes.records.Database;
import it.alzy.simplehomes.records.Home;
import it.alzy.simplehomes.storage.Cache;
import it.alzy.simplehomes.storage.IStorage;
import it.alzy.simplehomes.storage.impl.MySQLStorage;
import it.alzy.simplehomes.storage.impl.SQLiteStorage;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import co.aikar.commands.PaperCommandManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SimpleHomes extends JavaPlugin {

    @Getter
    private static SimpleHomes instance;

    @Getter
    private ExecutorService executor;

    @Getter
    private IStorage storage;

    @Getter
    private Cache cache;

    private PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configs
        loadConfigurations();

        // Initialize async thread pool
        executor = Executors.newFixedThreadPool(SettingsConfiguration.getInstance().getThreadPoolLimit());

        // Initialize cache
        cache = new Cache();

        //Initialize commands and events
        commandManager = new PaperCommandManager(this);
        registerCommands();
        registerEvents();

        // Initialize storage
        loadStorage();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }

        instance = null;
    }

    private void loadConfigurations() {
        LanguageConfiguration.getInstance().registerConfig();
        SettingsConfiguration.getInstance().registerConfig();
    }

    private void loadStorage() {
        String type = SettingsConfiguration.getInstance().getStorage().toLowerCase();

        switch (type) {
            case "sqlite" -> storage = new SQLiteStorage(this);
            case "mysql" -> {
                SettingsConfiguration config = SettingsConfiguration.getInstance();
                storage = new MySQLStorage(this, new Database(
                        config.getDBHost(),
                        config.getDBUsername(),
                        config.getDBPassword(),
                        config.getDBPort(),
                        config.getDBName(),
                        config.getDBPool()
                ));
            }
            default -> {
                getLogger().severe("Invalid storage type: " + type + ". Disabling plugin.");
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    private void registerCommands() {
        registerCompletitions();
        commandManager.registerCommand(new HomeCommand());
    }

    private void registerCompletitions() {
        commandManager.getCommandCompletions().registerAsyncCompletion("homes", c -> cache.get(c.getPlayer().getUniqueId()).stream().map(Home::homeName).collect(Collectors.toList()));
    }
}
