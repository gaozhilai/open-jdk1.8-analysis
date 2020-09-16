/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.*;

/**
 * Provides default implementations of {@link ExecutorService}
 * execution methods. This class implements the {@code submit},
 * {@code invokeAny} and {@code invokeAll} methods using a
 * {@link RunnableFuture} returned by {@code newTaskFor}, which defaults
 * to the {@link FutureTask} class provided in this package.  For example,
 * the implementation of {@code submit(Runnable)} creates an
 * associated {@code RunnableFuture} that is executed and
 * returned. Subclasses may override the {@code newTaskFor} methods
 * to return {@code RunnableFuture} implementations other than
 * {@code FutureTask}.
 *
 * <p><b>Extension example</b>. Here is a sketch of a class
 * that customizes {@link ThreadPoolExecutor} to use
 * a {@code CustomTask} class instead of the default {@code FutureTask}:
 *  <pre> {@code
 * public class CustomThreadPoolExecutor extends ThreadPoolExecutor {
 *
 *   static class CustomTask<V> implements RunnableFuture<V> {...}
 *
 *   protected <V> RunnableFuture<V> newTaskFor(Callable<V> c) {
 *       return new CustomTask<V>(c);
 *   }
 *   protected <V> RunnableFuture<V> newTaskFor(Runnable r, V v) {
 *       return new CustomTask<V>(r, v);
 *   }
 *   // ... add constructors, etc.
 * }}</pre>
 *
 * @since 1.5
 * @author Doug Lea
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/9/15 10:58
public abstract class AbstractExecutorService implements ExecutorService { /** {@link ExecutorService}的骨架实现类, 提供了一些默认方法实现 */

    /**
     * Returns a {@code RunnableFuture} for the given runnable and default
     * value.
     *
     * @param runnable the runnable task being wrapped
     * @param value the default value for the returned future
     * @param <T> the type of the given value
     * @return a {@code RunnableFuture} which, when run, will run the
     * underlying runnable and which, as a {@code Future}, will yield
     * the given value as its result and provide for cancellation of
     * the underlying task
     * @since 1.6
     */ // 将Runnable任务包装成FutureTask
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new FutureTask<T>(runnable, value);
    }

    /**
     * Returns a {@code RunnableFuture} for the given callable task.
     *
     * @param callable the callable task being wrapped
     * @param <T> the type of the callable's result
     * @return a {@code RunnableFuture} which, when run, will call the
     * underlying callable and which, as a {@code Future}, will yield
     * the callable's result as its result and provide for
     * cancellation of the underlying task
     * @since 1.6
     */ // 将Callable任务包装成FutureTask
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new FutureTask<T>(callable);
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */ /** 见{@link ExecutorService#submit(Runnable)} */
    public Future<?> submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<Void> ftask = newTaskFor(task, null); // 将给定任务包装成FutureTask
        execute(ftask); // 提交执行任务, 注意execute方法是抽象的, 需要具体子类实现
        return ftask; // 返回包装后的任务, 此时返回结果代表任务异步执行结果
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */ /** 见{@link ExecutorService#submit(Runnable, Object)} */
    public <T> Future<T> submit(Runnable task, T result) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task, result); // 将给定任务包装成FutureTask
        execute(ftask); // 提交执行任务, 注意execute方法是抽象的, 需要具体子类实现
        return ftask; // 返回包装后的任务, 此时返回结果代表任务异步执行结果
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */ /** 见{@link ExecutorService#submit(Callable)} */
    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<T> ftask = newTaskFor(task); // 将给定任务包装成FutureTask
        execute(ftask); // 提交执行任务, 注意execute方法是抽象的, 需要具体子类实现
        return ftask; // 返回包装后的任务, 此时返回结果代表任务异步执行结果
    }

    /**
     * the main mechanics of invokeAny.
     */ // invokeAny的具体实现
    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks,
                              boolean timed, long nanos)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks == null)
            throw new NullPointerException();
        int ntasks = tasks.size(); // 获得需要执行的任务数量
        if (ntasks == 0)
            throw new IllegalArgumentException();
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(ntasks);
        ExecutorCompletionService<T> ecs =
            new ExecutorCompletionService<T>(this); // 使用当前实例创建一个CompletionService实例

        // For efficiency, especially in executors with limited
        // parallelism, check to see if previously submitted tasks are
        // done before submitting more of them. This interleaving
        // plus the exception mechanics account for messiness of main
        // loop.

        try {
            // Record exceptions so that if we fail to obtain any
            // result, we can throw the last exception we got.
            ExecutionException ee = null;
            final long deadline = timed ? System.nanoTime() + nanos : 0L; // 获取预计超时时间
            Iterator<? extends Callable<T>> it = tasks.iterator(); // 获得要执行任务集合的迭代器

            // Start one task for sure; the rest incrementally
            futures.add(ecs.submit(it.next())); // 将任务提交给ExecutorCompletionService执行, 并且将Future结果放入List保存
            --ntasks; // 任务计数减一
            int active = 1; // 活跃任务计数初始化1

            for (;;) { // 自旋
                Future<T> f = ecs.poll(); // 查看是否有任务执行完毕
                if (f == null) { // 如果没有执行完毕的任务
                    if (ntasks > 0) { // 并且有剩余待执行任务
                        --ntasks; // 待执行任务计数减一
                        futures.add(ecs.submit(it.next())); // 提交执行一个任务
                        ++active; // 活跃任务计数加一
                    }
                    else if (active == 0) // 如果活跃任务计数为0
                        break; // 跳出循环
                    else if (timed) { // 如果指定了超时时间
                        f = ecs.poll(nanos, TimeUnit.NANOSECONDS); // 那么用指定超时时间的轮询方法
                        if (f == null) // 如果还是没有任务执行完毕
                            throw new TimeoutException(); // 抛出超时异常
                        nanos = deadline - System.nanoTime(); // 否则更新剩余超时时间
                    }
                    else
                        f = ecs.take(); // 否则执行阻塞的获取执行结果方法
                }
                if (f != null) { // 如果有任务执行完毕
                    --active; // 活跃任务计数减一
                    try {
                        return f.get(); // 返回获得结果
                    } catch (ExecutionException eex) {
                        ee = eex;
                    } catch (RuntimeException rex) {
                        ee = new ExecutionException(rex);
                    }
                }
            }

            if (ee == null)
                ee = new ExecutionException();
            throw ee;

        } finally {
            for (int i = 0, size = futures.size(); i < size; i++) // 已经获取到执行结果
                futures.get(i).cancel(true); // 将剩余未执行完毕的任务每个都取消
        }
    }
    /** 见{@link ExecutorService#invokeAny(Collection)} */
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0);
        } catch (TimeoutException cannotHappen) {
            assert false;
            return null;
        }
    }
    /** 见{@link ExecutorService#invokeAny(Collection, long, TimeUnit)} */
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                           long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }
    /** 见{@link ExecutorService#invokeAll(Collection)} */
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException {
        if (tasks == null)
            throw new NullPointerException();
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false; // 所有任务完成标识
        try {
            for (Callable<T> t : tasks) {
                RunnableFuture<T> f = newTaskFor(t);
                futures.add(f); // 将所有任务异步结果暂存
                execute(f); // 调用具体方法执行任务
            }
            for (int i = 0, size = futures.size(); i < size; i++) { // 遍历所有异步结果
                Future<T> f = futures.get(i);
                if (!f.isDone()) { // 如果当前异步结果没有执行完毕
                    try {
                        f.get(); // 那么调用阻塞方法等待执行完毕
                    } catch (CancellationException ignore) {
                    } catch (ExecutionException ignore) {
                    }
                }
            }
            done = true; // 标记所有任务执行完毕
            return futures; // 将结果返回
        } finally {
            if (!done) // 如果存在没有执行完毕的方法
                for (int i = 0, size = futures.size(); i < size; i++)
                    futures.get(i).cancel(true); // 返回结果前对其进行取消
        }
    }
    /** 见{@link ExecutorService#invokeAll(Collection, long, TimeUnit)} */
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                         long timeout, TimeUnit unit)
        throws InterruptedException {
        if (tasks == null)
            throw new NullPointerException();
        long nanos = unit.toNanos(timeout); // 获得超时时间纳秒
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        boolean done = false; // 所有任务执行完毕标识
        try {
            for (Callable<T> t : tasks)
                futures.add(newTaskFor(t)); // 将所有任务异步结果暂存

            final long deadline = System.nanoTime() + nanos; // 计算预期超时时间
            final int size = futures.size();

            // Interleave time checks and calls to execute in case
            // executor doesn't have any/much parallelism.
            for (int i = 0; i < size; i++) { // 遍历每一个任务
                execute((Runnable)futures.get(i)); // 实际执行任务
                nanos = deadline - System.nanoTime(); // 计算剩余超时时间
                if (nanos <= 0L) // 如果已经超时
                    return futures; // 直接返回异步结果
            }

            for (int i = 0; i < size; i++) { // 遍历每一个异步结果
                Future<T> f = futures.get(i);
                if (!f.isDone()) { // 如果没有完成
                    if (nanos <= 0L) // 并且超时
                        return futures; // 直接返回异步结果
                    try {
                        f.get(nanos, TimeUnit.NANOSECONDS); // 如果没有超时就等待当前任务执行完毕
                    } catch (CancellationException ignore) {
                    } catch (ExecutionException ignore) {
                    } catch (TimeoutException toe) {
                        return futures; // 有超时异常则返回异步结果
                    }
                    nanos = deadline - System.nanoTime(); // 更新剩余超时时间
                }
            }
            done = true; // 标记所有任务执行完毕
            return futures; // 返回异步结果
        } finally {
            if (!done) // 如果不是所有任务执行完毕
                for (int i = 0, size = futures.size(); i < size; i++) // 遍历每个任务
                    futures.get(i).cancel(true); // 对任务进行取消
        }
    }

}
