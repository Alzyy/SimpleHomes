package it.alzy.simplehomes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.plugin.java.JavaPlugin;

import it.alzy.simplehomes.configurations.LanguageConfiguration;
import it.alzy.simplehomes.configurations.SettingsConfiguration;
import it.alzy.simplehomes.records.Database;
import it.alzy.simplehomes.storage.Cache;
import it.alzy.simplehomes.storage.IStorage;
import it.alzy.simplehomes.storage.impl.MySQLStorage;
import it.alzy.simplehomes.storage.impl.SQLiteStorage;
import lombok.Getter;

public class SimpleHomes extends JavaPlugin {


    @Getter
    private static SimpleHomes instance;

    @Getter
    private ExecutorService executor;

    @Getter
    private IStorage storage;

    @Getter
    private Cache cache;

    @Override
    public void onEnable() {
        instance = this;

        //Initialize configurations
        loadConfigurations();
        loadStorage();

        //Create thread pool for async operations
        executor = Executors.newFixedThreadPool(SettingsConfiguration.getInstance().getThreadPoolLimit());

        //Load utils
        cache = new Cache();
    }


    @Override
    public void onDisable() {
        instance = null;
    }

    private void loadStorage() {
        String storageStr = SettingsConfiguration.getInstance().getStorage();
        switch (storageStr.toLowerCase()) {
            case "sqlite":
                storage = new SQLiteStorage(this);
                break;
            case "mysql":
                SettingsConfiguration config = SettingsConfiguration.getInstance();
                var info = new Database(
                    config.getDBHost(),
                    config.getDBUsername(),
                    config.getDBPassword(),
                    config.getDBPort(),
                    config.getDBName(),
                    config.getDBPool()
                );
                storage = new MySQLStorage(this, info);
                break;
            default:
                getLogger().severe("Invalid system storage type!\nDisabling plugin!");
                getServer().getPluginManager().disablePlugin(this);
                break;
        }
    }

    private void loadConfigurations() {
        LanguageConfiguration.getInstance().registerConfig();
        SettingsConfiguration.getInstance().registerConfig();
    }
    
}
