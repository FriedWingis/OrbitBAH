package me.friedwingis.plugin.orbitbah.commands;

import lombok.RequiredArgsConstructor;
import me.friedwingis.plugin.orbitbah.OrbitBAH;
import me.friedwingis.plugin.orbitbah.struct.BlackMarketGUI;
import me.friedwingis.plugin.orbitbah.struct.BlackMarketHistory;
import me.friedwingis.plugin.orbitcore.utils.Chat;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;

/**
 * Copyright FriedWingis - 2023
 * All code is private and not to be used by any
 * other entity unless explicitly stated otherwise.
 **/
@Command({"bah", "blackmarketauctionhouse"})
@RequiredArgsConstructor
public class BAHCommand {
    private final OrbitBAH plugin;

    @DefaultFor({"bah", "blackmarketauctionhouse"})
    private void onBAHCommand(final Player player) {
        final BlackMarketGUI gui = new BlackMarketGUI(plugin);
        gui.open(player);
    }

    @Subcommand("history")
    private void onHistorySub(final Player player) {
        final BlackMarketHistory history = plugin.getManager().getHistory();
        if (history.exists())
            plugin.getManager().getHistory().openHistoryInventory(player);
        else
            player.sendMessage(Chat.severe("No /bah history exists yet!"));
    }
}
