package com.sweityuo.delyHordes;

import com.sweityuo.delyHordes.Event.*;
import com.sweityuo.delyHordes.listeners.MobDeathListener;
import com.sweityuo.delyHordes.mobs.MobManager;
import com.sweityuo.delyHordes.placeholders.MyExpansion;
import com.sweityuo.delyHordes.utils.BossBarUtil;
import com.sweityuo.delyHordes.utils.TopDamageUtil;
import com.sweityuo.delyHordes.utils.WavesUtil;
import com.sweityuo.delyHordes.yml.CustomYml;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {
    private LootContoller lootController;
    private CustomYml lootFile;
    private MobManager mobManager;
    private MessageManager messageManager;
    private BossBarUtil bossBarUtil;
    private WavesUtil waves;
    private TopDamageUtil topDamageUtil;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MyExpansion(this).register();
        }
        topDamageUtil = new TopDamageUtil(this);
        mobManager = new MobManager(getConfig().getConfigurationSection("mobs"));
        waves = new WavesUtil(this, mobManager);
        lootController = new LootContoller();
        bossBarUtil = new BossBarUtil(waves, this);
        messageManager = new MessageManager(getConfig());
        lootFile = new CustomYml(this, "loot.yml");
        getServer().getPluginManager().registerEvents(
                new MobDeathListener(waves, this),
                this
        );
        getLogger().info("DelyHordes включён!");
    }
    @Override
    public void onDisable() {
        getLogger().info("DelyHordes выключён!");
        bossBarUtil.shutdown();


    }


    public TopDamageUtil getTopDamageUtil() {
        return topDamageUtil;
    }
    public MessageManager getMessageManager() {
        return messageManager;
    }

    public LootContoller getLootController() {
        return lootController;
    }
    public CustomYml getLootFile() {
        return lootFile;
    }

    public WavesUtil getWaves() {
        return waves;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("delyHordes")) {
            if (!sender.hasPermission("delyHordes.admin")) {
                messageManager.sendNoPermission(sender);
                return true;
            }
            if (args.length == 0) {
                messageManager.sendUse(sender);
                return true;
            }

            if (args[0].equalsIgnoreCase("start")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    messageManager.sendOnStarted(player);
                }
                waves.start();

                return true;
            }
            if (args[0].equalsIgnoreCase("stop")) {
                messageManager.sendOnStoped(sender);
                waves.stop();

                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                waves.stop();
                reloadConfig();
                topDamageUtil.reload();
                bossBarUtil.shutdown();
                mobManager = new MobManager(getConfig().getConfigurationSection("mobs"));
                lootController = new LootContoller();
                waves.reload();
                bossBarUtil = new BossBarUtil(waves, this);
                messageManager = new MessageManager(getConfig());
                lootFile.reload();
                messageManager.sendOnReload(sender);
                return true;
            }


            if(args[0].equalsIgnoreCase("addLoot")) {
                if (!(sender instanceof Player player)) {
                    messageManager.sendNoPermission(sender);
                    return true;
                }

                if (args.length < 2) {
                    messageManager.sendUse(sender);
                    return true;
                }
                float amount = 1;
                try {
                    amount = Float.parseFloat(args[1]);
                }
                catch (NumberFormatException e) {
                    sender.sendMessage("Введите корректное число"); //Это не хардкод
                }
                lootController.addItem(player, amount, this);
                return true;
            }
            if(args[0].equalsIgnoreCase("removeLoot")) {
                if (!(sender instanceof Player player)) {
                    messageManager.sendNoPermission(sender);
                    return true;
                }

                lootController.removeItem(player, this);
                return true;
            }

            messageManager.sendUse(sender);
            return true;
        }


        return false;
    }

}
