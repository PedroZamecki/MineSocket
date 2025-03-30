package org.zamecki.minesocket.event;

import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private final Map<String, IGameEvent> events = new HashMap<>();
    private final List<IGameEvent> runningEvents = new ArrayList<>();
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
            event.start(args);
            runningEvents.add(event);
            return true;
        }
        return false;
    }

    public void onServerTick() {
        runningEvents.removeIf(IGameEvent::tick);
    }
}
