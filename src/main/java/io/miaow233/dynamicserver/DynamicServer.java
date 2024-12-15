package io.miaow233.dynamicserver;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import io.miaow233.dynamicserver.commands.DynamicServerCommand;
import io.miaow233.dynamicserver.config.ConfigProvider;
import io.miaow233.dynamicserver.config.Settings;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

@Plugin(
        id = "dynamicserver",
        name = "DynamicServer",
        version = BuildConstants.VERSION,
        authors = {"miaow233"}
)
@SuppressWarnings("unused")
public class DynamicServer implements ConfigProvider {

    private final ProxyServer server;
    private final Logger logger;
    private final Path configDirectory;
    @Setter
    private Settings settings;

    @Inject
    public DynamicServer(ProxyServer server, Logger logger, @DataDirectory Path configDirectory) {
        this.server = server;
        this.logger = logger;
        this.configDirectory = configDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfigs();
        registerCommands();
        server.getEventManager().register(this, new EmptyServerListener(this));
        logger.info("Successfully enabled DynamicServer");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        server.getScheduler().tasksByPlugin(this).forEach(ScheduledTask::cancel);
        logger.info("Successfully disabled DynamicServer");
    }

    @Subscribe
    public void proxyReload(@NotNull ProxyReloadEvent event) {
        this.loadConfigs();
        this.log("DynamicServer has been reloaded!");
    }

    public void loadConfigs() {
        loadSettings();
    }

    private void registerCommands() {
        final BrigadierCommand command = new DynamicServerCommand(this).command();
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder(command).plugin(this).build(),
                command
        );
    }

    @Override
    @NotNull
    public DynamicServer getPlugin() {
        return this;
    }

    @Override
    public @NotNull Settings getSettings() {
        return settings;
    }

    @Override
    public @NotNull Path getConfigDirectory() {
        return configDirectory;
    }

    public Optional<RegisteredServer> getServer(String name) {
        return server.getServer(name);
    }

    public Collection<RegisteredServer> getAllServers() {
        return server.getAllServers();
    }


    public void log(@NotNull String message) {
        logger.info(message);
    }

    public Scheduler getScheduler() {
        return server.getScheduler();
    }
}


