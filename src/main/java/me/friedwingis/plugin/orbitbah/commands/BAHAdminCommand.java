package me.friedwingis.plugin.orbitbah.commands;

import lombok.AllArgsConstructor;
import me.friedwingis.plugin.orbitbah.OrbitBAH;
import me.friedwingis.plugin.orbitbah.struct.BlackMarketLootPoolGUI;
import me.friedwingis.plugin.orbitbah.struct.BlackMarketScheduler;
import me.friedwingis.plugin.orbitcore.objects.SavedJSONItemStack;
import me.friedwingis.plugin.orbitcore.utils.Chat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.IOException;
import java.util.UUID;

/**
 * Copyright FriedWingis - 2023
 * All code is private and not to be used by any
 * other entity unless explicitly stated otherwise.
 **/
@Command("bahadmin")
@CommandPermission("admin.OrbitBAH")
@AllArgsConstructor
public class BAHAdminCommand {
    private final OrbitBAH plugin;

    @DefaultFor("bahadmin")
    private void onBase(final BukkitCommandActor actor) {
        actor.reply(Chat.severe("Usage: /bahadmin <start/loot/additem/forcesave>"));
    }

    @Subcommand("loot")
    private void onLootSub(final Player player) {
        if (plugin.getManager().getLootTable().getPool().isEmpty()) {
            player.sendMessage(Chat.severe("No loot items exist yet! Add one via /bahadmin additem!"));
            return;
        }

        try {
            final BlackMarketLootPoolGUI poolGUI = new BlackMarketLootPoolGUI(plugin.getManager().getLootTable());
            poolGUI.open(player, 1);
        } catch (IOException e) {
            player.sendMessage(Chat.severe("An error has occurred while attempting to open the loot table. Please see Fried or check console.. If ya even got da access :P"));
            throw new RuntimeException(e);
        }
    }

    @Subcommand("additem")
    private void onAddItemSub(final Player player, final double weight) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(Chat.severe("You must be holding an item in order to execute this command!"));
            return;
        }

        try {
            final SavedJSONItemStack savedSlotItem = new SavedJSONItemStack(UUID.randomUUID(), item, weight, "NULL");
            plugin.getManager().getLootTable().saveItem(player, savedSlotItem);
        } catch (final Exception e) {
            player.sendMessage(Chat.severe("Usage: /bahadmin additem <chance>"));
        }
    }

    @Subcommand("forcesave")
    private void onForceSaveSub(final BukkitCommandActor actor) {
        try {
            plugin.getManager().getLootTable().save();
            plugin.getManager().getLootTable().load();

            actor.reply(Chat.praise("Reload of loot table complete."));
        } catch (final Exception e) {
            actor.reply(Chat.severe("Error while attempting to reload loot_table.json. contact fried, or look at error in console if you have permission."));
            e.printStackTrace();
        }
    }

    @Subcommand("start")
    private void onStartSub(final BukkitCommandActor actor) {
        if (plugin.getScheduler().status == BlackMarketScheduler.BAHItemStatus.DISPLAYING) {
            actor.reply(Chat.severe("BAH current listing is already active!"));
            return;
        }

        try {
            plugin.getScheduler().createNewListing();
            actor.reply(Chat.praise("Successfully created a new current listing!"));
        } catch (final Exception e) {
            actor.reply(Chat.severe("Error while attempting to create a new current listing. Contact Fried, or look at error in console if you have permission."));
            e.printStackTrace();
        }
    }
}
