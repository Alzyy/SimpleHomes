package it.alzy.simplehomes.events;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import it.alzy.simplehomes.SimpleHomes;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {

    private final SimpleHomes plugin = SimpleHomes.getInstance();

    @EventHandler
    public void asyncPlayerJoin(AsyncPlayerPreLoginEvent ev) {
        final UUID uuid = ev.getUniqueId();

        plugin.getLogger().info(String.format("Loading player %s into cache ", ev.getName()));
        plugin.getStorage().load(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        plugin.getCache().remove(ev.getPlayer().getUniqueId());
    }
}
