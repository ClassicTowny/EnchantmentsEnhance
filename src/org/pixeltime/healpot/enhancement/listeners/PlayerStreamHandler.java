package org.pixeltime.healpot.enhancement.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.pixeltime.healpot.enhancement.events.blacksmith.SecretBook;
import org.pixeltime.healpot.enhancement.events.blackspirit.Failstack;
import org.pixeltime.healpot.enhancement.events.inventory.Inventory;
import org.pixeltime.healpot.enhancement.manager.SettingsManager;
import org.pixeltime.healpot.enhancement.Main;
import org.pixeltime.healpot.enhancement.util.Util;

public class PlayerStreamHandler implements Listener {

    public Main m;


    public PlayerStreamHandler(Main m) {
        this.m = m;
    }


    /**
     * Sends a message to greet joined player.
     * 
     * @param e
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Util.sendMessage(SettingsManager.lang.getString("Config.welcome").replaceAll(
                "%player%", player.getName()), player);
        Failstack.loadLevels(player);
        SecretBook.loadStorage(player);
        Inventory.loadInventory(player);
    }


    /**
     * When a player leaves the server, listener saves a player's data from
     * hashmap to file, but will not write to disk.
     * 
     * @param e
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Failstack.saveLevels(player, false);
        SecretBook.saveStorageToDisk(player, false);
        Inventory.saveInventoryToDisk(player, false);
    }


    /**
     * When a player gets kicked off the server, listener saves a player's data
     * from hashmap to file, but will not write to disk.
     * 
     * @param e
     */
    @EventHandler
    public void onKick(PlayerKickEvent e) {
        Player player = e.getPlayer();
        Failstack.saveLevels(player, false);
        SecretBook.saveStorageToDisk(player, false);
        Inventory.saveInventoryToDisk(player, false);
    }

}
