package com.sweityuo.delyHordes.listeners;

import com.sweityuo.delyHordes.Main;
import com.sweityuo.delyHordes.utils.WavesUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public class MobDeathListener implements Listener {

    private final WavesUtil waves;
    private final Main main;

    public MobDeathListener(WavesUtil waves, Main main) {
        this.waves = waves;
        this.main = main;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!e.getEntity().getScoreboardTags().contains("DELY_HORDE")) return;
        e.getDrops().clear();
        main.getLootController().spawnItem(e.getEntity().getLocation(), main);

        waves.onMobDeath((LivingEntity) e.getEntity());

    }
}