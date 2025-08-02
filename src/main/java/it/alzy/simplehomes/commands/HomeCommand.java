package it.alzy.simplehomes.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import it.alzy.simplehomes.SimpleHomes;
import it.alzy.simplehomes.configurations.LanguageConfiguration;
import it.alzy.simplehomes.records.Home;
import it.alzy.simplehomes.utils.ChatUtils;
import it.alzy.simplehomes.utils.PermissionUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;

@CommandAlias("home|homes")
@Description("Main command")
public class HomeCommand extends BaseCommand {
    
    private final SimpleHomes plugin = SimpleHomes.getInstance();
    private final LanguageConfiguration lang = LanguageConfiguration.getInstance();

    @Default
    public void root(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gradient:#22C55E:#16A34A><bold>✔ SimpleHomes</bold></gradient> <gray>| Version </gray><white>"
                + plugin.getPluginMeta().getVersion() + "</white>\n"
                + "<gray>Developed with ❤ by </gray>"
                + "<hover:show_text:'Click to view my profile!'><click:open_url:'https://www.spigotmc.org/members/alzyit.1581572/'>"
                + "<gradient:#A1A1AA:#71717A><bold>AlzyIT</bold></gradient></click></hover>"));
    }

    @Default
    @CommandCompletion("@homes")
    public void withHomeName(Player player, String homeName) {
        plugin.getExecutor().execute(() -> {
            List<Home> homes = plugin.getCache().get(player.getUniqueId());
            Home targetHome = homes.stream().filter(home -> home.homeName().equalsIgnoreCase(homeName))
            .findFirst()
            .orElse(null);
            Bukkit.getScheduler().runTask(plugin,() -> {
                if(targetHome != null) {
                    player.teleport(targetHome.toLocation());
                    ChatUtils.send(player, lang.HOME_TELEPORTED, "%prefix%", lang.PREFIX, "%home%", homeName);
                } else {
                    ChatUtils.send(player, lang.HOME_UNKNOWN, "%prefix%", lang.PREFIX, "%home%", homeName);
                }
            });
        });

    }
    @CommandAlias("sethome|homeset")
    public void setHome(Player player, String name) {
        plugin.getExecutor().execute(() -> {
            List<Home> homes = plugin.getCache().get(player.getUniqueId());
            if(homes.stream().anyMatch(home -> home.homeName().equalsIgnoreCase(name))) {
                Bukkit.getScheduler().runTask(plugin, () -> ChatUtils.send(player, lang.HOME_ALREADY_SET, "%prefix%", lang.PREFIX, "%home%", name));
                return;
            }
            int limit = PermissionUtils.getHomeLimit(player);
            if(homes.size() >= limit) {
                Bukkit.getScheduler().runTask(plugin, () -> ChatUtils.send(player, lang.HOME_LIMIT, "%prefix%", lang.PREFIX, "%limit%", limit));
                return;
            }
            Home home = Home.fromLocation(name, player.getLocation());
            homes.add(home);
            plugin.getStorage().createHome(player.getUniqueId(), home);
            plugin.getCache().put(player.getUniqueId(), homes);
            Bukkit.getScheduler().runTask(plugin, () -> ChatUtils.send(player, lang.HOME_CREATED, "%prefix%", lang.PREFIX, "%home%", name));
        });
    }

    @CommandAlias("delhome|homedel")
    @CommandCompletion("@homes")
    public void delHome(Player player, String name) {
        plugin.getExecutor().execute(() -> {
            List<Home> homes = plugin.getCache().get(player.getUniqueId());
            if(homes.stream().anyMatch(home -> home.homeName().equalsIgnoreCase(name))) {
                homes.removeIf(home -> home.homeName().equalsIgnoreCase(name));
                plugin.getStorage().deleteHome(player.getUniqueId(), name);
                plugin.getCache().put(player.getUniqueId(), homes);

                Bukkit.getScheduler().runTask(plugin, () -> ChatUtils.send(player, lang.HOME_DELETED, "%prefix%", lang.PREFIX, "%home%", name));
                return;
            } else {
                ChatUtils.send(player, lang.HOME_UNKNOWN, "%prefix%", lang.PREFIX, "%home%", name);
                return;
            }
        });
    }
}
