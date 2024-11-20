package me.friedwingis.plugin.orbitbah.struct;

import me.friedwingis.plugin.orbitcore.objects.JSONLootTable;
import me.friedwingis.plugin.orbitcore.objects.PaginatedDisplayMenu;
import me.friedwingis.plugin.orbitcore.objects.SavedJSONItemStack;
import me.friedwingis.plugin.orbitcore.utils.Chat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Copyright FriedWingis - 2023
 * All code is private and not to be used by any
 * other entity unless explicitly stated otherwise.
 **/
public class BlackMarketLootPoolGUI extends PaginatedDisplayMenu {
    private final JSONLootTable lootTable;

    public BlackMarketLootPoolGUI(final JSONLootTable lootTable) throws IOException {
        super("/bah loot", 54, lootTable.asEditableItemStacks());
        this.lootTable = lootTable;
    }

    @Override
    public void handleMenuButtons(InventoryClickEvent e) {
        super.handleMenuButtons(e);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;

        final Player player = (Player) e.getWhoClicked();

        final PersistentDataContainer container = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
        final NamespacedKey key = new NamespacedKey("andromeda", "and_loot");

        if (e.getClick() == ClickType.SHIFT_RIGHT) {
            if (container.has(key, PersistentDataType.STRING) && player.hasPermission("admin.OrbitBAH")) {
                final UUID u = UUID.fromString(container.get(key, PersistentDataType.STRING));
                final boolean remove = remove(lootTable.getPool(), u);

                if (remove) {
                    player.sendMessage(Chat.praise("Successfully removed item from loot pool!"));
                    player.closeInventory();
                    player.performCommand("bahadmin loot");
                } else
                    player.sendMessage(Chat.severe("Could not remove item. getFromUUID is null, see Fried for more info."));
            }
        }
    }

    public boolean remove(final TreeMap<Double, SavedJSONItemStack> map, final UUID uuid) {
        final Iterator<Map.Entry<Double, SavedJSONItemStack>> iter = map.entrySet().iterator();

        while (iter.hasNext()) {
            final Map.Entry<Double, SavedJSONItemStack> entry = iter.next();
            final SavedJSONItemStack mapValue = entry.getValue();

            if (mapValue.uuid.equals(uuid)) {
                iter.remove();
                return true;
            }
        }

        return false;
    }
}
