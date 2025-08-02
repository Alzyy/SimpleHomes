package it.alzy.simplehomes.storage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import it.alzy.simplehomes.records.Home;

public class Cache {

    private final ConcurrentHashMap<UUID, List<Home>> cacheMap = new ConcurrentHashMap<>();

    public Cache() {

    }

    public void put(UUID uuid, List<Home> homes) {
        cacheMap.put(uuid, homes);
    }

    public List<Home> get(UUID uuid) {
        return cacheMap.get(uuid);
    }
}
