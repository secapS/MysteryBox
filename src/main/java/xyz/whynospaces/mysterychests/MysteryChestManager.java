package xyz.whynospaces.mysterychests;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import xyz.whynospaces.MysteryChest;

import java.util.Set;

/**
 * Created by Luke on 5/6/2016.
 */
public class MysteryChestManager {

    private static MysteryChest m_plugin;

    private static String m_itemsConfigPath = "items.";

    public static void setup(MysteryChest plugin) {
        m_plugin = plugin;
    }

    private MysteryChestManager() {}

    public static void addItem(ItemStack itemStack) {
        if(itemStack != null && itemStack.getType() != Material.AIR) {
            if(getConfig().getConfigurationSection(m_itemsConfigPath) == null) {
                getConfig().set(m_itemsConfigPath + 0, itemStack);
                saveConfig();
                m_plugin.reloadConfig();
            } else {
                Set<String> keys = getConfig().getConfigurationSection(m_itemsConfigPath).getKeys(false);
                getConfig().set(m_itemsConfigPath + keys.size(), itemStack);
                saveConfig();
                m_plugin.reloadConfig();
            }
        }
    }

    public static void saveConfig() {
        m_plugin.saveConfig();
    }

    public static FileConfiguration getConfig() {
        return m_plugin.getConfig();
    }
}
