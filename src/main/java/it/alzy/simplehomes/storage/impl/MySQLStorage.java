package it.alzy.simplehomes.storage.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.alzy.simplehomes.SimpleHomes;
import it.alzy.simplehomes.records.Database;
import it.alzy.simplehomes.records.Home;
import it.alzy.simplehomes.storage.IStorage;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class MySQLStorage implements IStorage {

    private final SimpleHomes plugin;
    private final ExecutorService executor;
    private final HikariDataSource dataSource;

    public MySQLStorage(SimpleHomes plugin, Database info) {
        this.plugin = plugin;
        this.executor = plugin.getExecutor();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(info.getJDBC());
        config.setUsername(info.username());
        config.setPassword(info.password());
        config.setMaximumPoolSize(info.maxPool());
        config.setConnectionTestQuery("SELECT 1");
        config.setConnectionTimeout(5000);
        config.setPoolName("SimpleHomes-MySQL-Pool");

        this.dataSource = new HikariDataSource(config);
        init();
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void init() {
        plugin.getLogger().info("Using MySQL as storage system");
        executor.execute(() -> {
            String query = """
                    CREATE TABLE IF NOT EXISTS users (
                        home_id INT AUTO_INCREMENT PRIMARY KEY,
                        uuid VARCHAR(36) NOT NULL,
                        homeName VARCHAR(16) NOT NULL,
                        location TEXT NOT NULL
                    )
                    """;
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute(query);
            } catch (SQLException e) {
                plugin.getLogger().severe("Couldn't create users table. Disabling plugin...");
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
            String query = "SELECT homeName, location FROM users WHERE uuid = ?";
            List<Home> homes = new ArrayList<>();

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String homeName = rs.getString("homeName");
                        String location = rs.getString("location");
                        Home home = Home.deserialize(homeName, location);
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
        plugin.getLogger().info("Shutting down MySQL connection pool");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
