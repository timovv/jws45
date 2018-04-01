package nz.timo.websocket;

public interface Schedulable {
    void invoke(ScheduledTaskContext ctx);
}
