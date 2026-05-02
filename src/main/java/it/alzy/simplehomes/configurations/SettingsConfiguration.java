package it.alzy.simplehomes.configurations;

import java.util.List;

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

    public boolean checkForUpdates() {
        return this.fileConfiguration.getBoolean("settings.check-for-update", true);
    }

    public int cooldown() {
        return this.fileConfiguration.getInt("settings.cooldown", 3);
    }

    public String getGuiName() {
        return this.fileConfiguration.getString("gui-settings.gui-title", "&e%player%'s Homes");
    }

    public String getBaseHeadEarth() {
        return this.fileConfiguration.getString("gui-settings.overworld-head", "");
    }
    public String getBaseHeadNether() {
        return this.fileConfiguration.getString("gui-settings.nether-head", "");
    }

    public String getBaseHeadEnd() {
        return this.fileConfiguration.getString("gui-settings.end-head", "");
    }

    public String getHeadName() {
        return this.fileConfiguration.getString("gui-settings.head.name", "&aHome &7(%homeName%)");
    }

    public String getGuiFillerMaterial() {
        return this.fileConfiguration.getString("gui-settings.gui-filler.material", "BLACK_STAINED_GLASS_PANE");
    }

    public String getGuiFillerName() {
        return this.fileConfiguration.getString("gui-settings.gui-filler.name", "&7 ");
    }

    public List<String> getHeadLore() {
        return this.fileConfiguration.getStringList("gui-settings.head.lore");
    }

    public String getSetName() {
        return this.fileConfiguration.getString("gui-settings.set-home.name", "&#101010Set Home");
    }

    public String getSetMaterial() {
        return this.fileConfiguration.getString("gui-settings.set-home.material", "LIME_BED");
    }

    public List<String> getSetLore() {
        return this.fileConfiguration.getStringList("gui-settings.set-home.lore");
    }

    public String getNextMaterial() {
        return this.fileConfiguration.getString("gui-settings.next-item.material", "ARROW");
    }
    public String getNextName() {
        return this.fileConfiguration.getString("gui-settings.next-item.name", "&aNext");
    }
    public String getBackMaterial() {
        return this.fileConfiguration.getString("gui-settings.back-item.material", "ARROW");
    }
    public String getBackName() {
        return this.fileConfiguration.getString("gui-settings.back-item.name", "&cBack");
    }
    public int getGuiRows() {
        return this.fileConfiguration.getInt("gui-settings.rows", 6);
    }

    public static SettingsConfiguration getInstance() {
        if(instance == null) instance = new SettingsConfiguration();
        return instance;
    }

    public void registerConfig() {
        registerLightConfig(SimpleHomes.getInstance());
    }

}
