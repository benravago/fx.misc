package fx.react;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fx.react.util.Timer;

class ScheduledExecutorServiceTimer implements Timer {

  static Timer create(java.time.Duration timeout, Runnable action, ScheduledExecutorService scheduler, Executor eventThreadExecutor) {
    return new ScheduledExecutorServiceTimer(timeout, action,
      (delay, unit, cmd) -> scheduler.schedule(cmd, delay, unit),
      eventThreadExecutor);
  }

  static Timer createPeriodic(java.time.Duration timeout, Runnable action, ScheduledExecutorService scheduler, Executor eventThreadExecutor) {
    return new ScheduledExecutorServiceTimer(timeout, action,
      (delay, unit, cmd) -> scheduler.scheduleAtFixedRate(cmd, delay, delay, unit),
      eventThreadExecutor);
  }

  final long timeout;
  final TimeUnit unit;
  final Runnable action;
  final Scheduler<Long, TimeUnit, Runnable, ScheduledFuture<?>> scheduler;
  final Executor eventThreadExecutor;

  ScheduledFuture<?> pendingTimer = null;
  long seq = 0;

  ScheduledExecutorServiceTimer(java.time.Duration timeout, Runnable action, Scheduler<Long, TimeUnit, Runnable, ScheduledFuture<?>> scheduler, Executor eventThreadExecutor) {
    this.timeout = timeout.toNanos();
    this.unit = TimeUnit.NANOSECONDS;
    this.action = action;
    this.scheduler = scheduler;
    this.eventThreadExecutor = eventThreadExecutor;
  }

  @Override
  public final void restart() {
    stop();
    var expected = seq;
    pendingTimer = scheduler.apply(timeout, unit, () -> {
      eventThreadExecutor.execute(() -> {
        if (seq == expected) {
          action.run();
        }
      });
    });
  }

  @Override
  public final void stop() {
    if (pendingTimer != null) {
      pendingTimer.cancel(false);
    }
    ++seq;
  }

}
