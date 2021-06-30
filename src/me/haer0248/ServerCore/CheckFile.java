package me.haer0248.ServerCore;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class CheckFile {

    public static Integer onJoin(Player player, String Folder) {
        String playerName = player.getName().toString();
        String playerUuid = player.getUniqueId().toString();

        File userdata = new File("plugins/ServerCore/", Folder);
        File f = new File(userdata, File.separator + playerUuid + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(f);

        if (!f.exists()) {
            try {
                playerData.set("Username", playerName);
                playerData.set("UUID", playerUuid);
                playerData.createSection("LastDeath");
                playerData.set("LastDeath.active", false);
                playerData.createSection("BedLocation");
                playerData.set("BedLocation.active", false);
                playerData.set("Nickname", "none");
                playerData.save(f);
                return 1;
            } catch (IOException exception) {
                exception.printStackTrace();
                return 9;
            }
        } else {
            try {
                playerData.set("Username", playerName);
                playerData.set("UUID", playerUuid);
                playerData.set("Deathed", null);
                playerData.set("Bed", null);
                playerData.save(f);
                return 2;
            } catch (IOException exception) {
                exception.printStackTrace();
                return 9;
            }
        }
    }

    public static Boolean onDeath(Player player, String Folder, Integer x, Integer y, Integer z, String world) {
        String playerName = player.getName().toString();
        String playerUuid = player.getUniqueId().toString();

        File userdata = new File("plugins/ServerCore/", Folder);
        File f = new File(userdata, File.separator + playerUuid + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(f);
        getLogger().info("§c[LOG]" + playerName + ": DeathLocation " + x + ", " + y +", " + z + ", " + world);
        try {
            playerData.set("LastDeath.x", x);
            playerData.set("LastDeath.y", y);
            playerData.set("LastDeath.z", z);
            playerData.set("LastDeath.world", world);
            playerData.set("LastDeath.active", true);
            playerData.save(f);
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public static Boolean onSleep(Player player, String Folder, Integer x, Integer y, Integer z, String world) {
        String playerName = player.getName().toString();
        String playerUuid = player.getUniqueId().toString();

        File userdata = new File("plugins/ServerCore/", Folder);
        File f = new File(userdata, File.separator + playerUuid + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(f);
        try {
            playerData.set("BedLocation.x", x);
            playerData.set("BedLocation.y", y);
            playerData.set("BedLocation.z", z);
            playerData.set("BedLocation.world", world);
            playerData.set("BedLocation.active", true);
            playerData.save(f);
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public static Object getLocation (Player player, String Folder, String Type) {
        String playerUsername = player.getName().toString();
        String playerUuid = player.getUniqueId().toString();

        File userdata = new File("plugins/ServerCore/", Folder);
        File f = new File(userdata, File.separator + playerUuid + ".yml");
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(f);
        Integer x, y, z;

        if (!(playerData.getBoolean(Type + ".active"))) {
            return "null";
        } else {
            World world = getServer().getWorld(playerData.getString(Type + ".world"));
            Location location = new Location(world, playerData.getInt(Type + ".x"), playerData.getInt(Type + ".y"), playerData.getInt(Type + ".z"));
            return location;
        }

    }
}
