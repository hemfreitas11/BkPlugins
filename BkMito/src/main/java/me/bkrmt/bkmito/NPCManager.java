package me.bkrmt.bkmito;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkcore.textanimator.CancellableTask;
import me.bkrmt.bkcore.textanimator.TextAnimator;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkmito.api.Mito;
import me.bkrmt.bkmito.api.UpdateReason;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;

class NPCManager implements me.bkrmt.bkmito.api.NPCManager {
    private NPC topNpc;
    private Hologram topHologram;
    private CancellableTask neabyChecker;
    private Listener interactListener;
    private HashMap<Integer, TextAnimator> animators;
    private HashMap<Integer, CancellableTask> updaters;
    private final BkMito plugin;

    NPCManager(BkMito plugin) {
        this.plugin = plugin;
        this.topNpc = null;
        this.topHologram = null;
        this.updaters = null;
        this.neabyChecker = null;
        this.interactListener = null;
        this.animators = null;
    }

    @Override
    public void setMitoNpc(final me.bkrmt.bkmito.api.Mito mito, UpdateReason reason) {
        wipe(reason);
        boolean swordAnim = plugin.getConfigManager().getConfig().getBoolean("mito-npc.hologram.sword-animation");
        List<String> hologramLines;
        try {
            hologramLines = plugin.getConfigManager().getConfig().getStringList("mito-npc.hologram.lines");
        } catch (Exception e) {
            e.printStackTrace();
            plugin.sendConsoleMessage("§cErro ao tentar encontar a mensagem do holograma.");
            return;
        }

        int placeholderUpdate = plugin.getConfigManager().getConfig().getInt("mito-npc.hologram.placeholder-update");
        boolean lookAtPlayers = !reason.equals(UpdateReason.UPDATE_ALL) ? false : plugin.getConfigManager().getConfig().getBoolean("mito-npc.npc.look-at-players.enabled");
        int lookDistance = !reason.equals(UpdateReason.UPDATE_ALL) || !lookAtPlayers ? 0 : plugin.getConfigManager().getConfig().getInt("mito-npc.npc.look-at-players.distance-to-look");
        boolean lookAround = !reason.equals(UpdateReason.UPDATE_ALL) ? false : plugin.getConfigManager().getConfig().getBoolean(("mito-npc.npc.random-look-around.enabled"));
        float leftRange = !reason.equals(UpdateReason.UPDATE_ALL) || !lookAround ? 0f : (float) plugin.getConfigManager().getConfig().getDouble("mito-npc.npc.random-look-around.radom-look-range.left-range");
        float rightRange = !reason.equals(UpdateReason.UPDATE_ALL) || !lookAround ? 0f : (float) plugin.getConfigManager().getConfig().getDouble("mito-npc.npc.random-look-around.radom-look-range.right-range");
        float upRange = !reason.equals(UpdateReason.UPDATE_ALL) || !lookAround ? 0f : (float) plugin.getConfigManager().getConfig().getDouble("mito-npc.npc.random-look-around.radom-look-range.up-range");
        float downRange = !reason.equals(UpdateReason.UPDATE_ALL) || !lookAround ? 0f : (float) plugin.getConfigManager().getConfig().getDouble("mito-npc.npc.random-look-around.radom-look-range.down-range");

        Location location = plugin.getConfigManager().getConfig().getLocation("mito-npc.npc.location");

        Chunk chunk = location.getChunk();
        boolean wasLoaded = chunk.isLoaded();
        if (!wasLoaded) chunk.load();

        if (reason.equals(UpdateReason.UPDATE_ALL)) {
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            topNpc = registry.createNPC(EntityType.PLAYER, "");
            topNpc.spawn(location);
            Configuration config = plugin.getConfigManager().getConfig();
            config.set("mito-npc.npc.id", topNpc.getId());
            config.saveToFile();
            topNpc.data().setPersistent("nameplate-visible", false);
            if (lookAtPlayers) topNpc.getOrAddTrait(LookClose.class).lookClose(true);
            if (lookAtPlayers) topNpc.getOrAddTrait(LookClose.class).setRange(lookDistance);
            topNpc.getOrAddTrait(LookClose.class).setRandomLook(true);
            topNpc.getOrAddTrait(LookClose.class).setRandomLookPitchRange(location.getPitch() - downRange, location.getPitch() + upRange);
            topNpc.getOrAddTrait(LookClose.class).setRandomLookYawRange(location.getYaw() - leftRange, location.getYaw() + rightRange);
            topNpc.getOrAddTrait(LookClose.class).setRealisticLooking(true);
            topNpc.getOrAddTrait(SkinTrait.class).setSkinName(mito.getPlayerName(), true);

            if (interactListener != null) HandlerList.unregisterAll(interactListener);
            interactListener = new Listener() {
                @EventHandler
                public void onInteractNpc(NPCLeftClickEvent event) {
                    if (event.getNPC().getId() == topNpc.getId()) {
                        event.getClicker().performCommand(plugin.getLangFile().get(Bukkit.getOfflinePlayer(mito.getUUID()), "commands.mito.command") + " " + plugin.getLangFile().get(Bukkit.getOfflinePlayer(mito.getUUID()), "commands.mito.subcommands.stats.command") + " " + mito.getPlayerName());
                    }
                }
            };
            Bukkit.getServer().getPluginManager().registerEvents(interactListener, plugin);
        }

        location.add(0.0D, 2.12 + (hologramLines.size() > 0 ? hologramLines.size() * 0.23 : 0) + (swordAnim ? 0.60 : 0), 0.0D);

        topHologram = HologramsAPI.createHologram(plugin, location);
        HashMap<Integer, TextLine> lines = new HashMap<>();
        animators = new HashMap<>();
        for (int c = 0; c < hologramLines.size(); c++) {
            int finalC = c;
            if (hologramLines.get(finalC).equalsIgnoreCase(" ")) continue;

            String hologramLine = Utils.translateColor(hologramLines.get(finalC)
                    .replace("{mito}", mito.getPlayerName())
                    .replace("{mito-kills}", String.valueOf(mito.getStats().getKills()))
                    .replace("{mito-times}", String.valueOf(mito.getStats().getMitoTimes()))
                    .replace("{mito-duration}", mito.getStats().getFormattedDuration()));

            lines.put(finalC, topHologram.appendTextLine(hologramLine));

            TextAnimator animator = plugin.getAnimatorManager().getTextAnimator("bkduel-top-npc-hologram-line-" + finalC, hologramLine);
            if (animator != null) {
                setLineAnimator(mito, lines, finalC, hologramLine, animator);
            }

            if (hologramLine.contains("%") && plugin.hasPlaceholderHook()) {
                if (updaters == null) updaters = new HashMap<>();
                try {
                    if (updaters.get(finalC) != null) updaters.get(finalC).cancel();
                } catch (Exception ignored) {
                }
                if (placeholderUpdate > 2) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            updateLine(mito, hologramLine, lines, finalC);
                        }
                    }.runTaskLater(BkMito.getInstance(), 20 * 2);
                }
                CancellableTask newTask = new CancellableTask(() -> updateLine(mito, hologramLine, lines, finalC), runnable -> runnable.runTaskTimerAsynchronously(plugin, 0, 20L * placeholderUpdate));
                updaters.put(finalC, newTask);
            }
        }

        if (swordAnim) topHologram.insertItemLine(hologramLines.size(), XMaterial.DIAMOND_SWORD.parseItem());
        if (animators.values().size() > 0) {
            neabyChecker = new CancellableTask(
                    () -> {
                        if (!plugin.hasPlaceholderHook()) neabyChecker.cancel();
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            boolean hasLineOfSight = false;
                            for (Player player : plugin.getHandler().getMethodManager().getOnlinePlayers()) {
                                if (plugin.getNmsVer().number > 8) {
                                    if (getTopNpc() != null && getTopNpc().getEntity() != null && getTopNpc().getEntity().getLocation() != null && getTopNpc().getEntity().getLocation().getWorld() != null && player.getLocation() != null && player.getLocation().getWorld() != null && player.getLocation().getWorld().equals(getTopNpc().getEntity().getLocation().getWorld()) && player.hasLineOfSight(getTopNpc().getEntity()) && player.getLocation().distance(getTopNpc().getEntity().getLocation()) < 35) {
                                        hasLineOfSight = true;
                                        break;
                                    }
                                } else {
                                    if (getTopNpc() != null && getTopNpc().getEntity() != null && getTopNpc().getEntity().getLocation() != null && getTopNpc().getEntity().getLocation().getWorld() != null && player.getLocation() != null && player.getLocation().getWorld() != null && player.getLocation().getWorld().equals(getTopNpc().getEntity().getLocation().getWorld()) && player.getLocation().distance(getTopNpc().getEntity().getLocation()) < 35) {
                                        hasLineOfSight = true;
                                        break;
                                    }
                                }
                            }

                            for (TextAnimator animator : animators.values()) {
                                if (animator != null) {
                                    if (hasLineOfSight) {
                                        if (animator.getAnimationTask() == null || animator.getAnimationTask().isCancelled()) {
                                            animator.animate();
                                        }
                                    } else {
                                        if (animator.getAnimationTask() != null) {
                                            animator.pause();
                                        }
                                    }
                                }
                            }
                        });
                    },
                    runnable -> runnable.runTaskTimerAsynchronously(plugin, 0, 20)
            );
            neabyChecker.start();
        }
        if (!wasLoaded) chunk.unload();

    }

    private void updateLine(Mito newTop, String hologramLine, HashMap<Integer, TextLine> lines, int finalC) {
        String updatedLine = ((hologramLine.contains("%") && plugin.hasPlaceholderHook()) ? PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(newTop.getUUID()), hologramLine) : hologramLine);
        String test1 = AnimatorManager.cleanText(ChatColor.stripColor(updatedLine)).trim();
        String test2 = AnimatorManager.cleanText(ChatColor.stripColor(lines.get(finalC).getText())).trim();
        if (!test1.equalsIgnoreCase(test2)) {
            try {
                TextAnimator tempAnimator = animators.get(finalC);
                if (tempAnimator != null) {
                    plugin.getAnimatorManager().destroy(tempAnimator);
                    TextAnimator newAnimator = plugin.getAnimatorManager().getTextAnimator("top-npc-hologram-line-" + finalC, updatedLine);
                    setLineAnimator(newTop, lines, finalC, updatedLine, newAnimator);
                } else {
                    lines.get(finalC).setText(updatedLine);
                }
            } catch (Exception ignored) {
                lines.get(finalC).setText(updatedLine);
            }
        }
    }

    private void setLineAnimator(me.bkrmt.bkmito.api.Mito newTop, HashMap<Integer, TextLine> lines, int finalC, String hologramLine, TextAnimator animator) {
        boolean isOptionText = AnimatorManager.isOptionText(hologramLine);
        animator.setReceiver(animationFrame -> {
            try {
                if (isOptionText) {
                    if ((hologramLine.contains("%") && plugin.hasPlaceholderHook())) {
                        lines.get(finalC).setText(PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(newTop.getUUID()), hologramLine).replaceAll("\\{([^}]*)}", animationFrame));
                    } else {
                        lines.get(finalC).setText(hologramLine.replaceAll("\\{([^}]*)}", animationFrame));
                    }
                } else lines.get(finalC).setText(animationFrame);
            } catch (Exception e) {
                e.printStackTrace();
                lines.get(finalC).setText("§cThis line had an error, check console!");
            }
        });
        animators.put(finalC, animator);
        animators.get(finalC).animate();
    }

    private NPC getTopNpc() {
        return topNpc;
    }

    private Hologram getTopHologram() {
        return topHologram;
    }

    private CancellableTask getNeabyChecker() {
        return neabyChecker;
    }

    private Listener getInteractListener() {
        return interactListener;
    }

    private HashMap<Integer, TextAnimator> getAnimators() {
        return animators;
    }

    @Override
    public void wipe(UpdateReason reason) {
        if (reason.equals(UpdateReason.UPDATE_ALL)) {
            if (getTopNpc() != null) {
                getTopNpc().getOrAddTrait(LookClose.class).lookClose(false);
                getTopNpc().destroy();
            }
            NPC configNpc = CitizensAPI.getNPCRegistry().getById(BkMito.getInstance().getConfigManager().getConfig().getInt("mito-npc.npc.id"));
            if (configNpc != null) {
                configNpc.getOrAddTrait(LookClose.class).lookClose(false);
                configNpc.destroy();
            }
        }
        if (neabyChecker != null) neabyChecker.cancel();
        if (animators != null) {
            for (TextAnimator animator : animators.values()) {
                if (animator != null) plugin.getAnimatorManager().destroy(animator);
            }
        }
        if (updaters != null) {
            for (CancellableTask updater : updaters.values()) {
                updater.cancel();
            }
        }
        if (topHologram != null) topHologram.delete();
    }

}
