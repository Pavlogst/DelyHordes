package com.sweityuo.delyHordes.mobs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobManager {
    List<Mob> mobsList = new ArrayList<>();

    public MobManager(ConfigurationSection mobs) {
        if (mobs == null) return;

        for (String key : mobs.getKeys(false)) {
            ConfigurationSection i = mobs.getConfigurationSection(key);
            if (i == null) continue;

            String id = key;
            EntityType type = EntityType.valueOf(i.getString("type"));
            String name = i.getString("customName");

            ConfigurationSection stats = i.getConfigurationSection("stats");
            double health = stats != null ? stats.getDouble("health") : 20;
            double attackDamage = stats != null ? stats.getDouble("attackDamage") : 2;

            List<PotionEffect> effects = new ArrayList<>();
            List<String> effectsList = i.getStringList("effects");

            for (String line : effectsList) {
                try {
                    String[] parts = line.split(";");

                    PotionEffectType effectType = PotionEffectType.getByName(parts[0].toUpperCase());
                    int amplifier = Integer.parseInt(parts[1]) - 1;

                    if (effectType != null) {
                        effects.add(new PotionEffect(effectType, Integer.MAX_VALUE, amplifier));
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Ошибка загрузки эффекта: " + line);
                }
            }

            ConfigurationSection equipment = i.getConfigurationSection("equipment");

            ItemStack mainHand = null;
            ItemStack offHand = null;
            ItemStack helmet = null;
            ItemStack chestplate = null;
            ItemStack leggings = null;
            ItemStack boots = null;

            if (equipment != null) {

                mainHand = loadItem(equipment.getConfigurationSection("mainHand"));
                offHand = loadItem(equipment.getConfigurationSection("offHand"));

                ConfigurationSection armor = equipment.getConfigurationSection("armor");
                if (armor != null) {
                    helmet = loadItem(armor.getConfigurationSection("helmet"));
                    chestplate = loadItem(armor.getConfigurationSection("chestplate"));
                    leggings = loadItem(armor.getConfigurationSection("leggings"));
                    boots = loadItem(armor.getConfigurationSection("boots"));
                }
            }

            Mob mob = new Mob(
                    type,
                    health,
                    attackDamage,
                    name,
                    effects,
                    mainHand,
                    offHand,
                    helmet,
                    chestplate,
                    leggings,
                    boots,
                    id
            );

            mobsList.add(mob);
        }
    }


    private ItemStack loadItem(ConfigurationSection section) {
        if (section == null) return null;

        String materialName = section.getString("material");
        if (materialName == null) return null;

        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            return new ItemStack(material);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Неизвестный материал: " + materialName);
            return null;
        }
    }

    public Mob getMob(String id) {
        for (Mob mob : mobsList) {
            if(mob.getId().equals(id)){
                return mob;
            }
        }
        return null;
    }
}
