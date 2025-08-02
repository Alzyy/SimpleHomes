package it.alzy.simplehomes.configurations;

import it.alzy.simplehomes.SimpleHomes;
import net.pino.simpleconfig.BaseConfig;
import net.pino.simpleconfig.annotations.Config;
import net.pino.simpleconfig.annotations.ConfigFile;

@Config
@ConfigFile("lang.yml")
public class LanguageConfiguration extends BaseConfig {
    private static LanguageConfiguration instance = null;


    public LanguageConfiguration() {}

    public static LanguageConfiguration getInstance() {
        if(instance == null) instance = new LanguageConfiguration();
        return instance;
    }

    public void registerConfig() {
        registerConfig(SimpleHomes.getInstance());
    }
}
