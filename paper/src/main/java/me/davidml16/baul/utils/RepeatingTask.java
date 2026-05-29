package me.davidml16.baul.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class RepeatingTask implements Runnable {

    private int taskId;

    public RepeatingTask(Plugin plugin, int arg1, int arg2) {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, arg1, arg2);
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

}
