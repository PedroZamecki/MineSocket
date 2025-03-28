package org.zamecki.minesocket.event;

import net.minecraft.server.MinecraftServer;
import java.util.HashMap;
import java.util.Map;

public class EventManager {
    private final Map<String, IGameEvent> events = new HashMap<>();
    private final MinecraftServer server;

    public EventManager(MinecraftServer server) {
        this.server = server;
        registerDefaultEvents();
    }

    private void registerDefaultEvents() {
        registerEvent(new FireworkEvent(server));
    }

    public void registerEvent(IGameEvent event) {
        events.put(event.getName().toLowerCase(), event);
    }

    public boolean handleEvent(String eventName, String[] args) {
        IGameEvent event = events.get(eventName.toLowerCase());
        if (event != null) {
            event.execute(args);
            return true;
        }
        return false;
    }
}
