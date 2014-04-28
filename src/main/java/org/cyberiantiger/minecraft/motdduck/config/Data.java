/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.motdduck.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author antony
 */
public class Data {

    private Map<String, User> data = new LinkedHashMap();

    private transient Map<String, String> hostToUser;

    public Data() {}

    public Data(Map<String, User> data) {
        this.data = data;
    }

    private Map<String,String> getHostToUser() {
        if (hostToUser == null) {
            hostToUser = new HashMap<String, String>();
            List<User> entries = new ArrayList<User>(data.size());
            entries.addAll(data.values());
            Collections.sort(entries, new Comparator<User>() {
                @Override
                public int compare(User o1, User o2) {
                    long o1Val = o1.getLastUpdated();
                    long o2Val = o2.getLastUpdated();
                    return o1Val < o2Val ? -1 : (o1Val > o2Val ? 1 : 0);
                }
            });
            for (User u : entries) {
                hostToUser.put(u.getLastIP(), u.getName());
            }
        }
        return hostToUser;
    }

    public synchronized void addPlayer(UUID uniqueId, String name, String hostName) {
        data.put(uniqueId.toString(), new User(name, hostName));
        getHostToUser().put(hostName, name);
    }

    public synchronized String getPlayer(String host) {
        return getHostToUser().get(host);
    }
}
