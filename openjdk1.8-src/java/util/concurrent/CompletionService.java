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
 * A service that decouples the production of new asynchronous tasks
 * from the consumption of the results of completed tasks.  Producers
 * {@code submit} tasks for execution. Consumers {@code take}
 * completed tasks and process their results in the order they
 * complete.  A {@code CompletionService} can for example be used to
 * manage asynchronous I/O, in which tasks that perform reads are
 * submitted in one part of a program or system, and then acted upon
 * in a different part of the program when the reads complete,
 * possibly in a different order than they were requested.
 *
 * <p>Typically, a {@code CompletionService} relies on a separate
 * {@link Executor} to actually execute the tasks, in which case the
 * {@code CompletionService} only manages an internal completion
 * queue. The {@link ExecutorCompletionService} class provides an
 * implementation of this approach.
 *
 * <p>Memory consistency effects: Actions in a thread prior to
 * submitting a task to a {@code CompletionService}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * actions taken by that task, which in turn <i>happen-before</i>
 * actions following a successful return from the corresponding {@code take()}.
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/9/16 16:20
public interface CompletionService<V> { // 此方法主要是将任务的执行与产生的结果解耦, 相当于一个阻塞队列, 队列里装的就是任务执行完毕后产生的结果
    /**
     * Submits a value-returning task for execution and returns a Future
     * representing the pending results of the task.  Upon completion,
     * this task may be taken or polled.
     *
     * @param task the task to submit
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */ // 提交执行一个任务, 得到一个异步结果
    Future<V> submit(Callable<V> task);

    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task.  Upon completion, this task may be
     * taken or polled.
     *
     * @param task the task to submit
     * @param result the result to return upon successful completion
     * @return a Future representing pending completion of the task,
     *         and whose {@code get()} method will return the given
     *         result value upon completion
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */ // 提交执行一个任务, 得到一个异步结果, 异步结果中包含的是调用时给定的对象
    Future<V> submit(Runnable task, V result);

    /**
     * Retrieves and removes the Future representing the next
     * completed task, waiting if none are yet present.
     *
     * @return the Future representing the next completed task
     * @throws InterruptedException if interrupted while waiting
     */ // 如果存在执行完毕的任务, 那么获取一个对应的结果, 如果不存在, 就阻塞等待有任务执行完毕
    Future<V> take() throws InterruptedException;

    /**
     * Retrieves and removes the Future representing the next
     * completed task, or {@code null} if none are present.
     *
     * @return the Future representing the next completed task, or
     *         {@code null} if none are present
     */ // 获得任务完成的结果, 如果不存在直接返回null
    Future<V> poll();

    /**
     * Retrieves and removes the Future representing the next
     * completed task, waiting if necessary up to the specified wait
     * time if none are yet present.
     *
     * @param timeout how long to wait before giving up, in units of
     *        {@code unit}
     * @param unit a {@code TimeUnit} determining how to interpret the
     *        {@code timeout} parameter
     * @return the Future representing the next completed task or
     *         {@code null} if the specified waiting time elapses
     *         before one is present
     * @throws InterruptedException if interrupted while waiting
     */ // 获得任务完成的结果, 不存在则等待一段时间, 如果等待指定时间后, 还是不存在直接返回null
    Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
}
