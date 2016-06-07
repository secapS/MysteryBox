package xyz.whynospaces.mysterychests;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftChest;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;
import xyz.whynospaces.MysteryChest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Luke on 5/6/2016.
 */
public class MysteryChestListener implements Listener {

    private MysteryChest m_plugin;

    public MysteryChestListener(MysteryChest plugin) {
        m_plugin = plugin;
    }

    @EventHandler
    public void onPlaceSign(SignChangeEvent event) {
        if(event.getLine(0) != null) {
            if(event.getLine(0).equalsIgnoreCase("[MysteryChest]")) {
                if(event.getBlock().getType() == Material.WALL_SIGN) {
                    if(MysteryChest.s_permission.has(event.getPlayer(), "mysterychest.admin.create")) {
                        org.bukkit.material.Sign sign = (org.bukkit.material.Sign)event.getBlock().getState().getData();
                        Sign signEntity = (Sign)event.getBlock().getState();
                        if(event.getBlock().getRelative(sign.getAttachedFace()).getType() == Material.CHEST) {
                            event.setLine(0, ChatColor.DARK_RED + event.getLine(0));
                            event.setLine(1, MysteryChestManager.getConfig().getString("price"));
                            Chest chest = (Chest)event.getBlock().getRelative(sign.getAttachedFace()).getState();
                            ((CraftChest)event.getBlock().getRelative(sign.getAttachedFace()).getState()).getTileEntity().a("Mystery Chest");
                            event.getPlayer().sendMessage(ChatColor.GREEN + "Mystery Chest created.");
                        } else {
                            event.getPlayer().sendMessage(ChatColor.RED + "Could not register Mystery Chest.");
                            event.getBlock().breakNaturally();
                        }
                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission.");
                    }
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Could not register Mystery Chest.");
                    event.getBlock().breakNaturally();
                }
            }
        }
    }

    public static List<String> s_playersUsingMysteryChest = new ArrayList<String>();

    @EventHandler
    public void onClickSign(PlayerInteractEvent event) {
        if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(event.getClickedBlock().getType() == Material.WALL_SIGN) {
                org.bukkit.material.Sign sign = (org.bukkit.material.Sign)event.getClickedBlock().getState().getData();
                Sign signEntity = (Sign)event.getClickedBlock().getState();
                if(event.getClickedBlock().getRelative(sign.getAttachedFace()).getType() == Material.CHEST) {
                    if(signEntity.getLine(0).equalsIgnoreCase(ChatColor.DARK_RED + "[MysteryChest]")) {
                        EconomyResponse canWithdraw = MysteryChest.s_economy.withdrawPlayer(event.getPlayer(), (double)MysteryChestManager.getConfig().getInt("price"));
                        if(canWithdraw.transactionSuccess()) {
                            if(!s_playersUsingMysteryChest.contains(event.getPlayer().getName())) {
                                event.setCancelled(true);
                                Block chest = event.getClickedBlock().getRelative(sign.getAttachedFace());
                                BukkitTask itemSelector = new ItemSelector(m_plugin, event.getPlayer(), chest, signEntity).runTask(m_plugin);
                                s_playersUsingMysteryChest.add(event.getPlayer().getName());
                            }
                        } else {
                            event.getPlayer().sendMessage(ChatColor.RED + "Insufficient funds.");
                        }
                    }
                }
            }


            if(event.getClickedBlock().getType() == Material.CHEST) {
                Chest chest = (Chest)event.getClickedBlock().getState();
                if(chest.getInventory().getName().equals("Mystery Chest")) {
                    if(MysteryChest.s_permission.has(event.getPlayer(), "mysterychest.admin.break")) {
                        event.setCancelled(false);
                    } else {
                        event.setCancelled(true);
                    }
                    if(MysteryChest.s_permission.has(event.getPlayer(), "mysterychest.admin.addItem")) {
                        if(event.getPlayer().getItemInHand() != null || event.getPlayer().getItemInHand().getType() != Material.AIR) {
                            MysteryChestManager.addItem(event.getPlayer().getItemInHand());
                        } else {
                            event.getPlayer().sendMessage(ChatColor.RED + "Couldn't add item to Mystery Chest (item null).");
                        }
                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if(event.getItem().hasMetadata("mysteryChest-nopickup")) {
            event.setCancelled(true);
        }
    }
}