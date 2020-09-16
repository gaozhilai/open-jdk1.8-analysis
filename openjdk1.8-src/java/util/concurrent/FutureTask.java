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
import java.util.concurrent.locks.LockSupport;

/**
 * A cancellable asynchronous computation.  This class provides a base
 * implementation of {@link Future}, with methods to start and cancel
 * a computation, query to see if the computation is complete, and
 * retrieve the result of the computation.  The result can only be
 * retrieved when the computation has completed; the {@code get}
 * methods will block if the computation has not yet completed.  Once
 * the computation has completed, the computation cannot be restarted
 * or cancelled (unless the computation is invoked using
 * {@link #runAndReset}).
 *
 * <p>A {@code FutureTask} can be used to wrap a {@link Callable} or
 * {@link Runnable} object.  Because {@code FutureTask} implements
 * {@code Runnable}, a {@code FutureTask} can be submitted to an
 * {@link Executor} for execution.
 *
 * <p>In addition to serving as a standalone class, this class provides
 * {@code protected} functionality that may be useful when creating
 * customized task classes.
 *
 * @since 1.5
 * @author Doug Lea
 * @param <V> The result type returned by this FutureTask's {@code get} methods
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/9/15 14:17
public class FutureTask<V> implements RunnableFuture<V> { // FutureTask是RunnableFuture的实现类, 包含异步任务信息, 也包含异步执行结果信息. 是将Callable适配成Runnable的适配器
    /*
     * Revision notes: This differs from previous versions of this
     * class that relied on AbstractQueuedSynchronizer, mainly to
     * avoid surprising users about retaining interrupt status during
     * cancellation races. Sync control in the current design relies
     * on a "state" field updated via CAS to track completion, along
     * with a simple Treiber stack to hold waiting threads.
     *
     * Style note: As usual, we bypass overhead of using
     * AtomicXFieldUpdaters and instead directly use Unsafe intrinsics.
     */

    /**
     * The run state of this task, initially NEW.  The run state
     * transitions to a terminal state only in methods set,
     * setException, and cancel.  During completion, state may take on
     * transient values of COMPLETING (while outcome is being set) or
     * INTERRUPTING (only while interrupting the runner to satisfy a
     * cancel(true)). Transitions from these intermediate to final
     * states use cheaper ordered/lazy writes because values are unique
     * and cannot be further modified.
     *
     * Possible state transitions:
     * NEW -> COMPLETING -> NORMAL
     * NEW -> COMPLETING -> EXCEPTIONAL
     * NEW -> CANCELLED
     * NEW -> INTERRUPTING -> INTERRUPTED
     */
    private volatile int state; // 当前任务的状态
    private static final int NEW          = 0; // 新任务初始状态, 注意这里的初始状态包含了执行中
    private static final int COMPLETING   = 1; // 任务执行完毕, 但是正在设置结果或者异常
    private static final int NORMAL       = 2; // 任务以普通方式执行完毕
    private static final int EXCEPTIONAL  = 3; // 任务以异常方式执行完毕
    private static final int CANCELLED    = 4; // 任务被取消
    private static final int INTERRUPTING = 5; // 任务正在被中断
    private static final int INTERRUPTED  = 6; // 任务已经被中断

    /** The underlying callable; nulled out after running */ // 具体要执行的Callable任务, 执行完毕后此字段置空
    private Callable<V> callable;
    /** The result to return or exception to throw from get() */ // 调用get方法返回的任务结果, 或者要抛出的异常
    private Object outcome; // non-volatile, protected by state reads/writes
    /** The thread running the callable; CASed during run() */ // 执行任务的线程
    private volatile Thread runner;
    /** Treiber stack of waiting threads */ // 线程等待栈, Treiber stack是一个叫Treiber提出的无锁并发栈, 无锁特性采用CAS操作保证
    private volatile WaitNode waiters;

    /**
     * Returns result or throws exception for completed task.
     *
     * @param s completed state value
     */ // 根据状态字段判断是返回结果还是抛出异常
    @SuppressWarnings("unchecked")
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        if (s == NORMAL) // 如果普通的执行完毕
            return (V)x; // 返回结果
        if (s >= CANCELLED) // 如果任务状态属于被取消类型(包括了中断状态)
            throw new CancellationException(); // 抛出取消异常
        throw new ExecutionException((Throwable)x); // 否则抛出执行异常, cause设置为outcome字段具体异常内容
    }

    /**
     * Creates a {@code FutureTask} that will, upon running, execute the
     * given {@code Callable}.
     *
     * @param  callable the callable task
     * @throws NullPointerException if the callable is null
     */ // 适配器构造函数接收Callable, 包装成FutureTask(主要意义在于将Callable转换成了Runnable)
    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable; // 保存任务内容
        this.state = NEW;       // ensure visibility of callable 初始化任务状态为NEW
    }

    /**
     * Creates a {@code FutureTask} that will, upon running, execute the
     * given {@code Runnable}, and arrange that {@code get} will return the
     * given result on successful completion.
     *
     * @param runnable the runnable task
     * @param result the result to return on successful completion. If
     * you don't need a particular result, consider using
     * constructions of the form:
     * {@code Future<?> f = new FutureTask<Void>(runnable, null)}
     * @throws NullPointerException if the runnable is null
     */ // 根据给定的Runnable构造FutureTask
    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result); // 将Runnable转换成Callable并且保存任务
        this.state = NEW;       // ensure visibility of callable 初始化任务状态为NEW
    }
    // 判断当前任务是否已经被取消
    public boolean isCancelled() {
        return state >= CANCELLED;
    }
    // 判断当前任务是否已经完毕
    public boolean isDone() {
        return state != NEW;
    }
    /** 见{@link Future#cancel(boolean)} */
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!(state == NEW && // 如果当前任务不是新建状态(包含了执行中), 那么已经无法被取消
              UNSAFE.compareAndSwapInt(this, stateOffset, NEW,
                  mayInterruptIfRunning ? INTERRUPTING : CANCELLED))) // 预期是NEW状态, CAS操作才会成功, 如果调用参数是允许中断的, 将状态更改为INTERRUPTING, 否则更改为CANCELLED
            return false; // 返回false, 取消失败
        try {    // in case call to interrupt throws exception
            if (mayInterruptIfRunning) { // 如果取消方法是允许中断正在执行线程的
                try {
                    Thread t = runner;
                    if (t != null)
                        t.interrupt(); // 对线程执行中断
                } finally { // final state
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED); // 然后将状态改为INTERRUPTED
                }
            }
        } finally {
            finishCompletion(); // 执行后置操作
        }
        return true; // 返回true取消成功
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */ /** 见{@link Future#get()} */
    public V get() throws InterruptedException, ExecutionException {
        int s = state; // 暂存当前任务状态
        if (s <= COMPLETING) // 如果当前任务状态还没有完成, 也就是执行中
            s = awaitDone(false, 0L); // 那么阻塞等待执行完毕
        return report(s); // 根据执行完毕反馈的状态决定是返回结果还是抛出异常
    }

    /**
     * @throws CancellationException {@inheritDoc}
     */ /** 见{@link Future#get(long, TimeUnit)} */
    public V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null)
            throw new NullPointerException();
        int s = state;
        if (s <= COMPLETING &&
            (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING) // 指定超时时间的等待执行方法返回状态还是执行中, 代表超时还未执行完毕
            throw new TimeoutException(); // 抛出超时异常
        return report(s);
    }

    /**
     * Protected method invoked when this task transitions to state
     * {@code isDone} (whether normally or via cancellation). The
     * default implementation does nothing.  Subclasses may override
     * this method to invoke completion callbacks or perform
     * bookkeeping. Note that you can query status inside the
     * implementation of this method to determine whether this task
     * has been cancelled.
     */ // 当前任务转换为完毕状态时的回调方法, 供子类自定义实现. 需要注意的是此方法内部有能力查询状态字段的值获知任务当前状态
    protected void done() { }

    /**
     * Sets the result of this future to the given value unless
     * this future has already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon successful completion of the computation.
     *
     * @param v the value
     */ // 将当前结果设置到outcome字段
    protected void set(V v) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) { // 尝试将执行中任务设置为COMPLETING
            outcome = v; // 将给定结果设置到outcome字段
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state 设置最终状态
            finishCompletion(); // 执行后置处理
        }
    }

    /**
     * Causes this future to report an {@link ExecutionException}
     * with the given throwable as its cause, unless this future has
     * already been set or has been cancelled.
     *
     * <p>This method is invoked internally by the {@link #run} method
     * upon failure of the computation.
     *
     * @param t the cause of failure
     */ // 将给定异常设置到outcome字段
    protected void setException(Throwable t) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) { // 尝试将执行中任务设置为COMPLETING
            outcome = t; // 将给定的异常设置到outcome字段
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state 设置当前任务最终状态为异常执行完毕
            finishCompletion(); // 执行后置处理
        }
    }
    // 实际执行任务逻辑
    public void run() {
        if (state != NEW || // 如果当前任务状态不是新建状态
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread())) // 或者设置runner为当前线程失败(并发放调用了run方法)
            return; // 那么直接return, 不执行任何逻辑
        try {
            Callable<V> c = callable; // 暂存任务内容
            if (c != null && state == NEW) { // 任务需要有具体内容, 并且是新建状态
                V result;
                boolean ran; // Doug Lea 老师这块变量命名有点随意了
                try {
                    result = c.call(); // 执行实际任务
                    ran = true; // 执行结果设置为true
                } catch (Throwable ex) {
                    result = null;
                    ran = false; // 执行结果设置为true
                    setException(ex); // 如果抛出异常了设置异常
                }
                if (ran) // 如果执行成功
                    set(result); // 那么将执行结果设置到outcome字段
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null; // 清空当前runner
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s = state;
            if (s >= INTERRUPTING) // 如果任务被中断了
                handlePossibleCancellationInterrupt(s); // 让出CPU, 让其他线程执行完中断逻辑
        }
    }

    /**
     * Executes the computation without setting its result, and then
     * resets this future to initial state, failing to do so if the
     * computation encounters an exception or is cancelled.  This is
     * designed for use with tasks that intrinsically execute more
     * than once.
     *
     * @return {@code true} if successfully run and reset
     */ /** 与{@link #run()}不同的是, 此方法不会改变任务状态, 任务一直是新建状态, 用于需要重复执行的情况, 例如{@link ScheduledThreadPoolExecutor} */
    protected boolean runAndReset() {
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return false;
        boolean ran = false;
        int s = state;
        try {
            Callable<V> c = callable;
            if (c != null && s == NEW) {
                try {
                    c.call(); // don't set result
                    ran = true;
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran && s == NEW; // 如果状态没有被改变, 并且成功执行, 那么才算是此方法执行成功
    }

    /**
     * Ensures that any interrupt from a possible cancel(true) is only
     * delivered to a task while in run or runAndReset.
     */ // 当前runner线程让步cpu给发起中断操作的线程更改当前任务状态, 由INTERRUPTING更改到INTERRUPTED, 完成中断
    private void handlePossibleCancellationInterrupt(int s) {
        // It is possible for our interrupter to stall before getting a
        // chance to interrupt us.  Let's spin-wait patiently.
        if (s == INTERRUPTING)
            while (state == INTERRUPTING)
                Thread.yield(); // wait out pending interrupt

        // assert state == INTERRUPTED;

        // We want to clear any interrupt we may have received from
        // cancel(true).  However, it is permissible to use interrupts
        // as an independent mechanism for a task to communicate with
        // its caller, and there is no way to clear only the
        // cancellation interrupt.
        //
        // Thread.interrupted();
    }

    /**
     * Simple linked list nodes to record waiting threads in a Treiber
     * stack.  See other classes such as Phaser and SynchronousQueue
     * for more detailed explanation.
     */ // 等待线程节点, 当前线程执行完后, 唤醒等待节点
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;
        WaitNode() { thread = Thread.currentThread(); }
    }

    /**
     * Removes and signals all waiting threads, invokes done(), and
     * nulls out callable.
     */ // 后置操作, 唤醒等待节点, 调用回调方法done(), 清空任务信息
    private void finishCompletion() {
        // assert state > COMPLETING;
        for (WaitNode q; (q = waiters) != null;) {
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
                for (;;) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t); // 唤醒后方所有线程
                    }
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }

        done();

        callable = null;        // to reduce footprint
    }

    /**
     * Awaits completion or aborts on interrupt or timeout.
     *
     * @param timed true if use timed waits
     * @param nanos time to wait, if timed
     * @return state upon completion
     */ // 等待任务完成后返回任务状态, 超过给定时间直接返回当前任务状态
    private int awaitDone(boolean timed, long nanos)
        throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L; // 计算预期超时时间
        WaitNode q = null;
        boolean queued = false; // 当前发起awaitDone的线程是否进入等待队列
        for (;;) { // 自旋直到超时, 任务完毕, 中断情况之一发生
            if (Thread.interrupted()) { // 当前发起等待任务的线程是否被中断
                removeWaiter(q); // 如果被中断, 移除等待执行结果的线程
                throw new InterruptedException(); // 抛出中断异常
            }

            int s = state; // 暂存当前任务状态
            if (s > COMPLETING) { // 如果任务完毕了(包含了其他异常完毕状态)
                if (q != null)
                    q.thread = null; // 将当前等待节点线程设置为null
                return s; // 返回当前任务状态
            }
            else if (s == COMPLETING) // cannot time out yet 如果当前任务已经执行完成正在设置结果阶段, 那么再等一等再返回, 等等再判断超时
                Thread.yield(); // 当前线程等一等, 把cpu让给其他线程
            else if (q == null) // 如果当前操作没有设置等待节点
                q = new WaitNode(); // 那么用当前线程构造一个等待节点
            else if (!queued) // 如果等待节点没有入等待队列
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                     q.next = waiters, q); // 那么将新建的等待节点入队
            else if (timed) { // 如果当前方法调用方指定了超时时间
                nanos = deadline - System.nanoTime(); // 检测还有多久超时
                if (nanos <= 0L) { // 如果预期时间比当前时间小, 那么已经超时了
                    removeWaiter(q); // 移除当前等待节点, 不再继续等待
                    return state; // 超时直接返回当前任务状态
                }
                LockSupport.parkNanos(this, nanos); // 如果没有超时, 当前操作线程直接等待剩余的时间
            }
            else // 如果没有指定超时时间, 也就是无限等待任务完毕
                LockSupport.park(this); // 直接阻塞当前线程, 等待任务执行完毕之后, 执行方法会唤醒后续等待线程, 也就是当前等待操作线程
        }
    }

    /**
     * Tries to unlink a timed-out or interrupted wait node to avoid
     * accumulating garbage.  Internal nodes are simply unspliced
     * without CAS since it is harmless if they are traversed anyway
     * by releasers.  To avoid effects of unsplicing from already
     * removed nodes, the list is retraversed in case of an apparent
     * race.  This is slow when there are a lot of nodes, but we don't
     * expect lists to be long enough to outweigh higher-overhead
     * schemes.
     */ // 无锁的方式移除给定等待节点, 需要考虑并发情况. 此方法本质是一个并发安全的移除栈节点操作
    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null; // 把要删除的节点包含的线程设置为空, 作为删除标记, 下方循环遍历找到节点并移除
            retry:
            for (;;) {          // restart on removeWaiter race
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) { // 初始条件q等于栈顶, q在每次循环后会变成q.next, 只要q不为空就会循环
                    s = q.next; // 暂存当前节点下一个节点
                    if (q.thread != null) // 如果当前节点包含的线程不为空
                        pred = q; // 那当前节点暂存到前驱结点变量, 继续下轮循环
                    else if (pred != null) { // 如果前驱结点不是空, 也就是移除的不是栈顶
                        pred.next = s; // 删除当前q节点
                        if (pred.thread == null) // check for race 如果pred包含线程也是空, 证明也要删除
                            continue retry; // 继续尝试删除过程
                    }
                    else if (!UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                          q, s)) // 当前要删除节点是栈顶, 并且节点包含的线程是空, 尝试将下一个节点设置成栈顶
                        continue retry; // 设置失败的话重新尝试移除栈顶
                }
                break; // 遍历了栈所有节点后, 方法执行完毕, 这里栈节点不会很多, 不会有性能问题
            }
        }
    }

    // Unsafe mechanics CAS操作相关变量初始化
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            stateOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("state"));
            runnerOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("runner"));
            waitersOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
