package nz.timo.websocket;

public interface PollableRegistrar {
    void register(Pollable pollable);

    void unregister(Pollable pollable);
}
