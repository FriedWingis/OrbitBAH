package me.friedwingis.plugin.orbitbah.managers;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.friedwingis.plugin.orbitbah.OrbitBAH;
import me.friedwingis.plugin.orbitbah.struct.BlackMarketHistory;
import me.friedwingis.plugin.orbitbah.struct.CurrentListing;
import me.friedwingis.plugin.orbitcore.objects.JSONLootTable;
import me.friedwingis.plugin.orbitcore.utils.Chat;
import me.friedwingis.plugin.orbitcore.utils.EconomyUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Copyright FriedWingis - 2023
 * All code is private and not to be used by any
 * other entity unless explicitly stated otherwise.
 **/
@Getter
public class BlackMarketManager {
    private final OrbitBAH plugin;
    private final JSONLootTable lootTable;
    private final BlackMarketHistory history;

    public CurrentListing currentListing;

    public BlackMarketManager(final OrbitBAH plugin) throws IOException {
        this.plugin = plugin;

        this.lootTable = new JSONLootTable(new File(plugin.getDataFolder(), "loot_table.json"), new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create());
        this.lootTable.load();

        this.history = new BlackMarketHistory(plugin);
        this.history.load();
    }

    public void save() throws IOException {
        this.lootTable.save();
        this.history.saveFiles();
    }

    public void bid(final Player player) {
        final int bid = currentListing.currentBid + currentListing.bidIncrement;
        if (!EconomyUtils.has(player, bid))
            return;

        final OfflinePlayer previousHB = Bukkit.getOfflinePlayerIfCached(currentListing.highestBidder);
        if (previousHB != null && previousHB.getPlayer() != null) {
            EconomyUtils.deposit(previousHB, currentListing.previousBid);
            previousHB.getPlayer().sendMessage(Chat.format("<red><b>[/BAH]</b> You have been outbid by " + player.getName() + "!"));
        }

        currentListing.highestBidder = player.getName();
        currentListing.currentBid = bid;
        currentListing.previousBid = currentListing.currentBid;
        currentListing.totalBids++;

        if (plugin.getScheduler().count <= 10)
            plugin.getScheduler().count += 10;

        if (currentListing.currentBid == 1000000 || currentListing.currentBid == 10000000 || currentListing.currentBid == 1000000000) {
            if (currentListing.currentBid == 1000000)
                currentListing.bidIncrement = 500000;
            else if (currentListing.currentBid == 10000000)
                currentListing.bidIncrement = 1000000;
            else
                currentListing.bidIncrement = 10000000;

            Bukkit.broadcast(Chat.format("<gold><b>[/BAH]</b> " + player.getName() + " has bid $" + Chat.formatMoney(bid) + " on ")
                    .append((currentListing.item.hasItemMeta() && currentListing.item.getItemMeta().hasDisplayName()) ? Objects.requireNonNull(currentListing.item.getItemMeta().displayName()) : Chat.format(currentListing.item.getType().name()))
                    .append(Chat.format("<gold>, the bid increment is now $" + Chat.formatMoney(currentListing.bidIncrement) + "!")));
        }

        player.sendMessage(Chat.format("<gold><b>[/BAH]</b> You have bid $" + Chat.formatMoney(bid) + " on <white>").append((currentListing.item.hasItemMeta() && currentListing.item.getItemMeta().hasDisplayName()) ? Objects.requireNonNull(currentListing.item.getItemMeta().displayName()) : Chat.format(currentListing.item.getType().name())));

        plugin.getLogger().info(player.getName() + " has placed a bid on the current listing (new_cur_bid:$" + currentListing.currentBid + ", new_bid_increment:$" + currentListing.bidIncrement);
    }
}
