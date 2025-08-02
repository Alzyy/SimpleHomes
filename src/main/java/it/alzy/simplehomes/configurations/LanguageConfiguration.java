package it.alzy.simplehomes.configurations;

import it.alzy.simplehomes.SimpleHomes;
import net.pino.simpleconfig.BaseConfig;
import net.pino.simpleconfig.annotations.Config;
import net.pino.simpleconfig.annotations.ConfigFile;
import net.pino.simpleconfig.annotations.inside.Path;

@Config
@ConfigFile("lang.yml")
public class LanguageConfiguration extends BaseConfig {
    private static LanguageConfiguration instance = null;


    public LanguageConfiguration() {}

    @Path("prefix")
    public String PREFIX = "&#3b396eѕɪᴍᴘʟᴇʜᴏᴍᴇѕ &8| ";
    @Path("messages.home.created")
    public String HOME_CREATED = "%prefix%&#86f0d9You've set a home &7(%home%)";
    @Path("messages.home.deleted")
    public String HOME_DELETED = "%prefix%&#88a865You've deleted a home &7(%home%)";
    @Path("messages.home.limit-reached")
    public String HOME_LIMIT = "%prefix%&#8b5ba5You've reached your limit of %limit% homes";
    @Path("messages.home.teleported")
    public String HOME_TELEPORTED = "%prefix%&#3fcb4aYou've teleported to your home %home%";
    @Path("message.home.unknown")
    public String HOME_UNKNOWN = "%prefix%&#ab85e1Couldn't find a home called %home%";
    @Path("message.home.already-set")
    public String HOME_ALREADY_SET = "%prefix%&#ab6bceYou've already a home called %home%!";

    public static LanguageConfiguration getInstance() {
        if(instance == null) instance = new LanguageConfiguration();
        return instance;
    }

    public void registerConfig() {
        registerConfig(SimpleHomes.getInstance());
    }
}
