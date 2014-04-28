/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.motdduck;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 *
 * @author antony
 */
class DuckMOTDCommand extends Command {
    private final Main plugin;

    public DuckMOTDCommand(Main plugin) {
        super("duckmotd", "duckmotd");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] strings) {
        try {
            plugin.saveData();
            plugin.loadConfig();
            cs.sendMessage("DuckMOTD config reloaded.");
        } catch (IllegalStateException e) {
            cs.sendMessage("Your configuration is broken, check your proxy log.");
        }
    }
}
