package nz.timo.websocket;

public interface Scheduler {

    void schedule(Schedulable toSchedule);

    void scheduleRepeating(Schedulable toSchedule);

    void scheduleLater(Schedulable toSchedule, long delayMillis);
}
