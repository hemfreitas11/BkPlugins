package me.bkrmt.newbasspawner;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import de.slikey.effectlib.EffectManager;
import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.bkgui.BkGUI;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkcore.xlibs.XMaterial;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class NewbasSpawner extends BkPlugin {
    private static NewbasSpawner plugin;
    private AnimatorManager animatorManager;
    private EffectManager effectManager;
    private SpawnerManager spawnerManager;
    private Economy economy;

    @Override
    public void onEnable() {
        plugin = this;
        animatorManager = new AnimatorManager(this);
        effectManager = new EffectManager(this);
        BkGUI.INSTANCE.register(this);
        Plugin redProtect = Bukkit.getPluginManager().getPlugin("RedProtect");
        if (redProtect != null && redProtect.isEnabled()) {
            if (setupEconomy()) {
                start(true);
                setRunning(true);
                buildHandler();
                boolean isEffects = getConfigManager().getConfig().getBoolean("efeitos.ativar");
                spawnerManager = new SpawnerManager(isEffects,
                        isEffects ? Particle.valueOf(getConfigManager().getConfig().getString("efeitos.efeito").toUpperCase()) : null,
                        isEffects ? Sound.valueOf(getConfigManager().getConfig().getString("efeitos.som").toUpperCase()) : null);
                sendConsoleMessage("[NewbasSpawners] §6Iniciando carregamento dos spawners, isso pode levar alguns segundos...");
                getSpawnerManager().loadSpawners();

                Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    public void onInteract(PlayerInteractEvent event) {
                        Player player = event.getPlayer();
                        Block block = event.getClickedBlock();
                        NewbasSpawner plugin = NewbasSpawner.getInstance();

                        if (block != null && !block.getType().equals(XMaterial.AIR.parseMaterial())) {
                            RedProtectAPI redApi = ((RedProtect) Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("RedProtect"))).getAPI();
                            Region redRegion = redApi.getRegion(block.getLocation());
                            if (redRegion == null || (redRegion.isMember(player) || redRegion.isLeader(player)) || player.hasPermission("redprotect.command.admin.*")) {
                                if (event.getHand().equals(EquipmentSlot.HAND)) {
                                    if (player.isSneaking()) {
                                        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                                            if (block.getType().equals(XMaterial.SPAWNER.parseMaterial())) {
                                                MenuTroca menu = new MenuTroca(plugin, player, block);
                                                menu.open();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, this);
            } else {
                sendConsoleMessage("[NewbasSpawner] §cO vault ou o seu plugin de economia nao foram encontrados. Desativando o plugin.");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        } else {
            sendConsoleMessage("[NewbasSpawners] §cRedProtect nao foi encontrado, desativando o plugin...");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        effectManager.dispose();
        HandlerList.unregisterAll(this);
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    @Override
    public AnimatorManager getAnimatorManager() {
        return animatorManager;
    }

    public static NewbasSpawner getInstance() {
        return plugin;
    }

    public String translatePrices(String text, List<Group> groups) {
        String newLine = text;
        for (Group group : groups) {
            String groupName = group.getName();
            if (newLine.contains("{" + groupName + "}"))
                newLine = newLine
                    .replace("{" + groupName + "}", String.valueOf(group.getPrice()));
        }
        return newLine;
    }

    public double getCost(Player player, String mob) {
        List<Double> validPrices = new ArrayList<>();
        double biggest = 1;
        for (Group group : getSpawnerManager().getSpawners().get(mob).getGroups()) {
            String perm = "nbspawner." + group.getName();
            double price = group.getPrice();
            if (price > biggest) biggest = price;
            if (player.hasPermission(perm)) {
                validPrices.add(price);
            }
        }

        if (validPrices.size() > 0) {
            return validPrices.get(validPrices.indexOf(Collections.min(validPrices)));
        } else {
            return biggest;
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
}
