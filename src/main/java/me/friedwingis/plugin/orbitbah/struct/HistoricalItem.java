package me.friedwingis.plugin.orbitbah.struct;

import me.friedwingis.plugin.orbitcore.utils.Base64Utils;
import me.friedwingis.plugin.orbitcore.utils.Chat;
import me.friedwingis.plugin.orbitcore.utils.TimeUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright FriedWingis - 2023
 * All code is private and not to be used by any
 * other entity unless explicitly stated otherwise.
 **/
public class HistoricalItem {
    private final ItemStack original;
    private final String winner, winningBid;
    public final long whenWon;

    public HistoricalItem(final String original, final String winner, final String winningBid, final long whenWon) throws IOException {
        this.original = Base64Utils.fromBase64(original);
        this.winner = winner;
        this.winningBid = winningBid;
        this.whenWon = whenWon;
    }

    public ItemStack getAsDisplayItem() throws IOException {
        final ItemStack item = new ItemStack(original);
        final ItemMeta meta = item.getItemMeta();

        final List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        lore.add(Chat.EMPTY_STRING);
        lore.add(Chat.format("<gray>------------------------"));
        if (winner.equals("NONE"))
            lore.add(Chat.format("<red>No players bid on this item."));
        else {
            lore.add(Chat.format("<gold>Winning bidder: <yellow>" + winner));
            lore.add(Chat.format("<gold>Winning bid: <yellow>$" + winningBid));
            lore.add(Chat.format("<gold>Item sold <b>" + TimeUtils.getFormattedTime((int) ((System.currentTimeMillis() - whenWon) / 1000)) + "</b> ago!"));
        }
        lore.add(Chat.format("<gray>------------------------"));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public String toString() {
        try {
            return Base64Utils.toBase64(original) + ";" + winner + ";" + winningBid + ";" + whenWon;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
