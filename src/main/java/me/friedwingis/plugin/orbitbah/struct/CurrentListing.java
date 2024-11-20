package me.friedwingis.plugin.orbitbah.struct;

import me.friedwingis.plugin.orbitcore.objects.SavedJSONItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.UUID;

/**
 * Copyright FriedWingis - 2023
 * All code is private and not to be used by any
 * other entity unless explicitly stated otherwise.
 **/
public class CurrentListing {
    public final ItemStack item;
    public String highestBidder;
    public int currentBid, previousBid, bidIncrement, totalBids;

    public CurrentListing(final ItemStack item) {
        this.item = item;
        this.highestBidder = "NONE";
        this.currentBid = 0;
        this.previousBid = 0;
        this.bidIncrement = 100000;
        this.totalBids = 0;
    }

    public SavedJSONItemStack build() throws IOException {
        return new SavedJSONItemStack(UUID.randomUUID(), item, 100.0, "NULL");
    }
}
