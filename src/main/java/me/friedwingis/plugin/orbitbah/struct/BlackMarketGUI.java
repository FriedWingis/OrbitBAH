package me.friedwingis.plugin.orbitbah.struct;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.friedwingis.plugin.orbitbah.OrbitBAH;
import me.friedwingis.plugin.orbitcore.objects.ItemBuilder;
import me.friedwingis.plugin.orbitcore.utils.Chat;
import me.friedwingis.plugin.orbitcore.utils.EconomyUtils;
import me.friedwingis.plugin.orbitcore.utils.InventoryUtils;
import me.friedwingis.plugin.orbitcore.utils.TimeUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Copyright FriedWingis - 2023
 * All code is private and not to be used by any
 * other entity unless explicitly stated otherwise.
 **/
public class BlackMarketGUI extends ChestGui {
    public static final Map<UUID, BlackMarketGUI> VIEWERS = Maps.newHashMap();

    private final OrbitBAH plugin;
    private final StaticPane pane;

    public BlackMarketGUI(final OrbitBAH plugin) {
        super(1, "Black Market Auction");
        this.plugin = plugin;
        this.pane = new StaticPane(9, 1);

        setOnGlobalClick(event -> event.setCancelled(true));
        setOnClose(closeEvent -> VIEWERS.remove(closeEvent.getPlayer().getUniqueId()));

        for (int i = 0; i < (pane.getHeight() * pane.getLength()); i++) {
            final Slot slot = Slot.fromIndex(i);

            if (i == 0)
                pane.addItem(getHistory(), slot);
            else if (i == 4)
                pane.addItem(plugin.getScheduler().status == BlackMarketScheduler.BAHItemStatus.COUNTING ? new GuiItem(getNextAuction()) : getMiddleItem(), slot);
            else if (i == 8)
                pane.addItem(getBook(), slot);
            else
                pane.addItem(new GuiItem(InventoryUtils.FILLER_PANE_GRAY), slot);
        }

        addPane(pane);
    }

    public void open(final Player player) {
        show(player);
        VIEWERS.put(player.getUniqueId(), this);
    }

    public void refreshMiddleItem() {
        final Slot slot = Slot.fromIndex(4);

        pane.removeItem(slot);
        pane.addItem(getMiddleItem(), slot);

        update();
    }

    public GuiItem getMiddleItem() {
        final CurrentListing currentListing = plugin.getManager().currentListing;
        final ItemStack st = currentListing.item.clone();

        final ItemMeta meta = st.getItemMeta();
        final List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();

        lore.add(Chat.format(""));
        lore.add(Chat.format("<grey>------------------------"));
        lore.add(Chat.format("<gold><bold>CLICK</bold></gold> <grey>item to bid <green>$" + Chat.formatMoney(currentListing.currentBid + currentListing.bidIncrement) + "!"));
        lore.add(Chat.format(""));
        lore.add(Chat.format("<gold>Current bid: <yellow>$" + Chat.formatMoney(currentListing.currentBid)));
        lore.add(Chat.format("<gold>Highest bidder: <yellow>" + currentListing.highestBidder));
        lore.add(Chat.format("<gold>Bid increment: <yellow>$" + Chat.formatMoney(currentListing.bidIncrement)));
        lore.add(Chat.format("<gold>Bidding ends in: <yellow>" + TimeUtils.getFormattedTime(plugin.getScheduler().count)));
        lore.add(Chat.format("<grey>------------------------"));

        meta.lore(lore);
        st.setItemMeta(meta);

        return new GuiItem(st, event -> {
            if (event.getWhoClicked() instanceof Player player) {
                if (event.getRawSlot() == 4 && plugin.getScheduler().status == BlackMarketScheduler.BAHItemStatus.DISPLAYING) {
                    final CurrentListing cL = plugin.getManager().currentListing;
                    final int bid = cL.currentBid + cL.bidIncrement;

                    if (player.getName().equalsIgnoreCase(cL.highestBidder)) {
                        player.sendMessage(Chat.severe("You are the highest bidder!"));
                        return;
                    }

                    if (!EconomyUtils.has(player, bid)) {
                        player.sendMessage(Chat.format("You do not have the required $" + Chat.formatMoney(bid) + "!"));
                        return;
                    }

                    final ChestGui gui = getConfirmGUI(st, bid);
                    gui.show(player);
                }
            }
        });
    }

    private ChestGui getConfirmGUI(final ItemStack currentListing, final double bid) {
        final ChestGui gui = new ChestGui(1, "Bid of $" + Chat.formatMoney(bid));
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        gui.setOnClose(closeEvent -> {
            if (closeEvent.getPlayer() instanceof Player player && closeEvent.getReason() != InventoryCloseEvent.Reason.OPEN_NEW)
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> open(player), 1L);
        });

        final OutlinePane p2 = new OutlinePane(9, 1);
        for (int i = 0; i <= (p2.getHeight() * p2.getLength()); i++) {
            if (i >= 0 && i < 4) {
                p2.addItem(new GuiItem(new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                        .setDisplayName("<gradient:green:dark_green><bold>Confirm")
                        .setLoreC(Chat.format("<gray>Confirm bid on"), (currentListing.hasItemMeta() && currentListing.getItemMeta().hasDisplayName()) ? currentListing.getItemMeta().displayName() : Chat.format(currentListing.getType().name()))
                        .build(),

                        event -> {
                            if (event.getWhoClicked() instanceof Player player) {
                                if (bid < plugin.getManager().currentListing.currentBid)
                                    player.sendMessage(Chat.severe("You have been outbid."));
                                else
                                    plugin.getManager().bid(player);

                                open(player);
                            }
                        })
                );
            } else if (i > 4) {
                p2.addItem(new GuiItem(new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                        .setDisplayName("<gradient:red:dark_red><bold>Cancel")
                        .setLoreC(Chat.format("<gray>Cancel bid on"), (currentListing.hasItemMeta() && currentListing.getItemMeta().hasDisplayName()) ? currentListing.getItemMeta().displayName() : Chat.format(currentListing.getType().name()))
                        .build(),

                        event -> {
                            if (event.getWhoClicked() instanceof Player player) {
                                open(player);
                                player.sendMessage(Chat.severe("You have canceled your bid!"));
                            }
                        })
                );
            } else
                p2.addItem(new GuiItem(currentListing));
        }

        gui.addPane(p2);

        return gui;
    }

    private GuiItem getHistory() {
        final ItemStack item = new ItemBuilder(Material.LEGACY_BOOK_AND_QUILL)
                .setDisplayName("<gradient:yellow:gold><b>Black Market History")
                .setLore("<gray>Click to view the /bah history")
                .build();

        return new GuiItem(item, event -> {
            if (event.getWhoClicked() instanceof Player player) {
                final BlackMarketHistory history = plugin.getManager().getHistory();
                if (history.exists())
                    plugin.getManager().getHistory().openHistoryInventory(player);
                else
                    player.sendMessage(Chat.severe("No /bah history exists yet!"));
            }
        });
    }

    public ItemStack getNextAuction() {
        return new ItemBuilder(Material.BARRIER)
                .setDisplayName("<gradient:red:dark_red><bold>???")
                .setLore("<red>Next auction in <u>" + TimeUtils.getFormattedTime(plugin.getScheduler().count) + "</u>...")
                .build();
    }

    private GuiItem getBook() {
        return new GuiItem(new ItemBuilder(Material.BOOK)
                .setDisplayName("<gradient:yellow:gold><bold>Black Market Auction")
                .setLore(
                        "<grey>Every 30 minutes, a <u>random rare item</u> will be",
                        "<grey>listed on the /bah for players to bid on.",
                        "",
                        "<grey>The bid increments will <u>increase as the",
                        "<grey><u>max bid increases</u>, with a <white>10s anti-snipe",
                        "<grey><white>timer</white> added each time a bid is placed.",
                        "",
                        "<grey>The player with the highest bid at the",
                        "<grey>end of the auction will receive the item."
                )
                .build());
    }
}
