package lneux.wildvoid;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;

public class TeleportCommand implements CommandExecutor {
    private final WeakHashMap<UUID, Long> cooldowns = new WeakHashMap<>();
    private final Random random = new Random();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        long cooldown = Wildvoid.getInstance().getConfig().getLong("delay-seconds");
        long currentTime = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        if (cooldowns.containsKey(uuid) && currentTime - cooldowns.get(uuid) < cooldown * 1000) {
            player.sendMessage(getMsg("messages.cooldown"));
            return true;
        }

        cooldowns.put(uuid, currentTime);
        player.sendMessage(getMsg("messages.generating"));

        Location origin = player.getLocation();
        Material platformBlock = Material.valueOf(Wildvoid.getInstance().getConfig().getString("platform-block"));

        new BukkitRunnable() {
            @Override
            public void run() {
                Location platformLoc = getRandomLocation(origin, 100);

                // Now schedule block placement and teleport on main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        buildPlatform(platformLoc, platformBlock);
                        player.teleport(platformLoc.clone().add(1, 1, 1));
                        player.sendMessage(getMsg("messages.teleporting"));
                    }
                }.runTask(Wildvoid.getInstance());
            }
        }.runTaskLater(Wildvoid.getInstance(), cooldown * 20L);

        return true;
    }

    private Location getRandomLocation(Location origin, int minDistance) {
        int x = origin.getBlockX() + (random.nextBoolean() ? 1 : -1) * (minDistance + random.nextInt(300));
        int z = origin.getBlockZ() + (random.nextBoolean() ? 1 : -1) * (minDistance + random.nextInt(300));
        int y = 64; // flat Y level for platform
        return new Location(origin.getWorld(), x, y, z);
    }

    private void buildPlatform(Location loc, Material blockType) {
        World world = loc.getWorld();
        for (int dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                Block block = world.getBlockAt(loc.clone().add(dx, 0, dz));
                block.setType(blockType);
            }
        }
    }

    private String getMsg(String path) {
        return ChatColor.translateAlternateColorCodes('&',
                Wildvoid.getInstance().getConfig().getString(path));
    }
}
