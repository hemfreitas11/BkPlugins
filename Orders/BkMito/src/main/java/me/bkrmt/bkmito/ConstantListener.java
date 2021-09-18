package me.bkrmt.bkmito;

import me.bkrmt.bkmito.api.Mito;
import me.bkrmt.bkmito.api.MitoManager;
import me.bkrmt.bkmito.api.UpdateReason;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ConstantListener implements Listener {
    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getLastDamageCause().getEntity() instanceof Player &&
                player.getKiller() != null) {
            Player killer = player.getKiller();
            MitoManager mitoManager = BkMito.getInstance().getMitoManager();
            if (mitoManager.getCurrentMito().getUUID().equals(player.getUniqueId())) {
                Mito currentMito = mitoManager.getCurrentMito();
                Mito mito = mitoManager.setMito(killer);
                if (mito != null) BkMito.getInstance().getMitoManager().sendNewMitoMessage(mito, currentMito);
            } else if (mitoManager.getCurrentMito().getUUID().equals(killer.getUniqueId())) {
                mitoManager.getCurrentMito().getStats().setKills(mitoManager.getCurrentMito().getStats().getKills() + 1);
                BkMito.getInstance().getNpcManager().setMitoNpc(mitoManager.getCurrentMito(), UpdateReason.UPDATE_STATS);
            }
        }
    }
}
