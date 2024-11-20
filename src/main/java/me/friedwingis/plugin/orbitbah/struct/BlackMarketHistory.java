package me.friedwingis.plugin.orbitbah.struct;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.friedwingis.plugin.orbitbah.OrbitBAH;
import me.friedwingis.plugin.orbitcore.objects.PaginatedDisplayMenu;
import me.friedwingis.plugin.orbitcore.utils.Base64Utils;
import me.friedwingis.plugin.orbitcore.utils.Chat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Copyright FriedWingis - 2023
 * All code is private and not to be used by any
 * other entity unless explicitly stated otherwise.
 **/
public class BlackMarketHistory {
    private final OrbitBAH plugin;
    private final List<HistoricalItem> historicalItems;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public BlackMarketHistory(final OrbitBAH plugin) {
        this.plugin = plugin;
        this.historicalItems = Lists.newArrayList();
    }

    public void load() throws IOException {
        final File file = new File(plugin.getDataFolder(), "history.json");
        file.createNewFile();

        try (FileReader reader = new FileReader(file)) {
            for (final String s : (List<String>) gson.fromJson(reader, new TypeToken<List<String>>() {}.getType())) {
                final String[] split = s.split(";");
                historicalItems.add(new HistoricalItem(split[0], split[1], split[2], Long.parseLong(split[3])));
            }

            plugin.getLogger().info("Successfully loaded all historical items!");
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().warning("Unable to load historical items! Could they be corrupted?");
        }
    }

    public void saveFiles() throws IOException {
        final File cLB = new File(plugin.getDataFolder(), "history.json");
        cLB.createNewFile();

        try (FileWriter writer = new FileWriter(cLB)) {
            final List<String> contents = Lists.newArrayList();
            historicalItems.forEach(historicalItem -> {
                contents.add(historicalItem.toString());
            });

            gson.toJson(contents, writer);
            writer.flush();
            writer.close();

            plugin.getLogger().info("Successfully saved all historical items");
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().warning("Unable to save historical items! Could they be corrupted?");
        }
    }

    public boolean exists() {
        return !historicalItems.isEmpty();
    }

    public void createHistoricalItem(final ItemStack won, final String winner, final int winningBid) throws IOException {
        historicalItems.add(new HistoricalItem(Base64Utils.toBase64(won), winner, Chat.formatMoney(winningBid), System.currentTimeMillis()));
    }

    public void openHistoryInventory(final Player player) {
        final List<HistoricalItem> copy = Lists.newLinkedList(this.historicalItems);
        copy.sort(Comparator.comparingInt(h -> (int) h.whenWon));

        final List<ItemStack> contents = Lists.newLinkedList();
        copy.forEach(historicalItem -> {
            try {
                contents.add(historicalItem.getAsDisplayItem());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Collections.reverse(contents);
        new BlackMarketHistoryInventory(contents).open(player, 1);
    }

    public static class BlackMarketHistoryInventory extends PaginatedDisplayMenu {

        public BlackMarketHistoryInventory(final List<ItemStack> contents) {
            super("Black Market Auction History", 54, contents);
        }
    }
}
