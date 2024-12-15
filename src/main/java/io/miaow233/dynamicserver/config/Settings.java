package io.miaow233.dynamicserver.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import io.miaow233.dynamicserver.DynamicServer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;


@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Settings implements ConfigValidator {
    public static final String CONFIG_HEADER = """
            ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
            ┃       Velocitab Config       ┃
            ┃    Developed by William278   ┃
            ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
            ┣╸ Information: https://william278.net/project/velocitab
            ┗╸ Documentation: https://william278.net/docs/velocitab""";

    @Comment("Check for updates on startup")
    private boolean checkForUpdates = true;

    @Comment("Servers config")
    private List<Server> servers = List.of();

    @NotNull
    public Server getServerFromName(@NotNull String name) {
        return servers.stream().filter(group -> group.getName().equals(name)).findFirst().orElseThrow(() -> new IllegalStateException("No group with name " + name + " found"));
    }

    @NotNull
    public Optional<Server> getServer(@NotNull String name) {
        return servers.stream().filter(server -> server.getName().equals(name)).findFirst();
    }

    @Override
    public void validateConfig(@NotNull DynamicServer plugin) throws IllegalStateException {

    }
}