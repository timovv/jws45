package nz.timo.websocket;

import java.util.PriorityQueue;

/**
 * A polling scheduler.
 */
public class PollableScheduler implements Scheduler {

    private PriorityQueue<SchedulingItem> invocationQueue = new PriorityQueue<>();

    public void schedule(Schedulable toSchedule) {
        scheduleLater(toSchedule, 0L);
    }

    @Override
    public void scheduleRepeating(Schedulable toSchedule) {
        invocationQueue.add(new SchedulingItem(toSchedule, System.nanoTime(), true));
    }

    public void scheduleLater(Schedulable toSchedule, long delayMillis) {
        invocationQueue.add(new SchedulingItem(toSchedule, System.nanoTime() + delayMillis * 1000L * 1000L, false));
    }

    public void poll() {
        if(!invocationQueue.isEmpty() && invocationQueue.peek().getDeadline() - System.nanoTime() <= 0) {
            SchedulingItem item = invocationQueue.poll();
            ScheduledTaskContextImpl ctx = new ScheduledTaskContextImpl(item.shouldRepeat());
            item.getSchedulable().invoke(ctx);
            if(ctx.getShouldRepeat()) {
                scheduleRepeating(item.getSchedulable());
            }
        }
    }

    private static class SchedulingItem implements Comparable<SchedulingItem> {
        private long deadline;
        private Schedulable schedulable;
        private final boolean shouldRepeat;

        private SchedulingItem(Schedulable schedulable, long deadline, boolean shouldRepeat) {
            this.deadline = deadline;
            this.schedulable = schedulable;
            this.shouldRepeat = shouldRepeat;
        }

        private long getDeadline() {
            return this.deadline;
        }

        private Schedulable getSchedulable() {
            return schedulable;
        }

        @Override
        public int compareTo(SchedulingItem schedulingItem) {
            return (int)(this.deadline - schedulingItem.deadline);
        }

        public boolean shouldRepeat() {
            return shouldRepeat;
        }
    }

    private class ScheduledTaskContextImpl implements ScheduledTaskContext {

        private boolean shouldRepeat;

        private ScheduledTaskContextImpl(boolean shouldRepeat) {
            this.shouldRepeat = shouldRepeat;
        }

        @Override
        public void setShouldRepeat(boolean repeat) {
            this.shouldRepeat = repeat;
        }

        private boolean getShouldRepeat() {
            return shouldRepeat;
        }
    }
}
