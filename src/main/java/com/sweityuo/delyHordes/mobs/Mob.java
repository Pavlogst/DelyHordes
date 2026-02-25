package com.sweityuo.delyHordes.mobs;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class Mob {
    private EntityType type;
    private double health;
    private double damage;
    private String customName;
    private List<PotionEffect> effects;

    public Mob(EntityType type, double health, double damage, String customName, List<PotionEffect> effects) {
        this.type = type;
        this.health = health;
        this.damage = damage;
        this.customName = customName;
        this.effects = effects;
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
}
