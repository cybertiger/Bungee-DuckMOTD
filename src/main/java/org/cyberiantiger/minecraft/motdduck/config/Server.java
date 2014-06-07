/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.motdduck.config;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.md_5.bungee.api.connection.PendingConnection;
import org.cyberiantiger.minecraft.motdduck.Main;

/**
 *
 * @author antony
 */
public class Server {

    private String defaultProfile;
    private Map<String, String> namedHosts;

    private transient Map<Pattern, String> namedHostLookup;

    public String findProfileName(Main plugin, PendingConnection connection) {
        synchronized (this) {
            if (namedHostLookup == null) {
                if (namedHosts == null) {
                    namedHostLookup = Collections.emptyMap();
                } else {
                    namedHostLookup = new LinkedHashMap<Pattern, String>(namedHosts.size());
                    for (Map.Entry<String, String> e : namedHosts.entrySet()) {
                        namedHostLookup.put(asRegex(e.getKey()), e.getValue());
                    }
                }
            }
        }
        InetSocketAddress namedVirtualHost = connection.getVirtualHost();
        if (namedVirtualHost != null) {
            String namedHost = namedVirtualHost.getHostName().toLowerCase() + ":" + namedVirtualHost.getPort();
            for (Map.Entry<Pattern, String> e : namedHostLookup.entrySet()) {
                if (e.getKey().matcher(namedHost).matches()) {
                    return e.getValue();
                }
            }
        }
        return defaultProfile;
    }

    private Pattern asRegex(String glob) {
        StringBuilder pattern = new StringBuilder();
        glob = glob.toLowerCase();
        for (int i = 0; i < glob.length(); i++) {
            char ch = glob.charAt(i);
            if (ch == '*') {
                pattern.append(".*");
            } else {
                pattern.append(Pattern.quote(String.valueOf(ch)));
            }
        }
        return Pattern.compile(pattern.toString());
    }
}
