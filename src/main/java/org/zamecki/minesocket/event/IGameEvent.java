package org.zamecki.minesocket.event;

public interface IGameEvent {
    String getName();

    boolean start(String[] args);

    boolean tick();
}
