/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.motdduck.config;

/**
 *
 * @author antony
 */
public class User {
    private String name;
    private String lastIP;
    private long lastUpdated;

    public User() {}

    public User(String name, String lastIP) {
        this.name = name;
        this.lastIP = lastIP;
        this.lastUpdated = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public String getLastIP() {
        return lastIP;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
