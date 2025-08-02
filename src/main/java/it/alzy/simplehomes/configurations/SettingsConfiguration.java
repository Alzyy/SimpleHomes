package it.alzy.simplehomes.configurations;

import it.alzy.simplehomes.SimpleHomes;
import net.pino.simpleconfig.LightConfig;
import net.pino.simpleconfig.annotations.Config;
import net.pino.simpleconfig.annotations.ConfigFile;

@Config
@ConfigFile("config.yml")
public class SettingsConfiguration extends LightConfig {
    private static SettingsConfiguration instance = null;

    public SettingsConfiguration() {}


    public int getThreadPoolLimit() {
        return this.fileConfiguration.getInt("settings.max-thread-pool", 2);
    }
    
    public String getStorage() {
        return this.fileConfiguration.getString("settings.storage-type", "sqlite");
    }

    public String getDBHost() {
        return this.fileConfiguration.getString("database.host", "localhost");
    }

    public String getDBUsername() {
        return this.fileConfiguration.getString("database.username", "root");
    }

    public String getDBPassword() {
        return this.fileConfiguration.getString("database.password", "CHANGEME");
    }

    public String getDBName() {
        return this.fileConfiguration.getString("database.database", "simplehomes");
    }
    
    public int getDBPort() {
        return this.fileConfiguration.getInt("database.port", 3306);
    }

    public int getDBPool() {
        return this.fileConfiguration.getInt("database.max-pool", 10);
    }

    public static SettingsConfiguration getInstance() {
        if(instance == null) instance = new SettingsConfiguration();
        return instance;
    }

    public void registerConfig() {
        registerLightConfig(SimpleHomes.getInstance());
    }
}
