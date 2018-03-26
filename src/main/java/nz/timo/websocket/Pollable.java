package nz.timo.websocket;

public interface Pollable {
    PollAgainPriority poll();
}
