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
    public String PREFIX = "&#ffcc00ѕɪᴍᴘʟᴇʜᴏᴍᴇѕ &8» ";

    @Path("messages.home.created")
    public String HOME_CREATED = "%prefix%&fYou've set a home &7(%home%)";

    @Path("messages.home.deleted")
    public String HOME_DELETED = "%prefix%&#ff4d4dYou've deleted a home &7(%home%)";

    @Path("messages.home.teleported")
    public String HOME_TELEPORTED = "%prefix%&#ffd966You've teleported to your home &f%home%";

    @Path("messages.home.already-set")
    public String HOME_ALREADY_SET = "%prefix%&#ffaa00You've already a home called %home%!";

    @Path("messages.home.reloaded")
    public String HOME_RELOADED = "%prefix%&#a3cf62You've reloaded the configurations!";

    @Path("messages.home.cooldown")
    public String HOME_COOLDOWN = "%prefix%&#ff9966You must wait %cooldown% seconds before using this command again.";

    @Path("messages.home.ask-name")
    public String HOME_ASK_NAME = "%prefix%&#ffcc00Write the home &f&lNAME &#ffcc00in chat (or type &c'cancel'&7)";

    @Path("messages.home.input-cancelled")
    public String HOME_INPUT_CANCELLED = "%prefix%&cCreation cancelled.";

    @Path("messages.home.invalid-name")
    public String HOME_INVALID_NAME = "%prefix%&#ff4d4dInvalid name! Use only alphanumeric characters.";

    public static LanguageConfiguration getInstance() {
        if(instance == null) instance = new LanguageConfiguration();
        return instance;
    }

    public void registerConfig() {
        registerConfig(SimpleHomes.getInstance());
    }
}