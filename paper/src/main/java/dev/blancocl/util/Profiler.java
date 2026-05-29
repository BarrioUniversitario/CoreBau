package dev.blancocl.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/** Lightweight always-on counters + opt-in nanosecond timers. */
public final class Profiler {

    public interface Scope extends AutoCloseable {
        @Override void close();
    }

    private final boolean enabled;
    private final Map<String, LongAdder> counters = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> timers   = new ConcurrentHashMap<>();

    public Profiler(boolean enabled) { this.enabled = enabled; }

    public void incr(String key) {
        if (!enabled) return;
        counters.computeIfAbsent(key, k -> new LongAdder()).increment();
    }

    public Scope time(String key) {
        if (!enabled) return () -> {};
        long start = System.nanoTime();
        return () -> timers.computeIfAbsent(key, k -> new LongAdder()).add(System.nanoTime() - start);
    }

    public Map<String, Long> snapshotCounters() {
        Map<String, Long> out = new java.util.LinkedHashMap<>();
        counters.forEach((k, v) -> out.put(k, v.sum()));
        return out;
    }

    public Map<String, Long> snapshotTimersNanos() {
        Map<String, Long> out = new java.util.LinkedHashMap<>();
        timers.forEach((k, v) -> out.put(k, v.sum()));
        return out;
    }

    public boolean enabled() { return enabled; }
}
