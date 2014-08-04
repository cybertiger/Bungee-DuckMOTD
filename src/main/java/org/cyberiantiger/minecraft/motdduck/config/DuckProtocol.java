/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.motdduck.config;

import net.md_5.bungee.api.ServerPing.Protocol;

/**
 *
 * @author antony
 */
public class DuckProtocol {

    private String name;
    private int version;

    private transient Protocol protocol;

    public Protocol asProtocol() {
        synchronized (this) {
            if (protocol == null) {
                protocol= new Protocol(name, version);
            }
            return protocol;
        }
    }
    
}
