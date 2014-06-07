/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.motdduck.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import org.cyberiantiger.minecraft.motdduck.Main;

/**
 *
 * @author antony
 */
public class Config {

    private Map<String, Profile> profiles;
    private Map<String, Server> servers;

    private transient final Map<ListenerInfo, Server> serverCache = new HashMap<ListenerInfo, Server>();

    private boolean hostMatch(Main plugin, String host, ListenerInfo info) {
        int split = host.lastIndexOf(':');
        if (split == -1) {
            plugin.getLogger().log(Level.WARNING, "Badly formatted host:port pair: {0}", host);
        }
        InetSocketAddress addr = info.getHost();
        String hostPart = host.substring(0, split);
        String portPart = host.substring(split+1);
        if (!"*".equals(hostPart)) {
            try {
                InetAddress hostAddr = InetAddress.getByName(hostPart);
                if (!hostAddr.equals(addr.getAddress())) {
                    return false;
                }
            } catch (UnknownHostException ex) {
                plugin.getLogger().log(Level.WARNING, "Unable to resolve server host: " + hostPart, ex);
                return false;
            }
        } 
        if (!"*".equals(portPart)) {
            try {
                int port = Integer.parseInt(portPart);
                if (port != addr.getPort()) {
                    return false;
                }
            } catch (NumberFormatException ex) {
                plugin.getLogger().log(Level.WARNING, "Unable to parse port number: " + portPart, ex);
                return false;
            }
        }
        return true;
    }

    public Profile findProfile(Main plugin, PendingConnection connection, ListenerInfo info) {
        Server serverMatch;
        synchronized (this) {
            if (serverCache.containsKey(info)) {
                serverMatch = serverCache.get(info);
            } else {
                serverMatch = null;
                if (servers != null) {
                    for (Map.Entry<String, Server> e : servers.entrySet()) {
                        if (e.getValue() != null && hostMatch(plugin, e.getKey(), info)) {
                            serverMatch = e.getValue();
                            break;
                        }
                    }
                }
                serverCache.put(info, serverMatch);
            }
        }
        String profileName = null;
        if (serverMatch != null) {
            profileName = serverMatch.findProfileName(plugin, connection);
        }
        if (!profiles.containsKey(profileName)) {
            return null;
        } else {
            return profiles.get(profileName);
        }
    }
}
