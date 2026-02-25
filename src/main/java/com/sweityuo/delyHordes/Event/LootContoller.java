package com.sweityuo.delyHordes.Event;

import com.sweityuo.delyHordes.Main;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LootContoller {

    public void addItem(Player player, float chance, Main plugin) {
        YamlConfiguration loot = plugin.getLootFile().getConfig();
        String path = "loot.items";

        List<Map<?, ?>> rawList = loot.getMapList(path);
        List<Map<String, Object>> items = new ArrayList<>();

        for (Map<?, ?> map : rawList) {
            items.add(new HashMap<>((Map<String, Object>) map));
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            plugin.getMessageManager().sendLootNoItemInHand(player);
            return;
        }

        if (chance <= 0) {
            player.sendMessage("§cШанс должен быть больше 0!");
            return;
        }

        if (chance > 1) {
            chance = 1;
        }

        String encoded = encodeItem(item);
        if (encoded == null) return;

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("item", encoded);
        itemData.put("chance", chance);

        items.add(itemData);

        loot.set(path, items);
        plugin.getLootFile().save();

        plugin.getMessageManager().sendLootAdded(player, chance);
    }






    public void removeItem(Player player, Main plugin) {
        YamlConfiguration loot = plugin.getLootFile().getConfig();

        String path = "loot.items";
        if (!loot.isList(path)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            plugin.getMessageManager().sendLootNoItemInHand(player);
            return;
        }

        String encoded = encodeItem(item);

        List<Map<?, ?>> items = loot.getMapList(path);

        boolean removed = items.removeIf(map ->
                encoded.equals(map.get("item"))
        );

        if (!removed) {
            player.sendMessage("§cТакой предмет не найден в луте.");
            return;
        }

        loot.set(path, items);
        plugin.getLootFile().save();

        plugin.getMessageManager().sendLootRemoved(player);
    }


    public String encodeItem(ItemStack item) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            BukkitObjectOutputStream data = new BukkitObjectOutputStream(output);

            data.writeObject(item);
            data.close();

            return Base64.getEncoder().encodeToString(output.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    Map<?, ?> rollByChance(List<Map<?, ?>> items) {

        double totalChance = 0;

        for (Map<?, ?> map : items) {
            totalChance += ((Number) map.get("chance")).doubleValue();
        }

        double random = Math.random() * totalChance;
        double current = 0;

        for (Map<?, ?> map : items) {
            current += ((Number) map.get("chance")).doubleValue();
            if (random <= current) {
                return map;
            }
        }

        return null;
    }
    public void spawnItem(Location loc, Main plugin) {
        YamlConfiguration loot = plugin.getLootFile().getConfig();
        String path = "loot.items";

        List<Map<?, ?>> list = loot.getMapList(path);
        if (list == null || list.isEmpty()) return;

        Map<?, ?> selected = rollByChance(list);
        if (selected == null) return;

        String encoded = (String) selected.get("item");
        ItemStack item = decodeItem(encoded);
        if (item == null) return;

        loc.getWorld().dropItemNaturally(loc, item);
    }
    public ItemStack decodeItem(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream data = new BukkitObjectInputStream(input);

            ItemStack item = (ItemStack) data.readObject();
            data.close();
            return item;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
