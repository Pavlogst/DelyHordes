package com.sweityuo.delyHordes.mobs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
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

            Mob mob = new Mob(type, health, attackDamage, name, effects);
            mobsList.add(mob);
        }
    }

    public Mob getRandomMob() {
        Random rand = new Random();
        int index = rand.nextInt(mobsList.size());
        return mobsList.get(index);
    }
}
