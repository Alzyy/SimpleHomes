package it.alzy.simplehomes.storage;

import java.util.UUID;

import it.alzy.simplehomes.records.Home;

public interface IStorage {
     
    void load(UUID uuid);
    void createHome(UUID uuid, Home home);
    void deleteHome(UUID uuid, String homeName);
    void close();
}