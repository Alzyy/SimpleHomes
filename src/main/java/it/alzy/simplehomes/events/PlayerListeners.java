package it.alzy.simplehomes.events;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import it.alzy.simplehomes.SimpleHomes;

public class PlayerListeners implements Listener {

    private final SimpleHomes plugin = SimpleHomes.getInstance();

    @EventHandler
    public void asyncPlayerJoin(AsyncPlayerPreLoginEvent ev) {
        final UUID uuid = ev.getPlayerProfile().getId();

        plugin.getLogger().info(String.format("Loading player %s into cache ", ev.getPlayerProfile().getName()));
        plugin.getStorage().load(uuid);
    }
}
