package com.sweityuo.delyHordes.utils;

import com.sweityuo.delyHordes.Main;
import org.bukkit.entity.LivingEntity;

import com.sweityuo.delyHordes.mobs.Mob;
import com.sweityuo.delyHordes.mobs.MobManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WavesUtil {
    private List<LivingEntity> mobs = new ArrayList<>();
    private int spawnedMob = 0;
    private int killedMob = 0;
    private int currentMobsAmount = 0;

    private int MAX_ALIVE;

    private final Main main;
    private final MobManager mobManager;

    private Location pos1;
    private Location pos2;
    private World world;
    private int timeSpawn;
    private int totalToSpawn;

    private BukkitRunnable task;

    public WavesUtil(Main main, MobManager mobManager) {
        this.main = main;
        this.mobManager = mobManager;
        loadConfig();
    }

    public void start() {
        stop();

        task = new BukkitRunnable() {
            @Override
            public void run() {

                if (spawnedMob >= totalToSpawn && currentMobsAmount <= 0) {
                    cancel();
                    return;
                }

                if (currentMobsAmount >= MAX_ALIVE) return;

                if (spawnedMob >= totalToSpawn) return;

                spawnOneMob();
            }
        };

        task.runTaskTimer(main, 0L, timeSpawn);
    }
    public void reload() {
        stop();
        loadConfig();
    }

    private void spawnOneMob() {
        Location loc = getSafeLocation();
        if (loc == null) return;

        Mob mobData = mobManager.getRandomMob();

        LivingEntity entity = (LivingEntity) world.spawnEntity(loc, mobData.getType());

        if (mobData.getCustomName() != null) {
            entity.setCustomName(ColorUtil.colorize(mobData.getCustomName()));
            entity.setCustomNameVisible(true);
        }

        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(mobData.getHealth());
        entity.setHealth(mobData.getHealth());

        for (PotionEffect effect : mobData.getEffects()) {
            entity.addPotionEffect(effect);
        }

        entity.setRemoveWhenFarAway(false);

        entity.getScoreboardTags().add("DELY_HORDE");

        spawnedMob++;
        currentMobsAmount++;
        mobs.add(entity);
    }


    public void onMobDeath(LivingEntity entity) {
        if(mobs.contains(entity)) {
            mobs.remove(entity);
        }
        killedMob++;
        currentMobsAmount--;
    }


    private Location getSafeLocation() {
        Random r = new Random();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int tries = 0; tries < 20; tries++) {

            int x = r.nextInt(maxX - minX + 1) + minX;
            int z = r.nextInt(maxZ - minZ + 1) + minZ;

            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);

            if (loc.getBlock().getType().isAir()
                    && loc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                return loc;
            }
        }

        return null;
    }


    private void loadConfig() {
        ConfigurationSection g = main.getConfig().getConfigurationSection("global");
        if (g == null) return;

        world = Bukkit.getWorld(g.getString("world"));

        pos1 = new Location(world,
                g.getConfigurationSection("poss1").getDouble("x"),
                g.getConfigurationSection("poss1").getDouble("y"),
                g.getConfigurationSection("poss1").getDouble("z"));

        pos2 = new Location(world,
                g.getConfigurationSection("poss2").getDouble("x"),
                g.getConfigurationSection("poss2").getDouble("y"),
                g.getConfigurationSection("poss2").getDouble("z"));

        totalToSpawn = g.getInt("mobsAmount");
        MAX_ALIVE = g.getInt("maxMobAmountInMoment");
        timeSpawn = g.getInt("mobSpawnTime");

    }

    public void stop(){
        for (LivingEntity entity : mobs) {
            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
        }

        mobs.clear();

        spawnedMob = 0;
        killedMob = 0;
        currentMobsAmount = 0;

        if (task != null) {
            task.cancel();
            task = null;
        }
    }


    public boolean isPlayerInZone(Player player) {
        if (player == null || world == null || pos1 == null || pos2 == null) return false;
        if (!player.getWorld().equals(world)) return false;

        Location loc = player.getLocation();

        int radius = 10;

        double minX = Math.min(pos1.getX(), pos2.getX()) - radius;
        double maxX = Math.max(pos1.getX(), pos2.getX()) + radius;
        double minY = Math.min(pos1.getY(), pos2.getY()) - radius;
        double maxY = Math.max(pos1.getY(), pos2.getY()) + radius;
        double minZ = Math.min(pos1.getZ(), pos2.getZ()) - radius;
        double maxZ = Math.max(pos1.getZ(), pos2.getZ()) + radius;

        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    public boolean isActive(){
        if(task == null) return false;
        if(task.isCancelled()) return false;
        else return true;
    }
    public int getLeftAmount() {
        return totalToSpawn - killedMob;
    }
}
