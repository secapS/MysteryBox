package xyz.whynospaces;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.whynospaces.mysterychests.MysteryChestListener;
import xyz.whynospaces.mysterychests.MysteryChestManager;

/**
 * Created by Luke on 5/6/2016.
 */
public class MysteryChest extends JavaPlugin {

    public static ProtocolManager s_protocolManager = null;

    public static Permission s_permission = null;
    public static Chat s_chat = null;
    public static Economy s_economy = null;

    public void onEnable() {
        //Listeners
        this.getServer().getPluginManager().registerEvents(new MysteryChestListener(this), this);

        //MysteryChest
        MysteryChestManager.setup(this);

        //ProtocolLib
        s_protocolManager = ProtocolLibrary.getProtocolManager();

        //Vault
        setupPermissions();
        setupChat();
        setupEconomy();

        this.saveDefaultConfig();
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            s_permission = permissionProvider.getProvider();
        }
        return (s_permission != null);
    }

    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            s_chat = chatProvider.getProvider();
        }

        return (s_chat != null);
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            s_economy = economyProvider.getProvider();
        }

        return (s_economy != null);
    }
}
