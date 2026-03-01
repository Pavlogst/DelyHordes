package com.sweityuo.delyHordes.utils;

import com.sweityuo.delyHordes.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TopDamageUtil {

    private final Main plugin;
    private final Map<UUID, Double> damageMap = new HashMap<>();

    private boolean enabled;
    private ConfigurationSection commandsSec;
    private List<String> broadcastLines;

    public TopDamageUtil(Main plugin) {
        this.plugin = plugin;
        load();
    }

    public void reload() {
        damageMap.clear();
        load();
    }

    private void load() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("topDamage");
        if (sec == null) return;

        enabled = sec.getBoolean("enable", true);
        commandsSec = sec.getConfigurationSection("commands");
        broadcastLines = sec.getStringList("broadcast");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addDamage(Player player, double damage) {
        if (!enabled || player == null) return;

        damageMap.merge(player.getUniqueId(), damage, Double::sum);
    }

    public void clear() {
        damageMap.clear();
    }

    public void finishAndReward() {
        if (!enabled) return;

        List<Map.Entry<UUID, Double>> top =
                damageMap.entrySet()
                        .stream()
                        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                        .limit(3)
                        .collect(Collectors.toList());

        Map<Integer, String> topNames = new HashMap<>();

        for (int i = 0; i < top.size(); i++) {
            Player p = Bukkit.getPlayer(top.get(i).getKey());
            String name = (p != null) ? p.getName() : "Хз кто-то";
            int place = i + 1;

            topNames.put(place, name);
            executeRewards(place, name);
        }

        sendBroadcast(topNames);
        damageMap.clear();
    }

    private void executeRewards(int place, String playerName) {
        if (commandsSec == null) return;

        List<String> cmds = commandsSec.getStringList(String.valueOf(place));
        if (cmds == null) return;

        for (String cmd : cmds) {
            String finalCmd = cmd.replace("%player%", playerName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        }
    }

    private void sendBroadcast(Map<Integer, String> topNames) {
        if (broadcastLines == null) return;

        String top1 = topNames.getOrDefault(1, "-");
        String top2 = topNames.getOrDefault(2, "-");
        String top3 = topNames.getOrDefault(3, "-");

        for (String line : broadcastLines) {
            String msg = line
                    .replace("%top1%", top1)
                    .replace("%top2%", top2)
                    .replace("%top3%", top3);

            Bukkit.broadcastMessage(ColorUtil.colorize(msg));
        }
    }
}