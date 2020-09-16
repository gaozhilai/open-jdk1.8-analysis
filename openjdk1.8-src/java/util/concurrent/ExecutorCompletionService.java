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

/**
 * A {@link CompletionService} that uses a supplied {@link Executor}
 * to execute tasks.  This class arranges that submitted tasks are,
 * upon completion, placed on a queue accessible using {@code take}.
 * The class is lightweight enough to be suitable for transient use
 * when processing groups of tasks.
 *
 * <p>
 *
 * <b>Usage Examples.</b>
 *
 * Suppose you have a set of solvers for a certain problem, each
 * returning a value of some type {@code Result}, and would like to
 * run them concurrently, processing the results of each of them that
 * return a non-null value, in some method {@code use(Result r)}. You
 * could write this as:
 *
 * <pre> {@code
 * void solve(Executor e,
 *            Collection<Callable<Result>> solvers)
 *     throws InterruptedException, ExecutionException {
 *     CompletionService<Result> ecs
 *         = new ExecutorCompletionService<Result>(e);
 *     for (Callable<Result> s : solvers)
 *         ecs.submit(s);
 *     int n = solvers.size();
 *     for (int i = 0; i < n; ++i) {
 *         Result r = ecs.take().get();
 *         if (r != null)
 *             use(r);
 *     }
 * }}</pre>
 *
 * Suppose instead that you would like to use the first non-null result
 * of the set of tasks, ignoring any that encounter exceptions,
 * and cancelling all other tasks when the first one is ready:
 *
 * <pre> {@code
 * void solve(Executor e,
 *            Collection<Callable<Result>> solvers)
 *     throws InterruptedException {
 *     CompletionService<Result> ecs
 *         = new ExecutorCompletionService<Result>(e);
 *     int n = solvers.size();
 *     List<Future<Result>> futures
 *         = new ArrayList<Future<Result>>(n);
 *     Result result = null;
 *     try {
 *         for (Callable<Result> s : solvers)
 *             futures.add(ecs.submit(s));
 *         for (int i = 0; i < n; ++i) {
 *             try {
 *                 Result r = ecs.take().get();
 *                 if (r != null) {
 *                     result = r;
 *                     break;
 *                 }
 *             } catch (ExecutionException ignore) {}
 *         }
 *     }
 *     finally {
 *         for (Future<Result> f : futures)
 *             f.cancel(true);
 *     }
 *
 *     if (result != null)
 *         use(result);
 * }}</pre>
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/9/16 16:33
public class ExecutorCompletionService<V> implements CompletionService<V> { // 按照任务执行完毕时间依次阻塞的获得执行结果, 阻塞效果通过阻塞队列实现
    private final Executor executor; // 任务的实际执行器
    private final AbstractExecutorService aes; // 如果执行器是AbstractExecutorService, 那么用AbstractExecutorService的newTask适配Callable任务
    private final BlockingQueue<Future<V>> completionQueue; // 存储任务执行结果的阻塞队列

    /**
     * FutureTask extension to enqueue upon completion
     */ // FutureTask的拓展, 实现了回调方法done(), 在回调的时候将产生的结果放入阻塞队列
    private class QueueingFuture extends FutureTask<Void> {
        QueueingFuture(RunnableFuture<V> task) {
            super(task, null);
            this.task = task;
        }
        protected void done() { completionQueue.add(task); }
        private final Future<V> task;
    }
    // 将Callable任务适配成FutureTask
    private RunnableFuture<V> newTaskFor(Callable<V> task) {
        if (aes == null) // 如果执行器不是AbstractExecutorService
            return new FutureTask<V>(task); // 直接构造FutureTask
        else
            return aes.newTaskFor(task); // 否则使用AbstractExecutorService的newTask方法进行适配
    }
    // 将Callable任务适配成FutureTask
    private RunnableFuture<V> newTaskFor(Runnable task, V result) {
        if (aes == null) // 如果执行器不是AbstractExecutorService
            return new FutureTask<V>(task, result); // 直接构造FutureTask
        else
            return aes.newTaskFor(task, result); // 否则使用AbstractExecutorService的newTask方法进行适配
    }

    /**
     * Creates an ExecutorCompletionService using the supplied
     * executor for base task execution and a
     * {@link LinkedBlockingQueue} as a completion queue.
     *
     * @param executor the executor to use
     * @throws NullPointerException if executor is {@code null}
     */ // 根据给定的执行器构造一个实例
    public ExecutorCompletionService(Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof AbstractExecutorService) ?
            (AbstractExecutorService) executor : null; // 如果执行器派生自AbstractExecutorService, 那么将其同时存储到字段aes中
        this.completionQueue = new LinkedBlockingQueue<Future<V>>(); // 初始化阻塞队列
    }

    /**
     * Creates an ExecutorCompletionService using the supplied
     * executor for base task execution and the supplied queue as its
     * completion queue.
     *
     * @param executor the executor to use
     * @param completionQueue the queue to use as the completion queue
     *        normally one dedicated for use by this service. This
     *        queue is treated as unbounded -- failed attempted
     *        {@code Queue.add} operations for completed tasks cause
     *        them not to be retrievable.
     * @throws NullPointerException if executor or completionQueue are {@code null}
     */ // 根据给定的执行器和给定的阻塞队列构造实例
    public ExecutorCompletionService(Executor executor,
                                     BlockingQueue<Future<V>> completionQueue) {
        if (executor == null || completionQueue == null)
            throw new NullPointerException();
        this.executor = executor;
        this.aes = (executor instanceof AbstractExecutorService) ?
            (AbstractExecutorService) executor : null;
        this.completionQueue = completionQueue;
    }
    /** 见{@link ExecutorCompletionService#submit(Callable)} */
    public Future<V> submit(Callable<V> task) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<V> f = newTaskFor(task);
        executor.execute(new QueueingFuture(f));
        return f;
    }
    /** 见{@link ExecutorCompletionService#submit(Runnable, Object)} */
    public Future<V> submit(Runnable task, V result) {
        if (task == null) throw new NullPointerException();
        RunnableFuture<V> f = newTaskFor(task, result);
        executor.execute(new QueueingFuture(f));
        return f;
    }
    /** 见{@link ExecutorCompletionService#take()} */
    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }
    /** 见{@link ExecutorCompletionService#poll()} */
    public Future<V> poll() {
        return completionQueue.poll();
    }
    /** 见{@link ExecutorCompletionService#poll(long, TimeUnit)} */
    public Future<V> poll(long timeout, TimeUnit unit)
            throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }

}
