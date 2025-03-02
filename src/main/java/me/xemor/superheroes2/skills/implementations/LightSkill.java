package me.xemor.superheroes2.skills.implementations;

import me.xemor.superheroes2.PowersHandler;
import me.xemor.superheroes2.Superhero;
import me.xemor.superheroes2.events.PlayerGainedSuperheroEvent;
import me.xemor.superheroes2.events.PlayerLostSuperheroEvent;
import me.xemor.superheroes2.skills.Skill;
import me.xemor.superheroes2.skills.skilldata.LightData;
import me.xemor.superheroes2.skills.skilldata.SkillData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class LightSkill extends SkillImplementation {
    public LightSkill(PowersHandler powersHandler) {
        super(powersHandler);
    }

    @EventHandler
    public void onGain(PlayerGainedSuperheroEvent e) {
        Superhero superhero = powersHandler.getSuperhero(e.getPlayer());
        if (superhero.hasSkill(Skill.LIGHT)) {
            runnable(e.getPlayer());
        }
    }

    @EventHandler
    public void onLost(PlayerLostSuperheroEvent e) {
        Superhero superhero = powersHandler.getSuperhero(e.getPlayer());
        Collection<SkillData> skillDatas = superhero.getSkillData(Skill.LIGHT);
        for (SkillData skillData : skillDatas) {
            LightData lightData = (LightData) skillData;
            e.getPlayer().removePotionEffect(lightData.getPotionEffect().getType());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Superhero superhero = powersHandler.getSuperhero(e.getPlayer());
        if (superhero.hasSkill(Skill.LIGHT)) {
            runnable(e.getPlayer());
        }
    }

    public void runnable(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Superhero superhero1 = powersHandler.getSuperhero(player);
                Collection<SkillData> data = superhero1.getSkillData(Skill.LIGHT);
                if (data.isEmpty()) {
                    this.cancel();
                    return;
                }
                for (SkillData skillData : data) {
                    LightData lightData = (LightData) skillData;
                    if (player.getWorld().getBlockAt(player.getLocation()).getLightLevel() > 10) {
                        player.addPotionEffect(lightData.getPotionEffect());
                    }
                    else {
                        player.removePotionEffect(lightData.getPotionEffect().getType());
                    }
                }
            }
        }.runTaskTimer(powersHandler.getPlugin(), 0L, 20L);
    }


}
