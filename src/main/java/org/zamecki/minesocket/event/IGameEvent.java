package org.zamecki.minesocket.event;

public interface IGameEvent {
    void execute(String[] args);
    String getName();
}
