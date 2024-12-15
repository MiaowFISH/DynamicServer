package io.miaow233.dynamicserver.commands;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.miaow233.dynamicserver.DynamicServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public final class DynamicServerCommand {
    private static final TextColor MAIN_COLOR = TextColor.color(0x00FB9A);
    private final @NotNull DynamicServer plugin;

    public DynamicServerCommand(final @NotNull DynamicServer plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public BrigadierCommand command() {
        final LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder
                .<CommandSource>literal("ds")
                .executes(ctx -> {
                    sendAboutInfo(ctx.getSource());
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("about")
                        .executes(ctx -> {
                            sendAboutInfo(ctx.getSource());
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                        .requires(src -> src.hasPermission("dynamicserver.command.reload"))
                        .executes(ctx -> {
                            plugin.loadConfigs();
                            ctx.getSource().sendMessage(Component.text(
                                    "DynamicServer has been reloaded!",
                                    MAIN_COLOR));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("connect")
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("connect", StringArgumentType.greedyString())
                                .suggests((ctx, _builder) -> {
                                    // Here we provide the names of the players along with a tooltip,
                                    // which can be used as an explanation of a specific argument or as a simple decoration
                                    plugin.getAllServers().forEach(server -> _builder.suggest(
                                            server.getServerInfo().getName()
                                    ));
                                    // If you do not need to add a tooltip to the hint
                                    // or your command is intended only for versions lower than Minecraft 1.13,
                                    // you can omit adding the tooltip, since for older clients,
                                    // the tooltip will not be displayed.
                                    return _builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    if (!(ctx.getSource() instanceof Player player)) {
                                        ctx.getSource().sendMessage(Component.text("You must be a player to use this command!", MAIN_COLOR));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String serverName = StringArgumentType.getString(ctx, "connect");

                                    RegisteredServer server = plugin.getServer(serverName).orElse(null);
                                    if (server == null) {
                                        ctx.getSource().sendMessage(Component.text("Server not found!", MAIN_COLOR));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    switch (plugin.getSettings().getServerFromName(serverName).getStatus()) {
                                        case OFFLINE:
                                            ctx.getSource().sendMessage(Component.text("Server is offline! Launching...", MAIN_COLOR));
                                            plugin.getSettings().getServerFromName(serverName).launch();
                                            break;
                                        case LAUNCHING:
                                            ctx.getSource().sendMessage(Component.text("Server is launching. Please wait...", MAIN_COLOR));
                                            break;
                                        case ONLINE:
                                            player.createConnectionRequest(server).fireAndForget();
                                            break;
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                        ).executes(ctx -> {
                            if (!(ctx.getSource() instanceof Player player)) {
                                ctx.getSource().sendMessage(Component.text("You must be a player to use this command!", MAIN_COLOR));
                                return Command.SINGLE_SUCCESS;
                            }
                            ctx.getSource().sendMessage(Component.text("You must specify a server!", MAIN_COLOR));
                            return Command.SINGLE_SUCCESS;
                        })
                );

        return new BrigadierCommand(builder);
    }

    private void sendAboutInfo(@NotNull CommandSource source) {
        source.sendMessage(Component.text("DynamicServer", MAIN_COLOR));
        source.sendMessage(Component.text("Version: 1.0", MAIN_COLOR));
        source.sendMessage(Component.text("Author: Miaow233", MAIN_COLOR));
    }
}
