package it.alzy.simplehomes.records;


import org.bukkit.Bukkit;
import org.bukkit.Location;

public record Home(String homeName, String worldName, double x, double y, double z, float yaw, float pitch) {

    public static Home fromLocation(String name, Location loc) {
        return new Home(name, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public static Home deserialize(String name, String locationStr) {
        String[] parts = locationStr.split(":");
        if(parts.length != 6) return null;
        return new Home(name, 
            parts[0],
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3]),
            Float.parseFloat(parts[4]),
            Float.parseFloat(parts[5])
        );
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public String serialize() {
        return String.join(":", 
            worldName,
            String.valueOf(x),
            String.valueOf(y),
            String.valueOf(z),
            String.valueOf(yaw),
            String.valueOf(pitch)        
        );
    }

}
