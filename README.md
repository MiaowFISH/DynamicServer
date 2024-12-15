# Dynamic Server

a plugin for velocity

Launch the Minecraft server dynamicly.

### Config

```yaml
servers:
  Lobby:
    name: Server
    enable: false
    lastCheck: CheckTime
    liveTime: "30m"
  BedWars:
    name: BedWars
    enable: false
    lastCheck: CheckTime
    liveTime: "30m"
```

### Commands

- /ds add <Server> [time]
- /ds del <Server>
- /ds set <Server> key value
- /ds enable <Server>
- /ds disable <Server>
- /ds connect <Server>

### How it works?

Check if the server is running

>    Running: run command `/server <Server>`

>    Stop: send a wait massage and launch the server.



Wait for the last player to leave. Trigger at X minutes timers.

After the X minutes, check if no one is connected.
>    No one is connected: shutdown the server.

>    Someone is connected: cancel the timer.
