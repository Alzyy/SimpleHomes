package it.alzy.simplehomes.storage.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.alzy.simplehomes.SimpleHomes;
import it.alzy.simplehomes.records.Home;
import it.alzy.simplehomes.storage.IStorage;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class SQLiteStorage implements IStorage {

    private final SimpleHomes plugin;
    private final ExecutorService executor;
    private final HikariDataSource dataSource;

    public SQLiteStorage(SimpleHomes plugin) {
        this.plugin = plugin;
        this.executor = plugin.getExecutor();

        File dbFile = new File(plugin.getDataFolder(), "Players.db");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getPath());
        config.setMaximumPoolSize(10);
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("SimpleHomes-SQLite-Pool");
        config.setConnectionTimeout(5000);
        this.dataSource = new HikariDataSource(config);

        init();
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void init() {
        plugin.getLogger().info("Using SQLite as storage system");
        executor.execute(() -> {
            String query = """
                    CREATE TABLE IF NOT EXISTS users (
                        home_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        uuid TEXT NOT NULL,
                        homeName TEXT NOT NULL,
                        location TEXT NOT NULL
                    )
                    """;
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute(query);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to create users table: " + e.getMessage());
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        });
    }

    @Override
    public void createHome(UUID ownerID, Home home) {
        executor.execute(() -> {
            String query = "INSERT INTO users(uuid, homeName, location) VALUES (?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, ownerID.toString());
                ps.setString(2, home.homeName());
                ps.setString(3, home.serialize());
                ps.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save home for " + ownerID + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void deleteHome(UUID uuid, String homeName) {
        executor.execute(() -> {
            String query = "DELETE FROM users WHERE uuid = ? AND homeName = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, uuid.toString());
                ps.setString(2, homeName);
                ps.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete home '" + homeName + "' for " + uuid + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void load(UUID uuid) {
        executor.execute(() -> {
            List<Home> homes = new ArrayList<>();
            String query = "SELECT homeName, location FROM users WHERE uuid = ?";

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, uuid.toString());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Home home = Home.deserialize(rs.getString("homeName"), rs.getString("location"));
                        if (home != null) homes.add(home);
                    }
                }

                plugin.getCache().put(uuid, homes);

            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load homes for " + uuid + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void close() {
        plugin.getLogger().info("Shutting down SQLite connection pool");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
