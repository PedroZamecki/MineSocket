package org.zamecki.minesocket.event;

public interface IGameEvent {
    String getName();

    void start(String[] args);

    boolean tick();
}
