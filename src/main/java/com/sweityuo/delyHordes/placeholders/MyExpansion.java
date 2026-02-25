package com.sweityuo.delyHordes.placeholders;

import com.sweityuo.delyHordes.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MyExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public MyExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "delyHordes";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Sweityuo";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";

        if (identifier.equalsIgnoreCase("isActive")) {
            return plugin.getWaves() != null && plugin.getWaves().isActive()
                    ? "Активный"
                    : "Неактивный";
        }
        return "";
    }
}
