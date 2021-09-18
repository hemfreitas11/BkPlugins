package me.bkrmt.newbasspawner;

import de.slikey.effectlib.effect.WarpEffect;
import me.bkrmt.bkcore.config.Configuration;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpawnerManager {
    private final HashMap<String, Spawner> spawners;
    private final boolean effects;
    private final Particle particle;
    private final Sound sound;
    private final NewbasSpawner plugin;

    public SpawnerManager(boolean effects, Particle particle, Sound sound) {
        this.plugin = NewbasSpawner.getInstance();
        this.effects = effects;
        this.particle = particle;
        this.sound = sound;
        this.spawners = new HashMap<>();
    }

    public boolean isEffects() {
        return effects;
    }

    public Sound getSound() {
        return sound;
    }

    public Particle getParticle() {
        return particle;
    }

    public void setSpawner(Block block, EntityType entityType, Player player) {
        BlockState blockState = block.getState();
        CreatureSpawner spawner = (CreatureSpawner) blockState;
        spawner.setSpawnedType(entityType);
        blockState.update();

        if (plugin.getSpawnerManager().isEffects()) {
            WarpEffect warpEffect = new WarpEffect(plugin.getEffectManager());
            warpEffect.visibleRange = 10;
            warpEffect.setLocation(block.getLocation().add(0.5, 0, 0.5));
            warpEffect.particle = plugin.getSpawnerManager().getParticle();
            BukkitTask soundRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    player.playSound(player.getLocation(), plugin.getSpawnerManager().getSound(), 0.2f, 0);
                }
            }.runTaskTimer(plugin, 0, 20);
            warpEffect.callback = soundRunnable::cancel;
            warpEffect.start();
        }
    }

    public void loadSpawners() {
        File spawnersFile = plugin.getFile("", "spawners.yml");
        Configuration spawnersConfig;
        if (!spawnersFile.exists()) {
            spawnersConfig = new Configuration(plugin, spawnersFile);
            spawnersConfig.saveToFile();
            plugin.getConfigManager().addConfig(spawnersConfig);
        } else {
            spawnersConfig = plugin.getConfigManager().getConfig("spawners.yml");
        }

        ConfigurationSection spawnerSection = spawnersConfig.getConfigurationSection("spawners");

        for (String mob : spawnerSection.getKeys(false)) {
            boolean enabled = spawnerSection.getBoolean(mob + ".ativar");

            if (enabled) {
                List<Group> groups = new ArrayList<>();
                ConfigurationSection section = spawnerSection.getConfigurationSection(mob + ".grupos");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        if (!key.equalsIgnoreCase("ativar") && !key.equalsIgnoreCase("nome") && !key.equalsIgnoreCase("lore")) {
                            groups.add(new Group(section.getDouble(key), key));
                        }
                    }

                    plugin.getSpawnerManager().getSpawners().put(mob, new Spawner(spawnerSection.getString(mob + ".nome"), mob, groups, spawnerSection.getStringList(mob + ".lore")));
                }
            }
        }
        plugin.sendConsoleMessage("[NewbasSpawners] §6Carregamento dos spawners terminado.");
    }

    public HashMap<String, Spawner> getSpawners() {
        return spawners;
    }
}
