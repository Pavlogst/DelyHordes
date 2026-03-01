package com.sweityuo.delyHordes.utils;


import com.sweityuo.delyHordes.Main;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarUtil {
    private final Map<UUID, BossBar> bars = new HashMap<>();
    private final boolean enabled;
    private Main plugin;
    private final BarStyle style;
    private final BarColor color;
    private final String text;
    private BukkitTask task;
    private WavesUtil wavesUtil;
    public BossBarUtil(WavesUtil wavesUtil, Main plugin) {
        this.plugin = plugin;
        this.wavesUtil = wavesUtil;
        ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("BossBar");
        enabled = cfg.getBoolean("enable", true);
        style = BarStyle.valueOf(cfg.getString("type", "SOLID"));
        color = BarColor.valueOf(cfg.getString("color", "GREEN"));
        text = ColorUtil.colorize(cfg.getString("text", ""));

        if (enabled) startTask();
    }
    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        bars.values().forEach(BossBar::removeAll);
        bars.clear();
    }
    private void startTask() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayer(player);
            }
        }, 20L, 20L);
    }

    private void updatePlayer(Player player) {
        if (!wavesUtil.isPlayerInZone(player) || !wavesUtil.isActive()) {
            removeBar(player);
            return;
        }

        BossBar bar = bars.computeIfAbsent(player.getUniqueId(), p -> {
            BossBar b = Bukkit.createBossBar("", color, style);
            b.addPlayer(player);
            return b;
        });

        int left = wavesUtil.getLeftAmount();
        int total = Math.max(wavesUtil.getTotalAmount(), 1);

        double progress = Math.max(0.0, Math.min(1.0, (double) left / total));

        String title = text
                .replace("%mobsLeft%", String.valueOf(wavesUtil.getLeftAmount()))
                .replace("%wave%", String.valueOf(wavesUtil.getCurrentWave()))
                .replace("%waves%", String.valueOf(wavesUtil.getTotalWaves()));

        bar.setTitle(title);
        bar.setProgress(progress);
        bar.setVisible(true);
    }

    private void removeBar(Player player) {
        BossBar bar = bars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }
}
