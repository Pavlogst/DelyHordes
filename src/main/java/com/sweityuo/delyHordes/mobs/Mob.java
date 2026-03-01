package com.sweityuo.delyHordes.mobs;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Mob {

    private final String id;
    private final EntityType type;
    private final double health;
    private final double damage;
    private final String customName;
    private final List<PotionEffect> effects;

    // 🆕 экипировка
    private final ItemStack mainHand;
    private final ItemStack offHand;

    private final ItemStack helmet;
    private final ItemStack chestplate;
    private final ItemStack leggings;
    private final ItemStack boots;


    public Mob(
            EntityType type,
            double health,
            double damage,
            String customName,
            List<PotionEffect> effects,

            ItemStack mainHand,
            ItemStack offHand,
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            String id
    ) {
        this.id = id;
        this.type = type;
        this.health = health;
        this.damage = damage;
        this.customName = customName;
        this.effects = effects;

        this.mainHand = mainHand;
        this.offHand = offHand;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;


    }


    
    public EntityType getType() {
        return type;
    }

    public double getHealth() {
        return health;
    }

    public double getDamage() {
        return damage;
    }

    public String getCustomName() {
        return customName;
    }

    public List<PotionEffect> getEffects() {
        return effects;
    }




    public ItemStack getMainHand() {
        return mainHand;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public ItemStack getChestplate() {
        return chestplate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }
    public String getId() {
        return id;
    }
}