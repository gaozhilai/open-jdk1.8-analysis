/*
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.util;

/**
 * This class provides a skeletal implementation of the {@link List}
 * interface to minimize the effort required to implement this interface
 * backed by a "random access" data store (such as an array).  For sequential
 * access data (such as a linked list), {@link AbstractSequentialList} should
 * be used in preference to this class.
 *
 * <p>To implement an unmodifiable list, the programmer needs only to extend
 * this class and provide implementations for the {@link #get(int)} and
 * {@link List#size() size()} methods.
 *
 * <p>To implement a modifiable list, the programmer must additionally
 * override the {@link #set(int, Object) set(int, E)} method (which otherwise
 * throws an {@code UnsupportedOperationException}).  If the list is
 * variable-size the programmer must additionally override the
 * {@link #add(int, Object) add(int, E)} and {@link #remove(int)} methods.
 *
 * <p>The programmer should generally provide a void (no argument) and collection
 * constructor, as per the recommendation in the {@link Collection} interface
 * specification.
 *
 * <p>Unlike the other abstract collection implementations, the programmer does
 * <i>not</i> have to provide an iterator implementation; the iterator and
 * list iterator are implemented by this class, on top of the "random access"
 * methods:
 * {@link #get(int)},
 * {@link #set(int, Object) set(int, E)},
 * {@link #add(int, Object) add(int, E)} and
 * {@link #remove(int)}.
 *
 * <p>The documentation for each non-abstract method in this class describes its
 * implementation in detail.  Each of these methods may be overridden if the
 * collection being implemented admits a more efficient implementation.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @since 1.2
 */
 // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/9 17:36
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> { // AbstractList抽象类为具体的List实现类提供了骨架方法实现, 减轻了实现类的代价
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */ // protected构造器, 只能被子类调用
    protected AbstractList() {
    }

    /**
     * Appends the specified element to the end of this list (optional
     * operation).
     *
     * <p>Lists that support this operation may place limitations on what
     * elements may be added to this list.  In particular, some
     * lists will refuse to add null elements, and others will impose
     * restrictions on the type of elements that may be added.  List
     * classes should clearly specify in their documentation any restrictions
     * on what elements may be added.
     *
     * <p>This implementation calls {@code add(size(), e)}.
     *
     * <p>Note that this implementation throws an
     * {@code UnsupportedOperationException} unless
     * {@link #add(int, Object) add(int, E)} is overridden.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws UnsupportedOperationException if the {@code add} operation
     *         is not supported by this list
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list
     * @throws NullPointerException if the specified element is null and this
     *         list does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *         prevents it from being added to this list
     */ // 在当前序列末尾添加指定元素
    public boolean add(E e) {
        add(size(), e); // 这个add方法是在指定位置放入元素, size()返回了当前已有元素数量, 刚好也是要新增的当前序列最后一个元素的下标
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ // 获取指定下标的元素
    abstract public E get(int index);

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */ // 设置指定下标为指定元素
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */ // 在规定位置index添加给定元素element, 如果index处已经存在元素, 那么将其以及其后面的元素右移
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation always throws an
     * {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */ // 将给定index位置的元素从当前序列中移除, 如果index后方有元素那么将他们左移
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }


    // Search Operations 查找操作

    /**
     * {@inheritDoc}
     *
     * <p>This implementation first gets a list iterator (with
     * {@code listIterator()}).  Then, it iterates over the list until the
     * specified element is found or the end of the list is reached.
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */ // 返回当前序列中给定元素o的位置, 从前往后查找, 如果存在多个返回第一个最小的index, 如果不存在返回-1
    public int indexOf(Object o) {
        ListIterator<E> it = listIterator(); // 获得序列迭代器ListIterator
        if (o==null) {
            while (it.hasNext()) // 遍历每一个元素, 直到找到指定元素并返回
                if (it.next()==null)
                    return it.previousIndex();
        } else {
            while (it.hasNext()) // 遍历元素, 直到找到指定元素并返回
                if (o.equals(it.next()))
                    return it.previousIndex();
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation first gets a list iterator that points to the end
     * of the list (with {@code listIterator(size())}).  Then, it iterates
     * backwards over the list until the specified element is found, or the
     * beginning of the list is reached.
     *
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */ // 返回当前序列中给定元素o的位置, 从后往前查找, 如果存在多个返回最后一个最大的index, 如果不存在返回-1
    public int lastIndexOf(Object o) {
        ListIterator<E> it = listIterator(size()); // 获得当前序列迭代器, 指定迭代器位置在序列尾部
        if (o==null) {
            while (it.hasPrevious()) // 从后往前遍历元素, 直到找到指定元素并返回
                if (it.previous()==null)
                    return it.nextIndex();
        } else {
            while (it.hasPrevious()) // 从后往前遍历元素, 直到找到指定元素并返回
                if (o.equals(it.previous()))
                    return it.nextIndex();
        }
        return -1;
    }


    // Bulk Operations 批量操作

    /**
     * Removes all of the elements from this list (optional operation).
     * The list will be empty after this call returns.
     *
     * <p>This implementation calls {@code removeRange(0, size())}.
     *
     * <p>Note that this implementation throws an
     * {@code UnsupportedOperationException} unless {@code remove(int
     * index)} or {@code removeRange(int fromIndex, int toIndex)} is
     * overridden.
     *
     * @throws UnsupportedOperationException if the {@code clear} operation
     *         is not supported by this list
     */ /** 清空当前集合, 调用{@link #removeRange(int, int)}通过迭代器遍历移除实现 */
    public void clear() {
        removeRange(0, size());
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation gets an iterator over the specified collection
     * and iterates over it, inserting the elements obtained from the
     * iterator into this list at the appropriate position, one at a time,
     * using {@code add(int, E)}.
     * Many implementations will override this method for efficiency.
     *
     * <p>Note that this implementation throws an
     * {@code UnsupportedOperationException} unless
     * {@link #add(int, Object) add(int, E)} is overridden.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @throws IndexOutOfBoundsException     {@inheritDoc}
     */ // 将给定集合的元素依次添加到当前序列指定index处, 添加顺序取决于给定集合返回的迭代器. 如果给定index已经存在元素, 那么将其以及其随后的元素右移
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index); // 检查index合法性, 不能超过当前已有元素下标
        boolean modified = false;
        for (E e : c) { // 遍历要添加的集合的每一个元素
            add(index++, e); // 调用add方法向指定下标添加元素, 并将原有元素整体右移
            modified = true; // 有添加成功将修改标记设为真
        }
        return modified; // 返回修改状态
    }


    // Iterators 迭代器相关

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * <p>This implementation returns a straightforward implementation of the
     * iterator interface, relying on the backing list's {@code size()},
     * {@code get(int)}, and {@code remove(int)} methods.
     *
     * <p>Note that the iterator returned by this method will throw an
     * {@link UnsupportedOperationException} in response to its
     * {@code remove} method unless the list's {@code remove(int)} method is
     * overridden.
     *
     * <p>This implementation can be made to throw runtime exceptions in the
     * face of concurrent modification, as described in the specification
     * for the (protected) {@link #modCount} field.
     *
     * @return an iterator over the elements in this list in proper sequence
     */ /** 返回一个只能向前遍历的迭代器, 这个迭代器直接实现了{@link Iterator}接口 */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns {@code listIterator(0)}.
     *
     * @see #listIterator(int)
     */ // 返回一个ListIterator迭代器, 这个迭代器在普通迭代器基础上还支持往前遍历
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns a straightforward implementation of the
     * {@code ListIterator} interface that extends the implementation of the
     * {@code Iterator} interface returned by the {@code iterator()} method.
     * The {@code ListIterator} implementation relies on the backing list's
     * {@code get(int)}, {@code set(int, E)}, {@code add(int, E)}
     * and {@code remove(int)} methods.
     *
     * <p>Note that the list iterator returned by this implementation will
     * throw an {@link UnsupportedOperationException} in response to its
     * {@code remove}, {@code set} and {@code add} methods unless the
     * list's {@code remove(int)}, {@code set(int, E)}, and
     * {@code add(int, E)} methods are overridden.
     *
     * <p>This implementation can be made to throw runtime exceptions in the
     * face of concurrent modification, as described in the specification for
     * the (protected) {@link #modCount} field.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */ // 返回一个指定了迭代器初始默认位置的ListIterator迭代器
    public ListIterator<E> listIterator(final int index) {
        rangeCheckForAdd(index);

        return new ListItr(index);
    }
    /** 当前序列基本的迭代器, 直接实现了{@link Iterator}接口 */
    private class Itr implements Iterator<E> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursor = 0; // 游标, 标记了下一次调用next方法应该返回的元素的位置

        /**
         * Index of element returned by most recent call to next or
         * previous.  Reset to -1 if this element is deleted by a call
         * to remove.
         */
        int lastRet = -1; // last return, 最近一次调用next或者previous方法返回的元素的下标

        /**
         * The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */ // 当前迭代器认为其基于的序列当前的操作数, 用于校验是否被并发修改, 如果对应迭代器操作不支持并发修改可以抛出异常, 实现快速失败fast-fail
        int expectedModCount = modCount;
        // 判断当前迭代器是否还存在下一个元素
        public boolean hasNext() {
            return cursor != size(); // 当前游标位置如果等于已有元素数量, 那么当前lastRet位置已经在序列尾部, 不存在下一个元素, 返回false
        }
        // 返回当前迭代器下一个元素
        public E next() {
            checkForComodification(); // 检测在当前迭代器迭代过程中是否被并发修改, 如果被修改抛出异常
            try {
                int i = cursor; // 获得当前要返回的元素下标
                E next = get(i);
                lastRet = i;
                cursor = i + 1; // cursor代表下一次访问的元素下标, 本次已经使用过, 将游标自增一
                return next; // 返回找到的元素
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }
        /** 将迭代器当前最后一个返回的元素从底层数组中移除, 此方法只能在每次{@link #next()}方法调用后调用一次 */
        public void remove() {
            if (lastRet < 0) // 后面移除元素后会将lastRet, 也就是最近一次返回元素的下标置为-1, 如果没有调用next重复调用此remove方法就抛出异常
                throw new IllegalStateException();
            checkForComodification(); // 检查被并发操作抛出异常

            try {
                AbstractList.this.remove(lastRet); // 移除上一次返回元素
                if (lastRet < cursor) // 这个remove方法也被ListItr继承使用, 当其调用previous后, cursor == lastRet, 本次remove导致的cursor变动由对应的previous维护
                    cursor--; // cursor代表了下次返回的元素, 上次返回的元素已经移除, 右侧元素整体向左移动一格, 所以cursor也要减一
                lastRet = -1; // 上次返回的元素已经不存在, 将lastRet设置为-1, 标记在不调用next或者ListItr中的previous并且重复调用此方法时需要抛出异常
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
        /** 检查迭代过程中当前序列被并发修改过就抛出异常{@link ConcurrentModificationException} */
        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
    // 序列特有的迭代器, 在基础的迭代器之上额外支持向前遍历
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) { // 构造序列迭代器时支持指定默认元素(第一次调用next方法返回的元素)位置
            cursor = index;
        }
        // 判断当前迭代器是否还有前一个元素
        public boolean hasPrevious() {
            return cursor != 0;
        }
        // 返回当前迭代器前一个元素
        public E previous() {
            checkForComodification(); // 检测如果被并发修改抛出异常
            try {
                int i = cursor - 1; // 获得调用next时本该返回的cursor位置, 并减一得到其前一个元素位置
                E previous = get(i); // 获得要返回的元素
                lastRet = cursor = i; // 将lastRet与cursor都设置为本次返回元素的下标位置
                return previous; // 返回元素
            } catch (IndexOutOfBoundsException e) {
                checkForComodification(); // 如果有越界异常检测是否被并发修改
                throw new NoSuchElementException(); // 没有被并发修改过则抛出无此元素异常
            }
        }
        // 返回下一个元素的下标
        public int nextIndex() {
            return cursor;
        }
        // 返回上一个元素的下标, 即cursor的上一个
        public int previousIndex() {
            return cursor-1;
        }
        // 将上一次返回的元素用指定元素替换
        public void set(E e) {
            if (lastRet < 0) // lastRet小于0可能是因为调用了remove方法, 右侧元素统一左移, 抛出异常, 避免调用者覆盖了不应该覆盖的元素
                throw new IllegalStateException();
            checkForComodification(); // 检查如果被并发修改则抛出异常

            try {
                AbstractList.this.set(lastRet, e); // 将上次返回元素用指定元素覆盖
                expectedModCount = modCount; // 更新操作次数
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
        // 将指定元素插入到下一个要返回的元素位置, 如果这个位置已经存在元素, 将他们统一向右移
        public void add(E e) {
            checkForComodification(); // 检测如果被并发修改抛出异常

            try {
                int i = cursor; // 确定元素插入位置
                AbstractList.this.add(i, e); // 将元素插入cursor即下一次next方法要返回的位置
                lastRet = -1; // 将上一次返回元素位置设为-1, 防止被remove等其他操作继续操作
                cursor = i + 1; // 由于原先以及原先位置右侧的元素统一向右移动一个位置, 所以得到新的cursor位置
                expectedModCount = modCount; // 修改操作数
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns a list that subclasses
     * {@code AbstractList}.  The subclass stores, in private fields, the
     * offset of the subList within the backing list, the size of the subList
     * (which can change over its lifetime), and the expected
     * {@code modCount} value of the backing list.  There are two variants
     * of the subclass, one of which implements {@code RandomAccess}.
     * If this list implements {@code RandomAccess} the returned list will
     * be an instance of the subclass that implements {@code RandomAccess}.
     *
     * <p>The subclass's {@code set(int, E)}, {@code get(int)},
     * {@code add(int, E)}, {@code remove(int)}, {@code addAll(int,
     * Collection)} and {@code removeRange(int, int)} methods all
     * delegate to the corresponding methods on the backing abstract list,
     * after bounds-checking the index and adjusting for the offset.  The
     * {@code addAll(Collection c)} method merely returns {@code addAll(size,
     * c)}.
     *
     * <p>The {@code listIterator(int)} method returns a "wrapper object"
     * over a list iterator on the backing list, which is created with the
     * corresponding method on the backing list.  The {@code iterator} method
     * merely returns {@code listIterator()}, and the {@code size} method
     * merely returns the subclass's {@code size} field.
     *
     * <p>All methods first check to see if the actual {@code modCount} of
     * the backing list is equal to its expected value, and throw a
     * {@code ConcurrentModificationException} if it is not.
     *
     * @throws IndexOutOfBoundsException if an endpoint index value is out of range
     *         {@code (fromIndex < 0 || toIndex > size)}
     * @throws IllegalArgumentException if the endpoint indices are out of order
     *         {@code (fromIndex > toIndex)}
     */ // 返回一个基于原始序列的范围为左闭右开[fromIndex, toIndex)(如果fromIndex==toIndex返回空序列)的子序列, 子序列与原始序列对元素的变动会互相影响
    public List<E> subList(int fromIndex, int toIndex) { // 如果子序列有结构性改变, 那么会抛出异常
        return (this instanceof RandomAccess ? // 如果当前序列实现类支持高效的随机访问
                new RandomAccessSubList<>(this, fromIndex, toIndex) : // 返回支持随机访问的子序列
                new SubList<>(this, fromIndex, toIndex)); // 返回没有随机标记的子序列
    }

    // Comparison and hashing 比较和哈希相关方法

    /**
     * Compares the specified object with this list for equality.  Returns
     * {@code true} if and only if the specified object is also a list, both
     * lists have the same size, and all corresponding pairs of elements in
     * the two lists are <i>equal</i>.  (Two elements {@code e1} and
     * {@code e2} are <i>equal</i> if {@code (e1==null ? e2==null :
     * e1.equals(e2))}.)  In other words, two lists are defined to be
     * equal if they contain the same elements in the same order.<p>
     *
     * This implementation first checks if the specified object is this
     * list. If so, it returns {@code true}; if not, it checks if the
     * specified object is a list. If not, it returns {@code false}; if so,
     * it iterates over both lists, comparing corresponding pairs of elements.
     * If any comparison returns {@code false}, this method returns
     * {@code false}.  If either iterator runs out of elements before the
     * other it returns {@code false} (as the lists are of unequal length);
     * otherwise it returns {@code true} when the iterations complete.
     *
     * @param o the object to be compared for equality with this list
     * @return {@code true} if the specified object is equal to this list
     */ // 判断两个序列是否相等, 如果两个序列包含相同元素并且元素顺序相同视为两个序列相等
    public boolean equals(Object o) {
        if (o == this) // 如果要比较对象就是当前序列, 返回true
            return true;
        if (!(o instanceof List)) // 如果要比较对象都不是序列类型, 那么直接返回false
            return false;

        ListIterator<E> e1 = listIterator(); // 获得当前序列自身的序列迭代器
        ListIterator<?> e2 = ((List<?>) o).listIterator(); // 获取要比较的对象的序列迭代器
        while (e1.hasNext() && e2.hasNext()) { // 遍历两个序列的元素
            E o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : o1.equals(o2))) // 如果某次遍历两个元素不等返回false
                return false;
        }
        return !(e1.hasNext() || e2.hasNext()); // 两个序列包含元素一致, 且顺序相同, 视为相等, 返回true
    }

    /**
     * Returns the hash code value for this list.
     *
     * <p>This implementation uses exactly the code that is used to define the
     * list hash function in the documentation for the {@link List#hashCode}
     * method.
     *
     * @return the hash code value for this list
     */ // 返回当前序列的hashCode, 此值通过当前序列包含的每一个元素的hashCode计算来实现
    public int hashCode() {
        int hashCode = 1;
        for (E e : this)
            hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
        return hashCode;
    }

    /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * <p>This method is called by the {@code clear} operation on this list
     * and its subLists.  Overriding this method to take advantage of
     * the internals of the list implementation can <i>substantially</i>
     * improve the performance of the {@code clear} operation on this list
     * and its subLists.
     *
     * <p>This implementation gets a list iterator positioned before
     * {@code fromIndex}, and repeatedly calls {@code ListIterator.next}
     * followed by {@code ListIterator.remove} until the entire range has
     * been removed.  <b>Note: if {@code ListIterator.remove} requires linear
     * time, this implementation requires quadratic time.</b>
     *
     * @param fromIndex index of first element to be removed
     * @param toIndex index after last element to be removed
     */ // 移除指定下标范围[fromIndex, toIndex)的元素, 通过序列迭代器实现
    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator<E> it = listIterator(fromIndex); // 获得指定默认位置的序列迭代器
        for (int i=0, n=toIndex-fromIndex; i<n; i++) { // 遍历到指定toIndex范围, 将遍历到的元素移除
            it.next();
            it.remove();
        }
    }

    /**
     * The number of times this list has been <i>structurally modified</i>.
     * Structural modifications are those that change the size of the
     * list, or otherwise perturb it in such a fashion that iterations in
     * progress may yield incorrect results.
     *
     * <p>This field is used by the iterator and list iterator implementation
     * returned by the {@code iterator} and {@code listIterator} methods.
     * If the value of this field changes unexpectedly, the iterator (or list
     * iterator) will throw a {@code ConcurrentModificationException} in
     * response to the {@code next}, {@code remove}, {@code previous},
     * {@code set} or {@code add} operations.  This provides
     * <i>fail-fast</i> behavior, rather than non-deterministic behavior in
     * the face of concurrent modification during iteration.
     *
     * <p><b>Use of this field by subclasses is optional.</b> If a subclass
     * wishes to provide fail-fast iterators (and list iterators), then it
     * merely has to increment this field in its {@code add(int, E)} and
     * {@code remove(int)} methods (and any other methods that it overrides
     * that result in structural modifications to the list).  A single call to
     * {@code add(int, E)} or {@code remove(int)} must add no more than
     * one to this field, or the iterators (and list iterators) will throw
     * bogus {@code ConcurrentModificationExceptions}.  If an implementation
     * does not wish to provide fail-fast iterators, this field may be
     * ignored.
     */ // 标记当前序列操作数, 用于检测是否被并发修改
    protected transient int modCount = 0;
    // 检测新增元素时指定下标是否合法
    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size()) // 如果指定下标小于0或大于当前序列最后一个元素下标, 抛出越界异常
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    // 构建越界报错信息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size();
    }
}
// 子序列, 基于真实序列的数据视图
class SubList<E> extends AbstractList<E> {
    private final AbstractList<E> l; // 子序列基于的真实序列
    private final int offset; // 偏移量
    private int size; // 自序列包含的元素数量
    // 构造基于原始序列的范围为左闭右开[fromIndex, toIndex)(如果fromIndex==toIndex返回空序列)的子序列, 子序列与原始序列对元素的变动会互相影响
    SubList(AbstractList<E> list, int fromIndex, int toIndex) { // 子序列构造方法,
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > list.size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
        l = list;
        offset = fromIndex;
        size = toIndex - fromIndex;
        this.modCount = l.modCount;
    }
    // 设置指定下标为指定元素, 真正添加逻辑指向其基于的真实的序列
    public E set(int index, E element) {
        rangeCheck(index);
        checkForComodification();
        return l.set(index+offset, element);
    }
    // 获取指定下标的元素
    public E get(int index) {
        rangeCheck(index);
        checkForComodification();
        return l.get(index+offset);
    }
    // 返回当前集合包含元素数量, 包含数量大于int最大值则返回int最大值
    public int size() {
        checkForComodification();
        return size;
    }
    // 在规定位置index添加给定元素element, 如果index处已经存在元素, 那么将其以及其后面的元素右移
    public void add(int index, E element) {
        rangeCheckForAdd(index);
        checkForComodification();
        l.add(index+offset, element);
        this.modCount = l.modCount;
        size++;
    }
    // 将给定index位置的元素从当前序列中移除, 如果index后方有元素那么将他们左移
    public E remove(int index) {
        rangeCheck(index);
        checkForComodification();
        E result = l.remove(index+offset);
        this.modCount = l.modCount;
        size--;
        return result;
    }
    // 移除指定下标范围[fromIndex, toIndex)的元素, 通过序列迭代器实现
    protected void removeRange(int fromIndex, int toIndex) {
        checkForComodification();
        l.removeRange(fromIndex+offset, toIndex+offset);
        this.modCount = l.modCount;
        size -= (toIndex-fromIndex);
    }
    // 将给定集合的元素依次添加到当前序列尾部, 添加顺序取决于给定集合返回的迭代器
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }
    // 将给定集合的元素依次添加到当前序列指定index处, 添加顺序取决于给定集合返回的迭代器. 如果给定index已经存在元素, 那么将其以及其随后的元素右移
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        int cSize = c.size();
        if (cSize==0)
            return false;

        checkForComodification();
        l.addAll(offset+index, c);
        this.modCount = l.modCount;
        size += cSize;
        return true;
    }
    // 返回当前子序列迭代器
    public Iterator<E> iterator() {
        return listIterator();
    }
    // 返回一个指定了迭代器初始默认位置的ListIterator迭代器
    public ListIterator<E> listIterator(final int index) {
        checkForComodification();
        rangeCheckForAdd(index);

        return new ListIterator<E>() { // 返回当前子序列迭代器实现类的对象
            private final ListIterator<E> i = l.listIterator(index+offset);

            public boolean hasNext() {
                return nextIndex() < size;
            }

            public E next() {
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public E previous() {
                if (hasPrevious())
                    return i.previous();
                else
                    throw new NoSuchElementException();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            public int previousIndex() {
                return i.previousIndex() - offset;
            }

            public void remove() {
                i.remove();
                SubList.this.modCount = l.modCount;
                size--;
            }

            public void set(E e) {
                i.set(e);
            }

            public void add(E e) {
                i.add(e);
                SubList.this.modCount = l.modCount;
                size++;
            }
        };
    }
    // 返回基于子序列的子序列, 基于原始序列的范围为左闭右开[fromIndex, toIndex)(如果fromIndex==toIndex返回空序列)的子序列, 子序列与原始序列对元素的变动会互相影响
    public List<E> subList(int fromIndex, int toIndex) {
        return new SubList<>(this, fromIndex, toIndex);
    }
    // 检测下标是否越界
    private void rangeCheck(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    // 检测添加元素时下标是否越界
    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    // 构建下标越界报错信息
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }
    // 检测被并发修改抛出异常
    private void checkForComodification() {
        if (this.modCount != l.modCount)
            throw new ConcurrentModificationException();
    }
}
// 有随机访问标记的子序列实现类
class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {
    RandomAccessSubList(AbstractList<E> list, int fromIndex, int toIndex) {
        super(list, fromIndex, toIndex);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new RandomAccessSubList<>(this, fromIndex, toIndex);
    }
}
