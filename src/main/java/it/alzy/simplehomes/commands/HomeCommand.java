package it.alzy.simplehomes.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.builder.item.SkullBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import it.alzy.simplehomes.SimpleHomes;
import it.alzy.simplehomes.configurations.LanguageConfiguration;
import it.alzy.simplehomes.configurations.SettingsConfiguration;
import it.alzy.simplehomes.records.Home;
import it.alzy.simplehomes.utils.ChatUtils;
import it.alzy.simplehomes.utils.PermissionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;


@CommandAlias("home|homes")
@Description("Main command")
public class HomeCommand extends BaseCommand {
    
    private final SimpleHomes plugin = SimpleHomes.getInstance();
    private final LanguageConfiguration lang = LanguageConfiguration.getInstance();
    private final SettingsConfiguration config = SettingsConfiguration.getInstance();

    @Default
    public void root(Player player) {
        plugin.getExecutor().execute(() -> {
            Gui gui = Gui.gui()
                .title(ChatUtils.createComponent(config.getGuiName(), "%player%", player.getName()))
                .rows(5)
                .create();
            plugin.getCache().get(player.getUniqueId())
                .stream()
                .map(this::generateHead)
                .map(GuiItem::new)
                .forEach(item -> {
                    gui.addItem(item);
                    item.setAction(event -> {
                        switch(event.getAction()) {
                            case PICKUP_ALL: {
                                Player clicker = (Player) event.getWhoClicked();
                                clicker.performCommand("home "+ item.getItemStack().getItemMeta().getPersistentDataContainer().get(plugin.getHomeKey(), PersistentDataType.STRING));
                                event.setCancelled(true);
                                event.getClickedInventory().close();
                                return;
                            }
                            case PICKUP_HALF: {
                                Player clicker = (Player) event.getWhoClicked();
                                clicker.performCommand("delhome "+ item.getItemStack().getItemMeta().getPersistentDataContainer().get(plugin.getHomeKey(), PersistentDataType.STRING));
                                event.setCancelled(true);
                                event.getClickedInventory().close();
                                return;
                            }
                            default: {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    });
                });

            Bukkit.getScheduler().runTask(plugin, () -> gui.open(player));
        }); 
    }


    @Subcommand("credits")
    public void credits(Player player) {
        player.sendActionBar(Component.text(player.getWorld().getEnvironment().name()));
        player.sendMessage(MiniMessage.miniMessage().deserialize(
            "<bold><gradient:#7DD3FC:#38BDF8>❖ SimpleHomes</gradient></bold> <gray>| Version </gray><white>"
            + plugin.getPluginMeta().getVersion() + "</white>\n"
            + "<gray>Developed with </gray><red>❤</red><gray> by </gray>"
            + "<hover:show_text:'Click to view my Spigot profile!'>"
            + "<click:open_url:'https://www.spigotmc.org/members/alzyit.1581572/'>"
            + "<bold><gradient:#C084FC:#9333EA>AlzyIT</gradient></bold></click></hover>"
        ));
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

    @CommandAlias("reload")
    @CommandPermission("simplehomes.command.reload")
    public void reload(Player player) {
        SimpleHomes.getInstance().reloadConfigurations();
        ChatUtils.send(player, lang.HOME_RELOADED, "%prefix%", lang.PREFIX);
    }

    private ItemStack generateHead(Home home) {
        String texture = switch (home.toLocation().getWorld().getEnvironment().name()) {
            case "NORMAL" -> config.getBaseHeadEarth();
            case "NETHER" -> config.getBaseHeadNether();
            case "THE_END" -> config.getBaseHeadEnd();
            default -> null;
        };

        if(texture == null) return new ItemStack(Material.AIR);
        SkullBuilder item = ItemBuilder.from(Material.PLAYER_HEAD).skull();
        SkullBuilder builtItem = createMeta(item, home);
        builtItem.texture(texture);
        return builtItem.build();
    }


    private SkullBuilder createMeta(SkullBuilder item, Home home) {
        Location loc = home.toLocation();
        item.name(ChatUtils.createComponent(config.getHeadName(), "%homeName%", home.homeName()));
        List<Component> lore = config.getHeadLore().stream()
        .map(line -> ChatUtils.createComponent(line, 
            "%x%", Math.floor(loc.getX()),
                "%y%", Math.floor(loc.getY()),
                "%z%", Math.floor(loc.getZ()),
                "%world%", loc.getWorld().getName(),
                "%worldEnvironment%", loc.getWorld().getEnvironment().name()
            )).collect(Collectors.toList());

        item.pdc(pdc -> pdc.set(plugin.getHomeKey(), PersistentDataType.STRING, home.homeName()));
        item.lore(lore);
        return item;
    }
}
