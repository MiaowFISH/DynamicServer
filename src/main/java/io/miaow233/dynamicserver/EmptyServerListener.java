package io.miaow233.dynamicserver;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.TaskStatus;
import io.miaow233.dynamicserver.config.Server;
import io.miaow233.dynamicserver.config.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class EmptyServerListener {

    private final DynamicServer plugin;
    private final Map<String, ScheduledTask> m_Timers = new HashMap<>();

    public EmptyServerListener(DynamicServer plugin) {
        this.plugin = plugin;
        Settings settings = plugin.getSettings();
        settings.getServers().stream().filter(Server::isEnable).forEach(server -> {
            if (server.isShutdownAtStart()) {
                CheckAndStop(server.getName());
                return;
            }
            CheckPlayerNumber(server.getName());
        });
        checkOnlineStatus();
    }

    /**
     * Checks the online status of all registered servers.
     * <p>
     * This method schedules a task to periodically check the online status of all registered servers.
     * It iterates over each server, pings it to check its status, and updates the status in the plugin's settings.
     * If the server is in the LAUNCHING state, it does nothing.
     * If the ping fails, the server's status is set to OFFLINE and a log message is printed.
     *
     * @throws None
     */
    private void checkOnlineStatus() {
        plugin.getScheduler().buildTask(plugin, () -> {
            // Check Server Status
            plugin.getAllServers().forEach(server -> {
                String serverName = server.getServerInfo().getName();
                server.ping().thenAccept(ping -> {
                    if (plugin.getSettings().getServerFromName(serverName).getStatus() == Server.STATUS.LAUNCHING) {

                    }
                    plugin.getSettings().getServerFromName(serverName).setStatus(Server.STATUS.ONLINE);
                }).exceptionally(ex -> {
                    plugin.log(String.format("Server[%s] OFFLINE.", serverName));
                    plugin.getSettings().getServerFromName(serverName).setStatus(Server.STATUS.OFFLINE);
                    return null;
                });
            });
        }).repeat(30L, TimeUnit.SECONDS).schedule();
    }

    private int GetPlayerNumber(String serverName) {
        RegisteredServer server = plugin.getServer(serverName).orElse(null);
        if (server == null) return -1;
        try {
            return server.getPlayersConnected().size();
        } catch (IllegalArgumentException _ex) {
            return -1;
        }
    }

    private void CheckPlayerNumber(String serverName) {
        if (!plugin.getSettings().getServerFromName(serverName).isEnable()) return;
        plugin.log(String.format("Server[%s] check player number.", serverName));
        int playerNumber = GetPlayerNumber(serverName);
        if (playerNumber == -1) {
        }
        else if (playerNumber <= 0) {
            int m_ShutdownTimeInMinutes = plugin.getSettings().getServerFromName(serverName).getShutdownTimeInMinutes();
            plugin.log(String.format("Server[%s] empty -> shutdown in %d minute(s).", serverName, m_ShutdownTimeInMinutes));
            CancelTimer(serverName);
            ScheduledTask m_currentTimer = plugin.getScheduler().buildTask(this.plugin, () -> CheckAndStop(serverName)).delay(m_ShutdownTimeInMinutes, TimeUnit.MINUTES).schedule();
            m_Timers.put(serverName, m_currentTimer);
        }
    }

    //Player Exit Handler
    private void CheckAndStop(String serverName) {
        if (!plugin.getSettings().getServerFromName(serverName).isEnable()) return;
        int playerNumber = GetPlayerNumber(serverName);
        if (playerNumber == -1) {
        }
        else if (playerNumber <= 0) {
            plugin.log(String.format("[EmptyServerStopper]Server[%s] empty -> Shutting Down.", serverName));
            this.shutdown(serverName);
        } else {
            plugin.log("Shutdown abort someone is connected.");
        }
    }

    private void CancelTimer(String serverName) {
        ScheduledTask m_currentTimer = m_Timers.get(serverName);
        if (m_currentTimer == null) return;
        if (m_currentTimer.status() == TaskStatus.CANCELLED) {
            plugin.log(String.format("Task for Server[%s] has been Cancelled.", serverName));
            m_Timers.put(serverName, null);
        } else {
            plugin.log("Cancel Timer: " + serverName);
            m_currentTimer.cancel();
        }
    }

    private void shutdown(String serverName) {
        plugin.log("Shutting Down Server: " + serverName);
        plugin.getSettings().getServer(serverName).ifPresent(server -> {
            server.shutdown();
            plugin.log("Shutdown Server: " + serverName);
        });
    }

    private void startup(String serverName) {
        plugin.log("Starting Server: " + serverName);
        plugin.getSettings().getServer(serverName).ifPresent(server -> {
            server.launch();
            plugin.log("Start Server: " + serverName);
        });
    }

    @Subscribe
    public void onKick(@NotNull KickedFromServerEvent event) {
        event.getPlayer().getTabList().getEntries().stream().filter(entry -> entry.getProfile() != null && !entry.getProfile().getId().equals(event.getPlayer().getUniqueId())).forEach(entry -> event.getPlayer().getTabList().removeEntry(entry.getProfile().getId()));
        event.getPlayer().getTabList().clearHeaderAndFooter();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onPlayerJoin(@NotNull ServerPostConnectEvent event) {
        final Player joined = event.getPlayer();
        final String serverName = joined.getCurrentServer().map(ServerConnection::getServerInfo).map(ServerInfo::getName).orElse("");
        CancelTimer(serverName);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerQuit(@NotNull DisconnectEvent event) {
        plugin.log("Player Quit: " + event.getPlayer().getUsername());
        for (String serverName : m_Timers.keySet()) {
            CheckPlayerNumber(serverName);
        }
    }

    @Subscribe
    public void onPlayerSwitchServer(@NotNull ServerConnectedEvent event) {
        String targetServer = event.getServer().getServerInfo().getName();
        RegisteredServer previousServer = event.getPreviousServer().orElse(null);
        CancelTimer(targetServer);
        if (previousServer != null) {
            CheckPlayerNumber(previousServer.getServerInfo().getName());
        }
    }
}
