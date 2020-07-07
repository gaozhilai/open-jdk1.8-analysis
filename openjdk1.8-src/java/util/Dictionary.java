/*
 * Copyright (c) 1995, 2004, Oracle and/or its affiliates. All rights reserved.
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
 * The <code>Dictionary</code> class is the abstract parent of any
 * class, such as <code>Hashtable</code>, which maps keys to values.
 * Every key and every value is an object. In any one <tt>Dictionary</tt>
 * object, every key is associated with at most one value. Given a
 * <tt>Dictionary</tt> and a key, the associated element can be looked up.
 * Any non-<code>null</code> object can be used as a key and as a value.
 * <p>
 * As a rule, the <code>equals</code> method should be used by
 * implementations of this class to decide if two keys are the same.
 * <p>
 * <strong>NOTE: This class is obsolete.  New implementations should
 * implement the Map interface, rather than extending this class.</strong>
 *
 * @author  unascribed
 * @see     java.util.Map
 * @see     java.lang.Object#equals(java.lang.Object)
 * @see     java.lang.Object#hashCode()
 * @see     java.util.Hashtable
 * @since   JDK1.0
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/7/7 15:32
public abstract /**  字典表抽象类, 已知实现类为{@link Hashtable}, 此抽象类出现在JDK1.0, 没有很好的考虑到集合框架层次, jdk1.2引入了集合框架, 此类被{@link Map}代替 */
class Dictionary<K,V> { // 可以看到jdk1.0时代的确考虑不到太多涉及层面的东西, 此抽象类命名甚至不是Abstract开头的
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */ // 默认构造器, 抽象类构造器默认隐式的只能被子类调用, 此处设置为了public也不影响这一规则, 不过可以看出考虑并不周全, 集合框架时代提供的抽象类比如AbstractSet构造器就是protected的
    public Dictionary() {
    }

    /**
     * Returns the number of entries (distinct keys) in this dictionary.
     *
     * @return  the number of keys in this dictionary.
     */ // 返回字典表实例包含的key-value对数量
    abstract public int size();

    /**
     * Tests if this dictionary maps no keys to value. The general contract
     * for the <tt>isEmpty</tt> method is that the result is true if and only
     * if this dictionary contains no entries.
     *
     * @return  <code>true</code> if this dictionary maps no keys to values;
     *          <code>false</code> otherwise.
     */ // 判断当前实例是否为空
    abstract public boolean isEmpty();

    /**
     * Returns an enumeration of the keys in this dictionary. The general
     * contract for the keys method is that an <tt>Enumeration</tt> object
     * is returned that will generate all the keys for which this dictionary
     * contains entries.
     *
     * @return  an enumeration of the keys in this dictionary.
     * @see     java.util.Dictionary#elements()
     * @see     java.util.Enumeration
     */ /** 返回当前实例包含的Key的枚举, 这里的结果类型也被废弃了, 被{@link Iterator}所代替 */
    abstract public Enumeration<K> keys();

    /**
     * Returns an enumeration of the values in this dictionary. The general
     * contract for the <tt>elements</tt> method is that an
     * <tt>Enumeration</tt> is returned that will generate all the elements
     * contained in entries in this dictionary.
     *
     * @return  an enumeration of the values in this dictionary.
     * @see     java.util.Dictionary#keys()
     * @see     java.util.Enumeration
     */ // 返回当前实例包含的value枚举
    abstract public Enumeration<V> elements();

    /**
     * Returns the value to which the key is mapped in this dictionary.
     * The general contract for the <tt>isEmpty</tt> method is that if this
     * dictionary contains an entry for the specified key, the associated
     * value is returned; otherwise, <tt>null</tt> is returned.
     *
     * @return  the value to which the key is mapped in this dictionary;
     * @param   key   a key in this dictionary.
     *          <code>null</code> if the key is not mapped to any value in
     *          this dictionary.
     * @exception NullPointerException if the <tt>key</tt> is <tt>null</tt>.
     * @see     java.util.Dictionary#put(java.lang.Object, java.lang.Object)
     */ // 根据给定key返回对应的value
    abstract public V get(Object key);

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this dictionary. Neither the key nor the
     * value can be <code>null</code>.
     * <p>
     * If this dictionary already contains an entry for the specified
     * <tt>key</tt>, the value already in this dictionary for that
     * <tt>key</tt> is returned, after modifying the entry to contain the
     *  new element. <p>If this dictionary does not already have an entry
     *  for the specified <tt>key</tt>, an entry is created for the
     *  specified <tt>key</tt> and <tt>value</tt>, and <tt>null</tt> is
     *  returned.
     * <p>
     * The <code>value</code> can be retrieved by calling the
     * <code>get</code> method with a <code>key</code> that is equal to
     * the original <code>key</code>.
     *
     * @param      key     the hashtable key.
     * @param      value   the value.
     * @return     the previous value to which the <code>key</code> was mapped
     *             in this dictionary, or <code>null</code> if the key did not
     *             have a previous mapping.
     * @exception  NullPointerException  if the <code>key</code> or
     *               <code>value</code> is <code>null</code>.
     * @see        java.lang.Object#equals(java.lang.Object)
     * @see        java.util.Dictionary#get(java.lang.Object)
     */ // 将指定的key-value对添加到当前实例
    abstract public V put(K key, V value);

    /**
     * Removes the <code>key</code> (and its corresponding
     * <code>value</code>) from this dictionary. This method does nothing
     * if the <code>key</code> is not in this dictionary.
     *
     * @param   key   the key that needs to be removed.
     * @return  the value to which the <code>key</code> had been mapped in this
     *          dictionary, or <code>null</code> if the key did not have a
     *          mapping.
     * @exception NullPointerException if <tt>key</tt> is <tt>null</tt>.
     */ // 根据给定key, 移除当前实例相应的key-value对
    abstract public V remove(Object key);
}
