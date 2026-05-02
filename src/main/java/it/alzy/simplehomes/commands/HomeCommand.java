package it.alzy.simplehomes.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.builder.item.SkullBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.papermc.paper.event.player.AsyncChatEvent;
import it.alzy.simplehomes.SimpleHomes;
import it.alzy.simplehomes.configurations.LanguageConfiguration;
import it.alzy.simplehomes.configurations.SettingsConfiguration;
import it.alzy.simplehomes.records.Home;
import it.alzy.simplehomes.utils.ChatUtils;
import it.alzy.simplehomes.utils.PermissionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@CommandAlias("home|homes")
public class HomeCommand extends BaseCommand implements Listener {

    private final SimpleHomes plugin = SimpleHomes.getInstance();
    private final LanguageConfiguration lang = LanguageConfiguration.getInstance();
    private final SettingsConfiguration config = SettingsConfiguration.getInstance();
    private final Map<Player, Long> cooldownMap = new ConcurrentHashMap<>();

    private final Set<UUID> awaitingInput = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public HomeCommand() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Default
    public void root(Player player) {
        if (onCooldown(player)) {
            ChatUtils.send(player, lang.HOME_COOLDOWN, "%prefix%", lang.PREFIX, "%cooldown%", config.cooldown());
            return;
        }
        openHomeGui(player);
    }

    private void openHomeGui(Player p) {
        List<Home> homesRaw = plugin.getCache().get(p.getUniqueId());
        List<Home> homes = (homesRaw != null) ? new ArrayList<>(homesRaw) : new ArrayList<>();

        int currentHomeCount = homes.size();
        int limit = PermissionUtils.getHomeLimit(p);
        int rows = Math.min(6, Math.max(2, config.getGuiRows()));

        Bukkit.getScheduler().runTask(plugin, () -> {
            PaginatedGui gui = createPaginatedGui(p, rows);

            for (Home home : homes) {
                GuiItem item = new GuiItem(generateHead(home));
                item.setAction(event -> {
                    event.setCancelled(true);
                    String homeName = item.getItemStack().getItemMeta()
                            .getPersistentDataContainer().get(plugin.getHomeKey(), PersistentDataType.STRING);

                    if (homeName == null) return;

                    if (event.getAction() == InventoryAction.PICKUP_ALL) {
                        p.closeInventory();
                        teleportToHome(p, homeName);
                    } else if (event.getAction() == InventoryAction.PICKUP_HALF) {
                        deleteHome(p, homeName);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> openHomeGui(p), 1L);
                    }
                });
                gui.addItem(item);
            }

            int available = limit - currentHomeCount;
            for (int i = 0; i < available; i++) {
                int homeNumber = currentHomeCount + i + 1;

                GuiItem setHomeBed = ItemBuilder.from(Material.valueOf(config.getSetMaterial()))
                        .name(ChatUtils.createComponent(config.getSetName()))
                        .lore(config.getSetLore().stream()
                                .map(line -> ChatUtils.createComponent(line, "%n%", homeNumber))
                                .toList())
                        .asGuiItem(event -> {
                            event.setCancelled(true);
                            p.closeInventory();
                            awaitingInput.add(p.getUniqueId());
                            ChatUtils.send(p, lang.HOME_ASK_NAME, "%prefix%", lang.PREFIX);
                        });

                gui.addItem(setHomeBed);
            }

            gui.open(p);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!awaitingInput.contains(player.getUniqueId())) return;

        event.setCancelled(true);
        awaitingInput.remove(player.getUniqueId());


        String input = event.getMessage();
        if (input.equalsIgnoreCase("cancel")) {
            ChatUtils.send(player, lang.HOME_INPUT_CANCELLED, "%prefix%", lang.PREFIX);
            return;
        }

        String finalName = input.split(" ")[0].replaceAll("[^a-zA-Z0-9_-]", "");

        if (finalName.isEmpty()) {
            ChatUtils.send(player, lang.HOME_INVALID_NAME, "%prefix%", lang.PREFIX);
            return;
        }

        createNewHome(player, finalName);
    }


    private void createNewHome(Player player, String name) {
        plugin.getExecutor().execute(() -> {
            List<Home> homes = plugin.getCache().get(player.getUniqueId());

            if (homes.stream().anyMatch(h -> h.homeName().equalsIgnoreCase(name))) {
                Bukkit.getScheduler().runTask(plugin, () -> ChatUtils.send(player, lang.HOME_ALREADY_SET, "%prefix%", lang.PREFIX, "%home%", name));
                return;
            }

            Home home = Home.fromLocation(name, player.getLocation());
            homes.add(home);
            plugin.getStorage().createHome(player.getUniqueId(), home);
            plugin.getCache().put(player.getUniqueId(), homes);

            Bukkit.getScheduler().runTask(plugin, () -> {
                ChatUtils.send(player, lang.HOME_CREATED, "%prefix%", lang.PREFIX, "%home%", name);
                openHomeGui(player);
            });
        });
    }

    private void deleteHome(Player player, String name) {
        plugin.getExecutor().execute(() -> {
            List<Home> homes = plugin.getCache().get(player.getUniqueId());
            if (homes.removeIf(h -> h.homeName().equalsIgnoreCase(name))) {
                plugin.getStorage().deleteHome(player.getUniqueId(), name);
                plugin.getCache().put(player.getUniqueId(), homes);
                Bukkit.getScheduler().runTask(plugin, () -> ChatUtils.send(player, lang.HOME_DELETED, "%prefix%", lang.PREFIX, "%home%", name));
            }
        });
    }

    private void teleportToHome(Player player, String name) {
        List<Home> homes = plugin.getCache().get(player.getUniqueId());
        homes.stream().filter(h -> h.homeName().equalsIgnoreCase(name)).findFirst().ifPresent(home -> Bukkit.getScheduler().runTask(plugin, () -> {
            player.teleport(home.toLocation());
            ChatUtils.send(player, lang.HOME_TELEPORTED, "%prefix%", lang.PREFIX, "%home%", name);
        }));
    }

    private PaginatedGui createPaginatedGui(Player player, int rows) {
        int pageSize = (rows - 1) * 9;

        PaginatedGui gui = Gui.paginated()
                .title(ChatUtils.createComponent(config.getGuiName(), "%player%", player.getName()))
                .rows(rows)
                .pageSize(pageSize)
                .create();

        GuiItem filler = ItemBuilder.from(Material.valueOf(config.getGuiFillerMaterial()))
                .name(ChatUtils.createComponent(config.getGuiFillerName()))
                .asGuiItem(event -> event.setCancelled(true));

        gui.getFiller().fillBottom(filler);

        GuiItem next = ItemBuilder.from(Material.valueOf(config.getNextMaterial())).name(ChatUtils.createComponent(config.getNextName())).asGuiItem(e -> { e.setCancelled(true); gui.next(); });
        GuiItem prev = ItemBuilder.from(Material.valueOf(config.getBackMaterial())).name(ChatUtils.createComponent(config.getBackName())).asGuiItem(e -> { e.setCancelled(true); gui.previous(); });

        gui.setItem(rows, 1, prev);
        gui.setItem(rows, 9, next);

        return gui;
    }

    private boolean onCooldown(Player p) {
        if (p.hasPermission("simplehomes.bypass.cooldown")) return false;
        long now = System.currentTimeMillis();
        long lastUse = cooldownMap.getOrDefault(p, 0L);
        if (now - lastUse >= config.cooldown() * 1000L) {
            cooldownMap.put(p, now);
            return false;
        }
        return true;
    }

    private ItemStack generateHead(Home home) {
        Location loc = home.toLocation();
        String texture = switch (loc.getWorld().getEnvironment()) {
            case NETHER -> config.getBaseHeadNether();
            case THE_END -> config.getBaseHeadEnd();
            default -> config.getBaseHeadEarth();
        };
        return createMeta(ItemBuilder.from(Material.PLAYER_HEAD).skull(), home).texture(texture).build();
    }

    private SkullBuilder createMeta(SkullBuilder item, Home home) {
        Location loc = home.toLocation();
        item.name(ChatUtils.createComponent(config.getHeadName(), "%homeName%", home.homeName()));
        item.lore(config.getHeadLore().stream().map(line -> ChatUtils.createComponent(line, "%x%", loc.getBlockX(), "%y%", loc.getBlockY(), "%z%", loc.getBlockZ(), "%world%", loc.getWorld().getName(), "%worldEnvironment%", loc.getWorld().getEnvironment().name())).toList());
        item.pdc(pdc -> pdc.set(plugin.getHomeKey(), PersistentDataType.STRING, home.homeName()));
        return item;
    }


    @Subcommand("reload")
    @CommandPermission("simplehomes.admin")
    public void reloadSubCommand(Player player) {
        plugin.reloadConfigurations();
        ChatUtils.send(player, lang.HOME_RELOADED, "%prefix%", lang.PREFIX);
    }
}