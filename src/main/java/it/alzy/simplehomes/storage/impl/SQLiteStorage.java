package it.alzy.simplehomes.storage.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.bukkit.Bukkit;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import it.alzy.simplehomes.SimpleHomes;
import it.alzy.simplehomes.records.Home;
import it.alzy.simplehomes.storage.IStorage;

public class SQLiteStorage implements IStorage {

    private final SimpleHomes plugin;
    private final ExecutorService executor;
    private final HikariDataSource dataSource;

    public SQLiteStorage(SimpleHomes plugin) {
        this.plugin = plugin;
        this.executor = plugin.getExecutor();

        File dataFolder = plugin.getDataFolder();
        if(!dataFolder.exists()) dataFolder.mkdirs();
        File db = new File(dataFolder, "Players.db");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + db.getPath());
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

    public void init() {
        plugin.getLogger().info("Using SQLITE as storage system");
        executor.execute(() -> {
            String query = """
                    CREATE TABLE IF NOT EXISTS users(
                        home_id INTEGER PRIMARY KEY AUTOINCREMENT, 
                        uuid VARCHAR(36),
                        homeName VARCHAR(16),
                        location TEXT
                    )
                    """;
            try(Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute(query);
            } catch(SQLException e) {
                plugin.getLogger().severe("Couldn't create user table, Self disabling");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        });
    }


    public void createHome(UUID ownerID, Home home) {
        executor.execute(() -> {
            String query = "INSERT INTO users(uuid, homeName, location) VALUES (?,?,?)";
            try(PreparedStatement ps = getConnection().prepareStatement(query)) {
                ps.setString(1, ownerID.toString());
                ps.setString(2, home.homeName());
                ps.setString(3, home.serialize());
                ps.executeUpdate();
            } catch(SQLException e) {
                plugin.getLogger().severe("Couldn't save a player's home: " + e.getMessage());
                plugin.getLogger().info(e.getMessage());
            }
        });
    }

    public void deleteHome(UUID uuid, String homeName) {
        executor.execute(() -> {
            String query = "DELETE FROM users WHERE homeName = ? AND uuid = ?";
            try(PreparedStatement ps = getConnection().prepareStatement(query)) {
                 ps.setString(1, homeName);
                 ps.setString(2, uuid.toString());
                 ps.executeUpdate();
            } catch(SQLException e) {
                plugin.getLogger().severe("Couldn't delete a player's home: " + e.getMessage());
            }
        });
    }


    @Override
    public void load(UUID uuid) {
        executor.execute(() -> {
            List<Home> temp = new ArrayList<>();
            String query = "SELECT homeName, location FROM users WHERE uuid = ?";
            try(PreparedStatement ps = getConnection().prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try(ResultSet rs = ps.executeQuery()){
                    while(rs.next()) {
                        String homeName = rs.getString("homeName");
                        String location = rs.getString("location");
                        if(location == null || location.isEmpty()) continue;
                        String[] parts = location.split(":");
                        if(parts.length < 6) continue;
                        String worldName = parts[0];
                        double x = Double.parseDouble(parts[1]);
                        double y = Double.parseDouble(parts[2]);
                        double z = Double.parseDouble(parts[3]);
                        float yaw = Float.parseFloat(parts[4]);
                        float pitch = Float.parseFloat(parts[5]);
                        Home home = new Home(homeName, worldName, x, y, z, yaw, pitch);
                        temp.add(home);
                        plugin.getCache().put(uuid, temp);
                    }
                }
            } catch(SQLException e) {
                plugin.getLogger().severe("Couldn't load a player's home");
                plugin.getLogger().info(e.getMessage());
            }
        });
    }

    
    public void close() {
        plugin.getLogger().info("Closing SQLITE connection");
        try {
            if(getConnection() != null && !getConnection().isClosed()) {
                getConnection().close();
            }
        } catch(SQLException e) {
            plugin.getLogger().severe("Couldn't close connection: " + e.getMessage());

        }
    }

}
