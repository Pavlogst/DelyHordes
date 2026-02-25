package com.sweityuo.delyHordes.yml;

import com.sweityuo.delyHordes.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class CustomYml {

    private final Main plugin;
    private final String fileName;
    private File file;
    private YamlConfiguration config;

    public CustomYml(Main plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        create();
    }

    private void create() {
        file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}

