package xyz.whynospaces.mysterychests;

import com.comphenix.protocol.ProtocolManager;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.whynospaces.MysteryChest;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Luke on 5/6/2016.
 */
public class ItemSelector extends BukkitRunnable {

    private ProtocolManager m_protocolManager = MysteryChest.s_protocolManager;

    private Player m_player;
    private ItemStack m_itemStack;
    private List<ItemStack> m_items;
    private MysteryChest m_plugin;
    private Block m_chest;
    private Sign m_sign;

    public ItemSelector(MysteryChest plugin, Player player, Block chest, Sign sign) {
        m_player = player;
        m_plugin = plugin;
        m_chest = chest;
        m_sign = sign;
        grabItemsFromConfig();

        Location loc = m_chest.getLocation();
        BlockPosition position = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()); // Create the block position using loc
        PacketPlayOutBlockAction blockActionPacket = new PacketPlayOutBlockAction(position, net.minecraft.server.v1_8_R3.Block.getById(loc.getBlock().getTypeId()), (byte) 1, (byte)1);
        ((CraftPlayer) m_player).getHandle().playerConnection.sendPacket(blockActionPacket); // Send animation packet to CraftPlayer

    }

    public void run() {
        if(!m_items.isEmpty()) {
            Collections.shuffle(m_items);
            new BukkitRunnable() {
                int seconds = 0;
                public void run() {
                    seconds++;
                    ItemSelector.this.setItemStack(ItemSelector.this.m_items.get(new Random().nextInt(ItemSelector.this.m_items.size())));
                    final Item item = m_player.getWorld().dropItem(m_chest.getLocation().add(0.5,1,0.5), m_itemStack);
                    item.setMetadata("mysteryChest-nopickup", new FixedMetadataValue(m_plugin, null));

                    m_player.playSound(m_chest.getLocation(), Sound.LEVEL_UP, 1f, 1f);

                    BlockPosition pos = new BlockPosition(m_sign.getLocation().getBlockX(), m_sign.getLocation().getBlockY(), m_sign.getLocation().getBlockZ());
                    CraftWorld world = (CraftWorld) m_chest.getLocation().getWorld();
                    PacketPlayOutUpdateSign packet = new PacketPlayOutUpdateSign(world.getHandle(), pos, new IChatBaseComponent[] {
                            null,
                            IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + (itemCheck(m_itemStack) ? ChatColor.DARK_RED + ChatColor.stripColor(m_itemStack.getItemMeta().getDisplayName()) : ChatColor.DARK_RED + m_itemStack.getType().toString().toLowerCase().replaceAll("_", " ")) + "\"}"),
                            null,
                            null
                    });

                    ((CraftPlayer) m_player).getHandle().playerConnection.sendPacket(packet);

                    PacketPlayOutEntityDestroy destroyItem = new PacketPlayOutEntityDestroy(item.getEntityId());
                    for(Entity entity : item.getNearbyEntities(10, 10, 10)) {
                        if(entity instanceof Player) {
                            Player nearbyPlayer = (Player)entity;
                            if(!nearbyPlayer.equals(m_player)) {
                                ((CraftPlayer)nearbyPlayer).getHandle().playerConnection.sendPacket(destroyItem);
                            }
                        }
                    }

                    new BukkitRunnable() {
                        public void run() {
                            item.remove();
                        }
                    }.runTaskLater(m_plugin, 5L);
                    if(seconds == MysteryChestManager.getConfig().getInt("scroller-timer")) {


                        Location loc = m_chest.getLocation();
                        BlockPosition position2 = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                        PacketPlayOutBlockAction blockActionPacket2 = new PacketPlayOutBlockAction(position2, net.minecraft.server.v1_8_R3.Block.getById(loc.getBlock().getTypeId()), (byte) 1, (byte)0);
                        ((CraftPlayer) m_player).getHandle().playerConnection.sendPacket(blockActionPacket2);

                        m_player.getInventory().addItem(m_itemStack);

                        if(MysteryChestManager.getConfig().getBoolean("broadcast-on")) {
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', MysteryChestManager.getConfig().getString("broadcast-msg")
                                    .replaceAll("PLAYER_NAME", m_player.getName()).replaceAll("ITEM_NAME", (itemCheck(m_itemStack) ? ChatColor.stripColor(m_itemStack.getItemMeta().getDisplayName()) : m_itemStack.getType().toString().toLowerCase().replaceAll("_", " ")))));
                        }

                        this.cancel();

                        PacketPlayOutUpdateSign packet2 = new PacketPlayOutUpdateSign(world.getHandle(), pos, new IChatBaseComponent[] {
                                IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + ChatColor.DARK_RED + "[MysteryChest]" + "\"}"),
                                IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + MysteryChestManager.getConfig().getInt("price") + "\"}"),
                                null,
                                null
                        });
                        ((CraftPlayer) m_player).getHandle().playerConnection.sendPacket(packet2);

                        MysteryChestListener.s_playersUsingMysteryChest.remove(m_player.getName());
                    }
                }
            }.runTaskTimer(m_plugin, 0L, 20L);
            this.cancel();
        } else {
            m_player.sendMessage(ChatColor.RED + "The item list is empty.");
        }
    }

    public boolean itemCheck(ItemStack itemStack) {
        return itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName();
    }

    public Player getPlayer() {
        return m_player;
    }

    public Block getChest() {
        return m_chest;
    }

    public void grabItemsFromConfig() {
        m_items = new LinkedList<ItemStack>();
        if(MysteryChestManager.getConfig().getConfigurationSection("items.") != null) {
            for(String ids : MysteryChestManager.getConfig().getConfigurationSection("items.").getKeys(false)) {
                m_items.add(MysteryChestManager.getConfig().getItemStack("items." + ids));
            }
        } else {
            m_player.sendMessage(ChatColor.RED + "The item list is empty.");
        }
    }

    public List<ItemStack> getItems() {
        return m_items;
    }

    public ItemStack getItemStack() {
        return m_itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        m_itemStack = itemStack;
    }
}
