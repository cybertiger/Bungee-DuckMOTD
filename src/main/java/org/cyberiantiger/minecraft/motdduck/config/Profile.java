/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.motdduck.config;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing.Protocol;
import org.cyberiantiger.minecraft.motdduck.Main;

/**
 *
 * @author antony
 */
public class Profile {
    private String icon;
    private String protocolName;
    private int protocolVersion;
    private List<String> dynamicMotd;
    private List<String> staticMotd;

    private transient boolean loadedFavicon;
    private transient Favicon favicon;
    private transient boolean loadedProtocol;
    private transient Protocol protocol;

    private static final ThreadLocal<Random> RNG = new ThreadLocal<Random>() {

        @Override
        protected Random initialValue() {
            return new Random();
        }
        
    };

    public Favicon getFavicon(Main plugin) {
        synchronized(this) {
            if (!loadedFavicon) {
                if (icon != null) {
                    File faviconFile = new File(plugin.getDataFolder(), icon);
                    if (faviconFile.isFile()) {
                        try {
                            favicon = favicon.create(ImageIO.read(faviconFile));
                        } catch (IOException ex) {
                            plugin.getLogger().log(Level.WARNING, "Error loading icon file: " + faviconFile, ex);
                        }
                    }
                }
                loadedFavicon = true;
            }
        }
        return favicon;
    }

    public Protocol getProtocol(Main plugin) {
        synchronized(this) {
            if (!loadedProtocol) {
                if (protocolName != null) {
                    protocol = new Protocol(protocolName, protocolVersion);
                }
                loadedProtocol = true;
            }
        }
        return protocol;
    }

    public String getDynamicMotd() {
        if (dynamicMotd != null)  {
            return dynamicMotd.get(RNG.get().nextInt(dynamicMotd.size()));
        } else {
            return getStaticMotd();
        }
    }

    public String getStaticMotd() {
        if (staticMotd != null) {
            return staticMotd.get(RNG.get().nextInt(staticMotd.size()));
        } else {
            return null;
        }
    }
}
