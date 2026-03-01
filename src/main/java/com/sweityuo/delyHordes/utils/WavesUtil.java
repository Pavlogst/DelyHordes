package com.sweityuo.delyHordes.utils;

import com.sweityuo.delyHordes.Main;
import com.sweityuo.delyHordes.dates.WaveData;
import com.sweityuo.delyHordes.mobs.Mob;
import com.sweityuo.delyHordes.mobs.MobManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WavesUtil {

    private final List<LivingEntity> mobs = new ArrayList<>();

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

    private int currentWave = 0;
    private int totalWaves = 0;
    private final List<List<WaveData>> waves = new ArrayList<>();

    private BukkitRunnable task;

    public WavesUtil(Main main, MobManager mobManager) {
        this.main = main;
        this.mobManager = mobManager;
        loadConfig();
    }

    public void start() {
        stop();
        currentWave = 0;
        startNextWave();
    }

    public void reload() {
        stop();
        loadConfig();
    }

    private void startNextWave() {

        if (currentWave >= waves.size()) {
            main.getTopDamageUtil().finishAndReward();
            stop();
            return;
        }

        List<WaveData> waveMobs = waves.get(currentWave);

        totalToSpawn = waveMobs.stream().mapToInt(WaveData::getAmount).sum();
        spawnedMob = 0;
        killedMob = 0;
        currentMobsAmount = 0;

        task = new BukkitRunnable() {
            private final Queue<WaveData> queue = new LinkedList<>(waveMobs);
            private WaveData current;
            private int spawnedFromCurrent = 0;

            @Override
            public void run() {

                if (spawnedMob >= totalToSpawn && currentMobsAmount <= 0) {
                    cancel();
                    currentWave++;
                    startNextWave();
                    return;
                }

                if (currentMobsAmount >= MAX_ALIVE) {
                    return;
                }

                if (current == null || spawnedFromCurrent >= current.getAmount()) {
                    current = queue.poll();
                    spawnedFromCurrent = 0;

                    if (current == null) {
                        return;
                    }
                }

                spawnOneMob(current.getMobId());
                spawnedFromCurrent++;
            }
        };

        task.runTaskTimer(main, 0L, timeSpawn);
    }

    private void spawnOneMob(String mobId) {

        Location loc = getSafeLocation();
        if (loc == null) {
            return;
        }

        Mob mobData = mobManager.getMob(mobId);
        if (mobData == null) {
            return;
        }

        LivingEntity entity = (LivingEntity) world.spawnEntity(loc, mobData.getType());

        if (mobData.getCustomName() != null) {
            entity.setCustomName(ColorUtil.colorize(mobData.getCustomName()));
            entity.setCustomNameVisible(true);
        }

        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(mobData.getHealth());
        }
        entity.setHealth(mobData.getHealth());

        for (PotionEffect effect : mobData.getEffects()) {
            entity.addPotionEffect(effect);
        }

        if (entity.getEquipment() != null) {

            if (mobData.getMainHand() != null)
                entity.getEquipment().setItemInMainHand(mobData.getMainHand().clone());

            if (mobData.getOffHand() != null)
                entity.getEquipment().setItemInOffHand(mobData.getOffHand().clone());

            if (mobData.getHelmet() != null)
                entity.getEquipment().setHelmet(mobData.getHelmet().clone());

            if (mobData.getChestplate() != null)
                entity.getEquipment().setChestplate(mobData.getChestplate().clone());

            if (mobData.getLeggings() != null)
                entity.getEquipment().setLeggings(mobData.getLeggings().clone());

            if (mobData.getBoots() != null)
                entity.getEquipment().setBoots(mobData.getBoots().clone());

            entity.getEquipment().setItemInMainHandDropChance(0f);
            entity.getEquipment().setItemInOffHandDropChance(0f);
            entity.getEquipment().setHelmetDropChance(0f);
            entity.getEquipment().setChestplateDropChance(0f);
            entity.getEquipment().setLeggingsDropChance(0f);
            entity.getEquipment().setBootsDropChance(0f);
        }

        entity.setRemoveWhenFarAway(false);
        entity.getScoreboardTags().add("DELY_HORDE");

        spawnedMob++;
        currentMobsAmount++;
        mobs.add(entity);
    }

    public void onMobDeath(LivingEntity entity) {
        if (mobs.remove(entity)) {
            killedMob++;
            currentMobsAmount--;
        }
    }

    private Location getSafeLocation() {
        Random r = new Random();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int tries = 0; tries < 20; tries++) {

            int x = r.nextInt(maxX - minX + 1) + minX;
            int z = r.nextInt(maxZ - minZ + 1) + minZ;

            for (int y = maxY; y >= minY; y--) {
                Location check = new Location(world, x, y, z);

                if (!check.getBlock().getType().isAir()) {
                    Location spawn = check.clone().add(0.5, 1, 0.5);

                    if (spawn.getBlock().getType().isAir()
                            && spawn.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                        return spawn;
                    }
                }
            }
        }

        return null;
    }

    private void loadConfig() {

        ConfigurationSection g = main.getConfig().getConfigurationSection("global");
        if (g == null) {
            return;
        }

        world = Bukkit.getWorld(g.getString("world"));

        pos1 = new Location(world,
                g.getConfigurationSection("poss1").getDouble("x"),
                g.getConfigurationSection("poss1").getDouble("y"),
                g.getConfigurationSection("poss1").getDouble("z"));

        pos2 = new Location(world,
                g.getConfigurationSection("poss2").getDouble("x"),
                g.getConfigurationSection("poss2").getDouble("y"),
                g.getConfigurationSection("poss2").getDouble("z"));

        MAX_ALIVE = g.getInt("maxMobAmountInMoment");
        timeSpawn = g.getInt("mobSpawnTime");

        waves.clear();

        ConfigurationSection wavesSec =
                main.getConfig().getConfigurationSection("waves.list");

        if (wavesSec != null) {
            for (String waveKey : wavesSec.getKeys(false)) {

                ConfigurationSection wave =
                        wavesSec.getConfigurationSection(waveKey);
                if (wave == null) continue;

                List<WaveData> waveList = new ArrayList<>();

                for (Map<?, ?> mobMap : wave.getMapList("mobs")) {
                    String id = (String) mobMap.get("id");
                    int amount = (int) mobMap.get("amount");
                    waveList.add(new WaveData(id, amount));
                }

                waves.add(waveList);
            }
        }

        totalWaves = waves.size();
    }

    public void stop() {

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

    public boolean isActive() {
        return task != null && !task.isCancelled();
    }

    public int getLeftAmount() {
        return Math.max(0, totalToSpawn - killedMob);
    }

    public int getTotalAmount() {
        return totalToSpawn;
    }

    public int getCurrentWave() {
        return currentWave + 1;
    }

    public int getTotalWaves() {
        return totalWaves;
    }
}