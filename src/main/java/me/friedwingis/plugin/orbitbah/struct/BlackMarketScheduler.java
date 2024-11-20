package me.friedwingis.plugin.orbitbah.struct;

import me.friedwingis.plugin.orbitbah.OrbitBAH;
import me.friedwingis.plugin.orbitcore.objects.SavedJSONItemStack;
import me.friedwingis.plugin.orbitcore.patches.impl.CollectionBin;
import me.friedwingis.plugin.orbitcore.utils.Chat;
import me.friedwingis.plugin.orbitcore.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * Copyright FriedWingis - 2023
 * All code is private and not to be used by any
 * other entity unless explicitly stated otherwise.
 **/
public class BlackMarketScheduler extends BukkitRunnable {
    private OrbitBAH plugin;

    public BAHItemStatus status = BAHItemStatus.COUNTING;
    public int count = OrbitBAH.DISPLAYING_EVERY;

    public void begin(final OrbitBAH plugin) {
        this.plugin = plugin;
        runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    public void run() {
        BlackMarketGUI.VIEWERS.forEach((player, blackMarketGUI) -> {
            blackMarketGUI.refreshMiddleItem();
        });

        if (this.status == BAHItemStatus.COUNTING) {
            if (this.count <= 0) {
                try {
                    createNewListing();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                this.count--;
        } else {
            if (this.count <= 0) {
                endCurrentListing();
            } else
                this.count--;
        }
    }

    public void createNewListing() throws IOException {
        this.status = BAHItemStatus.DISPLAYING;
        this.count = OrbitBAH.DISPLAYING_FOR;

        SavedJSONItemStack saved = plugin.getManager().getLootTable().getPool().next();
        if (saved == null)
            saved = new SavedJSONItemStack(UUID.randomUUID(), new ItemStack(Material.DIAMOND), 100.0, "NULL");

        final CurrentListing currentListing = new CurrentListing(saved.build());
        this.plugin.getManager().currentListing = currentListing;

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("");
            player.sendMessage(Chat.center("<gold><bold>* Black Market Auction *"));
            player.sendMessage(Chat.center(Chat.format("<gold><bold>Item</bold>: ").append((currentListing.item.hasItemMeta() && currentListing.item.getItemMeta().hasDisplayName()) ? Objects.requireNonNull(currentListing.item.getItemMeta().displayName()) : Chat.format(currentListing.item.getType().name()))));
            player.sendMessage(Chat.center("<gold><bold>Time</bold>: <grey>" + TimeUtils.getFormattedTime(count)));
            player.sendMessage(Chat.center("<grey>Use <white>/bah <grey>to view and place bids."));
            player.sendMessage("");

            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 0.8f, 0.75f);
        }

        plugin.getLogger().info("Successfully created a new listing");
    }

    public void endCurrentListing() {
        this.status = BAHItemStatus.COUNTING;
        this.count = OrbitBAH.DISPLAYING_EVERY;

        final CurrentListing currentListing = plugin.getManager().currentListing;
        if (currentListing == null) {
            plugin.getLogger().warning("Current listing ended but no current listing existed in the first place!");
            return;
        }

        if (!currentListing.highestBidder.equalsIgnoreCase("None")) {
            final OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(currentListing.highestBidder);
            if (player != null) {
                try {
                    if (player.getPlayer() != null)
                        player.getPlayer().sendMessage(Chat.praise("Your /bah reward has been placed inside your /collect!"));

                    CollectionBin.encode(player.getUniqueId(), currentListing.build().build(), true);
                } catch (IOException e) {
                    if (player.getPlayer() != null)
                        player.getPlayer().sendMessage(Chat.severe("An error has occurred while attempting to save your /bah reward. Please contact an administrator."));

                    e.printStackTrace();
                }
            }
        }

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("");
            player.sendMessage(Chat.format("<gold><b>[/BAH]</b> " + currentListing.highestBidder + " has won the bid on ")
                    .append((currentListing.item.hasItemMeta() && currentListing.item.getItemMeta().hasDisplayName()) ? currentListing.item.getItemMeta().displayName() : Chat.format(currentListing.item.getType().name()))
                    .append(Chat.format(" <gold>for $" + Chat.formatMoney(currentListing.currentBid))));
            player.sendMessage("");
        }

        try {
            plugin.getManager().getHistory().createHistoricalItem(currentListing.item, currentListing.highestBidder, currentListing.currentBid);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        plugin.getLogger().info("Successfully ended a current listing (winner:" + currentListing.highestBidder + ", sold_for:$" + currentListing.currentBid + ", total_bids:" + currentListing.totalBids + ")");
        plugin.getManager().currentListing = null;
    }

    public enum BAHItemStatus {
        COUNTING, DISPLAYING;
    }
}
