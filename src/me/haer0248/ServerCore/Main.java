package me.haer0248.ServerCore;

import java.io.File;
import java.util.*;

import com.mojang.datafixers.types.templates.Check;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public static Main instance;
    private static Map<String, String> pm = new HashMap<>();

    @Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
        instance = this;
        
        File config = new File("plugins/ServerCore/config.yml");

        if (!(config.exists())){
        	this.getLogger().info("已複製 config.yml 至插件資料夾。");
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
        }

        instance = this;
	}

    public String prefix = format(getConfig().getString("prefix"));
    public String DataFile = getConfig().getString("data");
	
    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
    	reloadConfig();
    	
        Player player = event.getPlayer();
        List<String> MessageList = this.getConfig().getStringList("motd");
        
        if (this.getConfig().getBoolean("enable-motd")) {
			for (String message: MessageList) {
				player.sendMessage(format(message.replace("%player%", player.getName())));			        
			}
        }

        String playerName = player.getName().toString();
        player.sendMessage(prefix + format(getConfig().getString("message.searching")));
        Integer StatusCode = CheckFile.onJoin(player, DataFile);
        if (StatusCode == 1) {
            player.sendMessage(prefix + format(getConfig().getString("message.created").replace("%player%", playerName)));
        } else if (StatusCode == 2){
            player.sendMessage(prefix + format(getConfig().getString("message.updated").replace("%player%", playerName)));
        } else {
            player.sendMessage(prefix + format(getConfig().getString("message.failed").replace("%player%", playerName)));
        }
        
        event.setJoinMessage(format(getConfig().getString("join").replace("%player%", player.getName())));
    }

    @EventHandler
    private void onLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_BANNED) {
            Bukkit.getServer().broadcastMessage(format("&c封禁玩家 " + event.getPlayer().getName() + " 嘗試加入伺服器。"));
            event.setKickMessage(format("&c您已經被伺服器永久封禁。"));
        }

        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            event.setKickMessage(format("&b伺服器人數已滿。"));
        }
    }


    @EventHandler
    private void onChat(AsyncPlayerChatEvent event) {
        event.setMessage(event.getMessage().replaceAll("%", "\\%"));
        String prefix = format(getConfig().getString("chat").replace("%player%", event.getPlayer().getName()).replace("%message%", event.getMessage()));
        event.setFormat(prefix);
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission("servercore.chat.color")){
            event.setMessage(format(event.getMessage()));
        } else {
            event.setMessage(decolor(event.getMessage()));
        }
    }

    @EventHandler
    private void onSleep(PlayerBedEnterEvent event){
        int x = event.getBed().getX();
        int y = event.getBed().getY();
        int z = event.getBed().getZ();
        String world = event.getBed().getWorld().getName();

        event.getPlayer().sendMessage(format("&7&oServer 悄悄對您說: &c&o您的床點已設為 " + x + ", " + y + ", " + z + " &6&o您可以使用 /bed 來返回床點。"));
        CheckFile.onSleep(event.getPlayer(), DataFile, x, y, z, world);
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event){
	    String message = event.getDeathMessage();
	    int x = event.getEntity().getLocation().getBlockX();
        int y = event.getEntity().getLocation().getBlockY();
        int z = event.getEntity().getLocation().getBlockZ();
        String world = event.getEntity().getWorld().getName();

	    event.setDeathMessage(format("&c" + message));
	    event.getEntity().getPlayer().sendMessage(format("&7&oServer 悄悄對您說: &c&o您的死亡座標為 " + x + ", " + y + ", " + z + " &6&o您可以使用 /back 來返回死亡地點。"));
	    CheckFile.onDeath(event.getEntity().getPlayer(), DataFile, x, y, z, world);
    }

    public boolean onCommand(CommandSender sender , Command cmd , String lable , String[] args) {
        String cmds = cmd.getName();
        if (cmds.equalsIgnoreCase("bc")) {
            if (sender.isOp() || sender.hasPermission("servercore.admin")) {
                if (args.length == 0) {
                    sender.sendMessage(prefix + format("&3正確用法 /bc <訊息>"));
                } else {
                    List<String> bc_list = new ArrayList<>();
                    String[] bc_line = String.join(" ", args).split(",");
                    bc_list = Arrays.asList(bc_line);

                    Bukkit.getServer().broadcastMessage("§f ");
                    Bukkit.getServer().broadcastMessage(format(getConfig().getString("boardcast-title")));
                    for (String message : bc_list) {
                        Bukkit.getServer().broadcastMessage(format(message));
                    }
                    Bukkit.getServer().broadcastMessage("§f ");
                }
            } else {
                sender.sendMessage(prefix + format(getConfig().getString("message.denied")));
            }
        } else if (cmds.equalsIgnoreCase("setspawn")) {
            if (sender.isOp() || sender.hasPermission("servercore.admin")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(prefix + format(getConfig().getString("message.playeronly")));
                } else {
                    Player player = ((Player) sender).getPlayer();
                    int x = player.getLocation().getBlockX();
                    int y = player.getLocation().getBlockY();
                    int z = player.getLocation().getBlockZ();
                    String world = player.getWorld().getName();

                    getConfig().set("SpawnLocation.x", x);
                    getConfig().set("SpawnLocation.y", y);
                    getConfig().set("SpawnLocation.z", z);
                    getConfig().set("SpawnLocation.world", world);
                    saveConfig();

                    sender.sendMessage(prefix + format(getConfig().getString("message.setspawn")));

                }
            } else {
                sender.sendMessage(prefix + format(getConfig().getString("message.denied")));
            }
        } else if (cmds.equalsIgnoreCase("back")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + format(getConfig().getString("message.playeronly")));
            } else {
                Object DeathLocation = CheckFile.getLocation(((Player) sender).getPlayer(), DataFile, "LastDeath");
                if (DeathLocation == "null"){
                    sender.sendMessage(prefix + format(getConfig().getString("message.cantback")));
                } else {
                    Player player = ((Player) sender).getPlayer();
                    player.teleport((Location) DeathLocation);
                    player.sendMessage(prefix + format(getConfig().getString("message.back")));
                }
            }
        } else if (cmds.equalsIgnoreCase("spawn")){
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + format(getConfig().getString("message.playeronly")));
            } else {
                Location SpawnLocation = new Location(getServer().getWorld(getConfig().getString("SpawnLocation.world")), getConfig().getDouble("SpawnLocation.x"), getConfig().getDouble("SpawnLocation.y"), getConfig().getDouble("SpawnLocation.z"));
                Player player = ((Player) sender).getPlayer();
                player.teleport(SpawnLocation);
                player.sendMessage(prefix + format(getConfig().getString("message.spawn")));
            }
        } else if (cmds.equalsIgnoreCase("bed")){
            if (!(sender instanceof Player)) {
                sender.sendMessage(prefix + format(getConfig().getString("message.playeronly")));
            } else {
                Object BedLocation = CheckFile.getLocation(((Player) sender).getPlayer(), DataFile, "BedLocation");
                if (BedLocation == "null"){
                    sender.sendMessage(prefix + format(getConfig().getString("message.cantbed")));
                } else {
                    Player player = ((Player) sender).getPlayer();
                    player.teleport((Location) BedLocation);
                    player.sendMessage(prefix + format(getConfig().getString("message.bed")));
                }
            }
        } else if (cmds.equalsIgnoreCase("list")) {
            StringBuilder online = new StringBuilder();
            final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            for (Player player : players) {
                if (online.length() > 0) {
                    online.append(", ");
                }
                online.append(player.getDisplayName());
            }
            String full = "§7";
            if (players.size() >= Bukkit.getMaxPlayers()) {
                full = " §c§l伺服器已滿！";
            }
            sender.sendMessage("§8§m－－－－－－－－－§6 玩家列表 ("+ players.size() + "/" + Bukkit.getMaxPlayers() + ")"+ full +" §8§m－－－－－－－－－\n"
                    + "§r" + online.toString());

        } else if (cmds.equalsIgnoreCase("m") || cmds.equalsIgnoreCase("msg")) {
            if (sender instanceof Player) {
                if (args.length == 0) {
                    sender.sendMessage(prefix + format(getConfig().getString("message.msg.help")));
                }else {
                    Player Player_to = getPlayer(args[0]);

                    if (Player_to != null) {
                        String str = new String();
                        for(int i=1;i<args.length;i++){
                            str += args[i] + " ";
                        }
                        str = decolor(str);
                        String message = "§e來自 "+ sender.getName() +": " + str.substring(0, str.length());
                        String message_form = "§d傳送給 "+ Player_to.getName() +": " + str.substring(0, str.length());
                        if (str.length() >= 255) {
                            sender.sendMessage(prefix + format(getConfig().getString("message.msg.maxlength")));
                        }else {
                            sender.sendMessage(message_form);
                            Player_to.sendMessage(message);
                            pm.put(Player_to.getName(), sender.getName());
                        }
                    }else {
                        sender.sendMessage(prefix + format(getConfig().getString("message.playeroffline").replace("%player%", args[0])));
                    }
                }
            }else {
                sender.sendMessage(prefix + format(getConfig().getString("message.playeronly")));
            }

        } else if (cmds.equalsIgnoreCase("r") || cmds.equalsIgnoreCase("reply")) {
            if (sender instanceof Player) {
                if (args.length == 0) {
                    sender.sendMessage(prefix + format(getConfig().getString("message.reply.help")));
                }else {
                    Player Player_reply = getPlayer(pm.get(sender.getName()));
                    if (Player_reply != null) {
                        String str = new String();
                        for(int i=0;i<args.length;i++){
                            str += args[i] + " ";
                        }
                        str = decolor(str);
                        String message = "§e來自 "+ sender.getName() +": " + str.substring(0, str.length());
                        String message_form = "§d傳送給 "+ Player_reply.getName() +": " + str.substring(0, str.length());
                        if (str.length() >= 255) {
                            sender.sendMessage(prefix + format(getConfig().getString("message.msg.maxlength")));
                        }else {
                            sender.sendMessage(message_form);
                            Player_reply.sendMessage(message);
                            pm.put(Player_reply.getName(), sender.getName());
                        }
                    }else {
                        sender.sendMessage(prefix + format(getConfig().getString("message.reply.noplayer")));
                    }
                }
            }else {
                sender.sendMessage(prefix + format(getConfig().getString("message.playeronly")));
            }
        } else if (cmds.equalsIgnoreCase("fly")) {
            if (sender instanceof Player) {
                Player player = ((Player) sender).getPlayer();
                if (player.isFlying()){
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.sendMessage(prefix + format(getConfig().getString("message.fly.disable")));
                } else {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.sendMessage(prefix + format(getConfig().getString("message.fly.enable")));
                }
            }else {
                sender.sendMessage(prefix + format(getConfig().getString("message.playeronly")));
            }
        }
        return true;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(format(getConfig().getString("quit").replace("%player%", player.getName())));
    }

    public static String format(String format){
		return ChatColor.translateAlternateColorCodes('&', format);
	}

    public static String decolor(String format) {
        return format.replaceAll("&1|&2|&3|&4|&5|&6|&7|&8|&9|&a|&b|&c|&d|&e|&f|&l|&m|&n|&o|&r", "");
    }

    public static Player getPlayer(String player_name) {
        List<Player> player_list = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : player_list) {
            if (player.getName().equals(player_name)) {
                return player;
            }
        }

        return null;
    }
}