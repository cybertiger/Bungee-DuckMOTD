/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.motdduck;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.PlayerInfo;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.ServerPing.Protocol;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.cyberiantiger.minecraft.motdduck.config.Config;
import org.cyberiantiger.minecraft.motdduck.config.Data;
import org.cyberiantiger.minecraft.motdduck.config.Profile;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 *
 * @author antony
 */
public class Main extends Plugin implements Listener {
    private static final String CONFIG = "config.yml";
    private static final String DATA = "data.yml";

    private Config config;
    private Data userData;

    private File getConfigFile() {
        return new File(getDataFolder(), CONFIG);
    }

    private File getDataFile() {
        return new File(getDataFolder(), DATA);
    }

    private void saveDefaultConfig() {
        File data = getDataFolder();
        if (!data.exists()) {
            data.mkdir();
        }
        File config = getConfigFile();
        if (!config.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(config);
                try {
                    ByteStreams.copy(getClass().getClassLoader().getResourceAsStream(CONFIG), new FileOutputStream(config));
                } finally {
                    out.close();
                }
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Could not create config", ex);
                throw new IllegalStateException();
            }
        }
    }

    public void loadConfig() {
        try {
            Yaml configLoader = new Yaml(new CustomClassLoaderConstructor(Config.class, getClass().getClassLoader()));
            configLoader.setBeanAccess(BeanAccess.FIELD);
            this.config = configLoader.loadAs(new FileReader(getConfigFile()), Config.class);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Error loading configuration", ex);
        }
    }

    private void loadData() {
        File dataFile = getDataFile();
        if (dataFile.isFile() && dataFile.canRead()) {
            Yaml yaml = new Yaml(new CustomClassLoaderConstructor(Data.class, getClass().getClassLoader()));
            yaml.setBeanAccess(BeanAccess.FIELD);
            try {
                this.userData = yaml.loadAs(new FileReader(dataFile), Data.class);
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Error loading data", ex);
                this.userData = new Data();
            }
        } else {
            this.userData = new Data();
        }
    }

    public void saveData() {
        try {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            FileWriter out = new FileWriter(getDataFile());
            try {
                synchronized(userData) {
                    out.write(yaml.dumpAsMap(userData));
                }
            } finally  {
                out.close();
            }
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Failed to write userdata file: " + getDataFile(), ex);
        }
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
        loadConfig();
        loadData();
    }

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerCommand(this, new DuckMOTDCommand(this));
        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        saveData();
    }

    @EventHandler
    public void onPostLoginEvent(PostLoginEvent e) {
        e.getPlayer();
        userData.addPlayer(e.getPlayer().getUniqueId(), e.getPlayer().getName(), e.getPlayer().getAddress().getAddress().getHostAddress());
    }

    @EventHandler
    public void onProxyPing(ProxyPingEvent e) {
        PendingConnection c = e.getConnection();
        ListenerInfo l = c.getListener();
        if (config != null) {
            Profile profile = config.findProfile(this, c, l);
            if (profile != null) {
                ServerPing response = e.getResponse();
                Favicon icon = profile.getFavicon(this);
                Protocol protocol = profile.getProtocol(this);
                String user = userData.getPlayer(c.getAddress().getAddress().getHostAddress());
                String motd;
                if (user != null) {
                    motd = profile.getDynamicMotd();
                    motd = String.format(motd, user);
                } else {
                    motd = profile.getStaticMotd();
                    motd = String.format(motd, (Object)null);
                }
                Players players = profile.getPlayers(this);
                if (icon != null) {
                    response.setFavicon(icon);
                }
                if (protocol != null) {
                    response.setVersion(protocol);
                }
                if (motd != null) {
                    response.setDescription(motd);
                }
                if (players != null) {
                    response.setPlayers(players);
                }
            }
        }
    }
}