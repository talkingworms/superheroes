package me.xemor.superheroes;

import me.xemor.superheroes.Events.PlayerGainedPowerEvent;
import me.xemor.superheroes.Events.PlayerLostPowerEvent;
import me.xemor.superheroes.Superpowers.Power;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PowersHandler {

    HashMap<UUID, Power> uuidToPowers = new HashMap<>();
    FileConfiguration powersFile;
    Superheroes superheroes;
    EnumSet<Power> enabledPowers = EnumSet.noneOf(Power.class);


    public PowersHandler(Superheroes superheroes) {
        powersFile = superheroes.getConfig();
        this.superheroes = superheroes;
        loadPowers();
    }

    public void enablePower(Power power) {
        enabledPowers.add(power);
    }

    public boolean isEnabled(Power power) {
        return enabledPowers.contains(power);
    }

    public Power getPower(Player player) {
        Power power = uuidToPowers.get(player.getUniqueId());
        if (player.getGameMode() == GameMode.SPECTATOR && power != Power.Phase) {
            return null;
        }
        return power;
    }

    public void setPower(Player player, Power power) {
        Power currentPower = uuidToPowers.get(player.getUniqueId());
        if (currentPower != null) {
            PlayerLostPowerEvent playerLostPowerEvent = new PlayerLostPowerEvent(player, currentPower);
            Bukkit.getServer().getPluginManager().callEvent(playerLostPowerEvent);
        }
        uuidToPowers.put(player.getUniqueId(), power);
        showOffPower(player, power);
        savePower(player, power);
        if (currentPower != power) {
            PlayerGainedPowerEvent playerGainedPowerEvent = new PlayerGainedPowerEvent(player, power);
            Bukkit.getServer().getPluginManager().callEvent(playerGainedPowerEvent);
        }
    }

    public void temporarilyRemovePower(Player player, Player remover) {
        final Power oldPower = uuidToPowers.get(player.getUniqueId());
        uuidToPowers.remove(player.getUniqueId());
        if (remover != null) {
            remover.sendMessage(ChatColor.BOLD + player.getName() + " has had their power erased temporarily!");
        }
        player.sendMessage(ChatColor.BOLD + player.getName() + " has had their power erased temporarily!");
        PlayerLostPowerEvent playerLostPowerEvent = new PlayerLostPowerEvent(player, oldPower);
        Bukkit.getServer().getPluginManager().callEvent(playerLostPowerEvent);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (uuidToPowers.get(player.getUniqueId()) == null) {
                    PlayerGainedPowerEvent playerGainedPowerEvent = new PlayerGainedPowerEvent(player, oldPower);
                    Bukkit.getServer().getPluginManager().callEvent(playerGainedPowerEvent);
                    uuidToPowers.put(player.getUniqueId(), oldPower);
                    Bukkit.broadcastMessage(ChatColor.BOLD + player.getName() + " has had their powers reinstated!");
                }
            }
        }.runTaskLater(JavaPlugin.getPlugin(Superheroes.class), 300L);
    }

    public void setRandomPower(Player player) {
        Power power = uuidToPowers.get(player);
        Power newPower = power;
        while (power == newPower) {
            Power[] allPowers = Power.values();
            Random random = new Random();
            int rng = random.nextInt(allPowers.length);
            newPower = allPowers[rng];
            if (newPower == null) {
                continue;
            }
            this.setPower(player, newPower);
        }
    }

    public void showOffPower(Player player, Power power) {
        player.sendTitle(power.getNameColourCode(), power.getDescription(), 10, 100, 10);
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5F, 1F);
        Bukkit.broadcastMessage(ChatColor.BOLD + player.getName() + " has gained the power of " + power.getNameColourCode());
    }

    public void savePower(Player player, Power power) {
        powersFile.set(String.valueOf(player.getUniqueId()), power.toString());
        superheroes.saveConfig();
    }

    public void loadPowers() {
        for (Map.Entry<String, Object> entry :  powersFile.getValues(false).entrySet()) {
            uuidToPowers.put(UUID.fromString(entry.getKey()), Power.valueOf((String) entry.getValue()));
        }
    }
}
