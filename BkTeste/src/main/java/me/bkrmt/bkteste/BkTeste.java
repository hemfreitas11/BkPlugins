package me.bkrmt.bkteste;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.bkgui.BkGUI;
import me.bkrmt.bkcore.command.CommandModule;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import org.bukkit.event.Listener;

public class BkTeste extends BkPlugin implements Listener {
    private static BkTeste instance;
    private AnimatorManager animatorManager;

    @Override
    public void onEnable() {
        instance = this;
        BkGUI.INSTANCE.register(this);
        animatorManager = new AnimatorManager(this);
        start(true);
        setRunning(true);
        getCommandMapper()
            .addCommand(new CommandModule(new CmdTeste(this, "teste", ""), null))
            .registerAll();

    }

    @Override
    public AnimatorManager getAnimatorManager() {
        return animatorManager;
    }

    @Override
    public void onDisable() {
        getConfigManager().saveConfigs();
    }

    public static BkTeste getInstance() {
        return instance;
    }

}
