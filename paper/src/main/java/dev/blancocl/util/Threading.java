package dev.blancocl.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Single source of truth for every executor the plugin uses.
 *
 * <p>The plugin enforces a strict threading model:</p>
 * <ul>
 *   <li><b>Bukkit main</b> — packet sends, world reads, event dispatch.</li>
 *   <li><b>visibility()</b> — CPU-bound async work (distance checks, view-set diffs).</li>
 *   <li><b>skinIo()</b> — virtual threads for HTTP / disk.</li>
 *   <li><b>scheduled()</b> — periodic ticks (hologram animation, velocity pings).</li>
 * </ul>
 *
 * <p>Callers can defensively check with {@link #assertMain()} / {@link #assertAsync()}.</p>
 */
public final class Threading {

    private final Plugin plugin;
    private final String mainThreadName;
    private final ExecutorService visibility;
    private final ExecutorService skinIo;
    private final ScheduledExecutorService scheduled;

    public Threading(Plugin plugin, int visibilityParallelism, boolean useVirtualThreads) {
        this.plugin = plugin;
        this.mainThreadName = Thread.currentThread().getName();

        int parallelism = visibilityParallelism > 0
                ? visibilityParallelism
                : Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        this.visibility = new ForkJoinPool(
                parallelism,
                new NamedForkJoinThreadFactory("npc-visibility"),
                (t, e) -> plugin.getLogger().severe("Uncaught in npc-visibility: " + e),
                true);

        this.skinIo = useVirtualThreads
                ? Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("npc-skin-", 0).factory())
                : Executors.newFixedThreadPool(4, namedFactory("npc-skin"));

        this.scheduled = Executors.newScheduledThreadPool(2, namedFactory("npc-sched"));
    }

    public Plugin plugin()                { return plugin; }
    public ExecutorService visibility()   { return visibility; }
    public ExecutorService skinIo()       { return skinIo; }
    public ScheduledExecutorService scheduled() { return scheduled; }

    /** Run {@code task} on the Bukkit main thread. No-op queued if already main. */
    public void onMain(Runnable task) {
        if (Bukkit.isPrimaryThread()) task.run();
        else Bukkit.getScheduler().runTask(plugin, task);
    }

    public <T> CompletableFuture<T> supplyMain(java.util.function.Supplier<T> supplier) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        onMain(() -> {
            try { cf.complete(supplier.get()); }
            catch (Throwable t) { cf.completeExceptionally(t); }
        });
        return cf;
    }

    public void assertMain() {
        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("Expected main thread, was " + Thread.currentThread().getName());
    }

    public void assertAsync() {
        if (Bukkit.isPrimaryThread())
            throw new IllegalStateException("Expected async thread, was on main");
    }

    /** Drain & shutdown — called from {@code onDisable}. */
    public void shutdown(long timeoutMs) {
        for (ExecutorService es : new ExecutorService[]{visibility, skinIo, scheduled}) {
            es.shutdown();
            try { es.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            if (!es.isTerminated()) es.shutdownNow();
        }
    }

    private static ThreadFactory namedFactory(String prefix) {
        AtomicInteger i = new AtomicInteger();
        return r -> {
            Thread t = new Thread(r, prefix + "-" + i.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
    }

    private static final class NamedForkJoinThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        private final String prefix;
        private final AtomicInteger i = new AtomicInteger();
        NamedForkJoinThreadFactory(String prefix) { this.prefix = prefix; }
        @Override public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            t.setName(prefix + "-" + i.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }
}
