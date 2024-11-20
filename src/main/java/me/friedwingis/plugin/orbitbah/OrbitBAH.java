package me.friedwingis.plugin.orbitbah;

import lombok.Getter;
import me.friedwingis.plugin.orbitbah.commands.BAHAdminCommand;
import me.friedwingis.plugin.orbitbah.commands.BAHCommand;
import me.friedwingis.plugin.orbitbah.managers.BlackMarketManager;
import me.friedwingis.plugin.orbitbah.struct.BlackMarketHistory;
import me.friedwingis.plugin.orbitbah.struct.BlackMarketLootPoolGUI;
import me.friedwingis.plugin.orbitbah.struct.BlackMarketScheduler;
import me.friedwingis.plugin.orbitcore.OrbitCore;
import me.friedwingis.plugin.orbitcore.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

@Getter
public final class OrbitBAH extends JavaPlugin {
    public static final Integer
            DISPLAYING_EVERY = 30 * 60,
            DISPLAYING_FOR = 5 * 60;

    private BlackMarketScheduler scheduler;
    private BlackMarketManager manager;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();

        this.scheduler = new BlackMarketScheduler();
        this.scheduler.begin(this);

        try {
            this.manager = new BlackMarketManager(this);
        } catch (IOException e) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        OrbitCore._I.getCommandHandler().register(
                new BAHCommand(this), new BAHAdminCommand(this)
        );

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            private void onClick(final InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {
                    final InventoryHolder holder = event.getView().getTopInventory().getHolder();
                    if (holder instanceof BlackMarketLootPoolGUI menu) {
                        event.setCancelled(true);
                        event.setResult(Event.Result.DENY);

                        menu.handleMenuButtons(event);
                    } else if (holder instanceof BlackMarketHistory.BlackMarketHistoryInventory menu) {
                        event.setCancelled(true);
                        event.setResult(Event.Result.DENY);

                        menu.handleMenuButtons(event);
                    }
                }
            }
        }, this);
    }

    @Override
    public void onDisable() {
        if (scheduler.status == BlackMarketScheduler.BAHItemStatus.DISPLAYING) {
            scheduler.endCurrentListing();
            Bukkit.broadcast(Chat.format("<gold><b>[/BAH]</b> The current listing has ended early due to server restart."));
        }

        try {
            this.manager.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
