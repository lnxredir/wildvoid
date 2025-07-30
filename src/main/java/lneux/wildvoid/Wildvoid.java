package lneux.wildvoid;

import org.bukkit.plugin.java.JavaPlugin;

public class Wildvoid extends JavaPlugin {
    private static Wildvoid instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getCommand("rtp").setExecutor(new TeleportCommand());
        getCommand("wild").setExecutor(new TeleportCommand());
    }

    public static Wildvoid getInstance() {
        return instance;
    }
}
