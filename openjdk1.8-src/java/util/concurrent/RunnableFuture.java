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
 * A {@link Future} that is {@link Runnable}. Successful execution of
 * the {@code run} method causes completion of the {@code Future}
 * and allows access to its results.
 * @see FutureTask
 * @see Executor
 * @since 1.6
 * @author Doug Lea
 * @param <V> The result type returned by this Future's {@code get} method
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/9/15 14:08
public interface RunnableFuture<V> extends Runnable, Future<V> { // RunnableFuture具有可执行的能力, 也可以代表一个异步的执行结果, 也就是说本身包含任务信息, 也包含任务执行完毕后的结果信息
    /**
     * Sets this Future to the result of its computation
     * unless it has been cancelled.
     */ // 此处要被线程执行的任务是将结果放入到Future中
    void run();
}
