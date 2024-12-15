package io.miaow233.dynamicserver.config;

import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;


@Getter
@SuppressWarnings("unused")
@Configuration
public class Server{
    private String name;
    private boolean enable;
    private boolean shutdownAtStart;
    private int shutdownTimeInMinutes;
    @Setter
    private STATUS status = STATUS.UNKNOWN;

    public void launch() {
        if (this.status == STATUS.LAUNCHING) return;
        try {
            System.out.println("Run command: " + "/usr/bin/pm2 start " + name);
            Runtime.getRuntime().exec("/usr/bin/pm2 start " + name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.status = STATUS.LAUNCHING;
    }

    public void shutdown() {
        if (this.status == STATUS.OFFLINE && !this.shutdownAtStart) return;
        try {
            System.out.println("Run command: " + "/usr/bin/pm2 stop " + name);
            Runtime.getRuntime().exec("/usr/bin/pm2 stop " + name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.status = STATUS.OFFLINE;
    }

    public enum STATUS{
        ONLINE,
        OFFLINE,
        LAUNCHING,
        UNKNOWN
    }
}
