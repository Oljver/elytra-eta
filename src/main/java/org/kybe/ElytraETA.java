package org.kybe;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

/**
 * Elytra Eta
 *
 * @author kybe236
 */
public class ElytraETA extends Plugin {

    @Override
    public void onLoad() {
        this.getLogger().info("Elytra ETA Plugin loaded!");

        final ETAHud ETAHud = new ETAHud("Elytra ETA");
        RusherHackAPI.getHudManager().registerFeature(ETAHud);
    }

    @Override
    public void onUnload() {
        this.getLogger().info("Elytra ETA Plugin unloaded!");
    }

}