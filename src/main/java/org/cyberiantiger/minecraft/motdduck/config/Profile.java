/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.motdduck.config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing.PlayerInfo;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.ServerPing.Protocol;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import org.cyberiantiger.minecraft.motdduck.Main;

/**
 *
 * @author antony
 */
public class Profile {
    private static final PlayerInfo[] EMPTY_PLAYER_LIST = new PlayerInfo[0];

    private enum PlayerListType {
        NONE() {
            @Override
            public PlayerInfo[] getPlayerInfos(Profile profile, List<ProxiedPlayer> players) {
                return EMPTY_PLAYER_LIST;
            }
        },
        PLAYERS() {
            private final int MAX_PLAYERS = 10;
            @Override
            public PlayerInfo[] getPlayerInfos(Profile profile, List<ProxiedPlayer> players) {
                PlayerInfo[] result = new PlayerInfo[players.size() < MAX_PLAYERS ? players.size() : MAX_PLAYERS];
                boolean full = false;
                for (int i = 0; i < result.length; i++) {
                    if (i == MAX_PLAYERS-1 && !players.isEmpty()) {
                        result[i] = new PlayerInfo(String.format(".... %d more ....", players.size()), (UUID)null);
                        full = true;
                    } else {
                        ProxiedPlayer player = players.remove(RNG.get().nextInt(players.size()));
                        result[i] = new PlayerInfo(player.getName(), player.getUniqueId());
                    }
                }
                Arrays.sort(result, 0, full ? result.length - 1 : result.length, new Comparator<PlayerInfo>() {
                    @Override
                    public int compare(PlayerInfo o1, PlayerInfo o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                return result;
            }
        },
        FIXED() {
            @Override
            public PlayerInfo[] getPlayerInfos(Profile profile, List<ProxiedPlayer> players) {
                int playerCount = players.size();
                int maxPlayers = profile.maxPlayers;
                if (profile.fixedPlayerList == null) {
                    return EMPTY_PLAYER_LIST;
                }
                PlayerInfo[] result = new PlayerInfo[profile.fixedPlayerList.size()];
                for (int i = 0; i < profile.fixedPlayerList.size(); i++) {
                    result[i] = new PlayerInfo(String.format(profile.fixedPlayerList.get(i),playerCount,maxPlayers), (UUID) null);
                }
                return result;
            }
        };

        public abstract PlayerInfo[] getPlayerInfos(Profile profile, List<ProxiedPlayer> players);

    }
    private String icon;
    private String protocolName;
    private int protocolVersion;
    private List<String> dynamicMotd;
    private List<String> staticMotd;
    private int maxPlayers;
    private List<String> playerListServers;
    private String playerListType;
    private List<String> fixedPlayerList;

    private transient boolean loadedFavicon;
    private transient Favicon favicon;
    private transient boolean loadedProtocol;
    private transient Protocol protocol;
    private transient boolean loadedPlayerList;
    private transient Set<ServerInfo> playerListServersSet;
    private transient PlayerListType playerListTypeEnum;

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

    public Players getPlayers(Main plugin) {
        ProxyServer proxy = plugin.getProxy();
        synchronized(this) {
            if (!loadedPlayerList) {
                if (playerListServers != null) {
                    playerListServersSet = new HashSet(playerListServers.size());
                    for (String server : playerListServers) {
                        ServerInfo serverInfo = proxy.getServerInfo(server);
                        if (serverInfo != null) {
                            playerListServersSet.add(serverInfo);
                        }
                    }
                }
                loadedPlayerList = true;
            }
            try {
                playerListTypeEnum = PlayerListType.valueOf(playerListType.toUpperCase());
            } catch (IllegalArgumentException ex) {
                playerListTypeEnum = PlayerListType.NONE;
            } catch (NullPointerException ex) {
                playerListTypeEnum = PlayerListType.NONE;
            }
        }
        List<ProxiedPlayer> result = new LinkedList<ProxiedPlayer>();
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            Server server = player.getServer();
            if (server != null) {
                if (playerListServersSet != null) {
                    if(playerListServersSet.contains(server.getInfo())) {
                        result.add(player);
                    }
                } else {
                    result.add(player);
                }
            }
        }
        return new Players(maxPlayers, result.size(), playerListTypeEnum.getPlayerInfos(this, result));
    }
}
