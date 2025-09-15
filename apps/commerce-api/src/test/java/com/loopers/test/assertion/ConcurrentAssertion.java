package com.loopers.test.assertion;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConcurrentAssertion<V> {

    private final int threadCount;
    private final Supplier<ExecutorService> executorServiceProvider;

    private final AtomicInteger successCount = new AtomicInteger(0);
    private final List<Throwable> errors = new CopyOnWriteArrayList<>();

    public static Initializer assertThatConcurrence() {
        return new Initializer();
    }

    public ConcurrentAssertion<V> isDone() {
        assertThat(this.successCount.get() + this.errors.size()).isEqualTo(this.threadCount);
        return this;
    }

    public ConcurrentAssertion<V> hasNoError() {
        assertThat(this.successCount).hasValue(this.threadCount);
        assertThat(this.errors).isEmpty();
        return this;
    }

    public ConcurrentAssertion<V> hasErrorCount(int errorCount) {
        assertThat(this.errors).hasSize(errorCount);
        return this;
    }

    public ConcurrentAssertion<V> isThrown() {
        assertThat(this.errors).isNotEmpty();
        return this;
    }

    public <T extends Throwable> ConcurrentAssertion<V> isThrownBy(Class<T> type) {
        assertThat(this.errors).isNotEmpty().allMatch(type::isInstance);
        return this;
    }

    public ConcurrentAssertion<V> hasFieldOrPropertyWithValue(String name, Object value) {
        assertThat(this.errors).isNotEmpty().element(0).hasFieldOrPropertyWithValue(name, value);
        return this;
    }

    private ConcurrentAssertion<V> execute(RunnableCallableAdapter<V> adapter) {
        CyclicBarrier barrier = new CyclicBarrier(this.threadCount);
        CountDownLatch latch = new CountDownLatch(this.threadCount);

        try (ExecutorService executorService = this.executorServiceProvider.get()) {
            for (int i = 0; i < this.threadCount; i++) {
                int n = i;
                executorService.submit(() -> {
                    try {
                        // Suspend all threads until ready to execute.
                        barrier.await(10, TimeUnit.SECONDS);

                        V executed = adapter.execute(n);
                        this.successCount.incrementAndGet();
                        return executed;
                    } catch (Throwable t) {
                        this.errors.add(t);
                        return null;
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all threads to complete execution.
            if (!latch.await(10, TimeUnit.MINUTES)) {
                throw new TimeoutException("Timed out waiting for an executor service to execute");
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return this;
    }

    // -------------------------------------------------------------------------------------------------

    public static class Initializer {

        private static final int DEFAULT_THREAD_COUNT = 1;
        private static final Supplier<ExecutorService> DEFAULT_EXECUTOR_SERVICE_PROVIDER = Executors::newVirtualThreadPerTaskExecutor;

        private int threadCount = DEFAULT_THREAD_COUNT;
        private Supplier<ExecutorService> executorServiceProvider = DEFAULT_EXECUTOR_SERVICE_PROVIDER;

        public Initializer withThreadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Initializer withExecutorServiceProvider(Supplier<ExecutorService> provider) {
            this.executorServiceProvider = provider;
            return this;
        }

        public <V> ConcurrentAssertion<V> isExecutedBy(Runnable runnable) {
            ConcurrentAssertion<V> assertion = new ConcurrentAssertion<>(this.threadCount, this.executorServiceProvider);
            RunnableCallableAdapter<V> adapter = new RunnableCallableAdapter<>(runnable);

            return assertion.execute(adapter);
        }

        public <V> ConcurrentAssertion<V> isExecutedBy(Callable<V> callable) {
            ConcurrentAssertion<V> assertion = new ConcurrentAssertion<>(this.threadCount, this.executorServiceProvider);
            RunnableCallableAdapter<V> adapter = new RunnableCallableAdapter<>(callable);

            return assertion.execute(adapter);
        }

        public <V> ConcurrentAssertion<V> isExecutedBy(Consumer<Integer> consumer) {
            ConcurrentAssertion<V> assertion = new ConcurrentAssertion<>(this.threadCount, this.executorServiceProvider);
            RunnableCallableAdapter<V> adapter = new RunnableCallableAdapter<>(consumer);

            return assertion.execute(adapter);
        }

    }

    // -------------------------------------------------------------------------------------------------

    private static class RunnableCallableAdapter<V> implements Runnable, Callable<V>, Consumer<Integer> {

        private final Runnable runnable;
        private final Callable<V> callable;
        private final Consumer<Integer> consumer;

        private RunnableCallableAdapter(Runnable runnable) {
            this.runnable = runnable;
            this.callable = null;
            this.consumer = null;
        }

        private RunnableCallableAdapter(Callable<V> callable) {
            this.runnable = null;
            this.callable = callable;
            this.consumer = null;
        }

        private RunnableCallableAdapter(Consumer<Integer> consumer) {
            this.runnable = null;
            this.callable = null;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            if (this.runnable != null) {
                this.runnable.run();
                return;
            }

            if (this.callable != null) {
                try {
                    this.callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            if (this.consumer != null) {
                this.consumer.accept(null);
            }
        }

        @Override
        public V call() throws Exception {
            if (this.runnable != null) {
                this.runnable.run();
                return null;
            }

            if (this.callable != null) {
                return this.callable.call();
            }

            if (this.consumer != null) {
                this.consumer.accept(null);
                return null;
            }

            return null;
        }

        @Override
        public void accept(Integer i) {
            if (this.runnable != null) {
                this.runnable.run();
                return;
            }

            if (this.callable != null) {
                try {
                    this.callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            if (this.consumer != null) {
                this.consumer.accept(i);
            }
        }

        public V execute(Integer i) throws Exception {
            if (this.runnable != null) {
                run();
                return null;
            }

            if (this.callable != null) {
                return call();
            }

            if (this.consumer != null) {
                accept(i);
                return null;
            }

            return null;
        }

    }

}
