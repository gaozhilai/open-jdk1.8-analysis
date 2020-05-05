/*
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * This class implements a vector of bits that grows as needed. Each
 * component of the bit set has a {@code boolean} value. The
 * bits of a {@code BitSet} are indexed by nonnegative integers.
 * Individual indexed bits can be examined, set, or cleared. One
 * {@code BitSet} may be used to modify the contents of another
 * {@code BitSet} through logical AND, logical inclusive OR, and
 * logical exclusive OR operations.
 *
 * <p>By default, all bits in the set initially have the value
 * {@code false}.
 *
 * <p>Every bit set has a current size, which is the number of bits
 * of space currently in use by the bit set. Note that the size is
 * related to the implementation of a bit set, so it may change with
 * implementation. The length of a bit set relates to logical length
 * of a bit set and is defined independently of implementation.
 *
 * <p>Unless otherwise noted, passing a null parameter to any of the
 * methods in a {@code BitSet} will result in a
 * {@code NullPointerException}.
 *
 * <p>A {@code BitSet} is not safe for multithreaded use without
 * external synchronization.
 *
 * @author  Arthur van Hoff
 * @author  Michael McCloskey
 * @author  Martin Buchholz
 * @since   JDK1.0
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/5/4 11:04
public class BitSet implements Cloneable, java.io.Serializable { // BitSet是比特位的集合, 每个比特位有真假两种值, 可以用很少的空间记录位信息
    /*
     * BitSets are packed into arrays of "words."  Currently a word is
     * a long, which consists of 64 bits, requiring 6 address bits.
     * The choice of word size is determined purely by performance concerns.
     */
    private final static int ADDRESS_BITS_PER_WORD = 6; // 当前'word'由long实现, 每个'word'需要6个bit位来描述地址
    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD; // 即每个'word'能记录2^6=64个比特位(long是8字节)
    private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1; // 下标掩码, 即00011111

    /* Used to shift left or right for a partial word mask 即64位每位都为1, 11111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111 */
    private static final long WORD_MASK = 0xffffffffffffffffL;

    /**
     * @serialField bits long[]
     *
     * The bits in this BitSet.  The ith bit is stored in bits[i/64] at
     * bit position i % 64 (where bit position 0 refers to the least
     * significant bit and 63 refers to the most significant bit).
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("bits", long[].class),
    };

    /**
     * The internal field corresponding to the serialField "bits".
     */ // BitSet中包含的比特位由long数组实现
    private long[] words;

    /**
     * The number of words in the logical size of this BitSet.
     */ // 已经使用的'word'数量(数组中已经使用的word中, 最后一个word包含的比特位有被设置true值)
    private transient int wordsInUse = 0;

    /**
     * Whether the size of "words" is user-specified.  If so, we assume
     * the user knows what he's doing and try harder to preserve it.
     */ // 标记初始比特位数量是否是用户指定的
    private transient boolean sizeIsSticky = false;

    /* use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = 7997698588986878753L;

    /**
     * Given a bit index, return word index containing it.
     */ // 根据给定的比特位下标得出所在的'word'下标
    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;  // 比特位下标右移6位, 即相当于除以2^6, 得到比特位所在'word'下标
    }

    /**
     * Every public method must preserve these invariants.
     */ // 校验已使用的word计数wordsInUse值是否正确, 是否符合当前word数组情况
    private void checkInvariants() {
        assert(wordsInUse == 0 || words[wordsInUse - 1] != 0); // 已经使用的'word'为0个, 或者根据wordsInUse计数找到的数组目前最后一个被使用的word值不为0, 即包含的比特位存在true
        assert(wordsInUse >= 0 && wordsInUse <= words.length); // 已经使用的比特位大于等于0个并且wordsInUse计数小于word数组长度
        assert(wordsInUse == words.length || words[wordsInUse] == 0); // wordInUse计数等于数组长度, 或者根据wordsInUse找到的第一个还没被使用的'word'值为0, 即包含的比特位没有被赋值true
    }

    /**
     * Sets the field wordsInUse to the logical size in words of the bit set.
     * WARNING:This method assumes that the number of words actually in use is
     * less than or equal to the current value of wordsInUse!
     */ // 重新计算已使用的word数量, wordsInUse计数
    private void recalculateWordsInUse() {
        // Traverse the bitset until a used word is found
        int i;
        for (i = wordsInUse-1; i >= 0; i--) // 倒序遍历word数组
            if (words[i] != 0) // 找到数组中从前到后最后一个被使用的word
                break;

        wordsInUse = i+1; // The new logical size 其对应的下标加一得到当前已经使用的word计数
    }

    /**
     * Creates a new bit set. All bits are initially {@code false}.
     */ // 初始化一个默认64个比特位的BitSet
    public BitSet() {
        initWords(BITS_PER_WORD); // 初始化64个比特位的long数组, 即new long[1]
        sizeIsSticky = false; // 标记当前比特位数量值非用户指定
    }

    /**
     * Creates a bit set whose initial size is large enough to explicitly
     * represent bits with indices in the range {@code 0} through
     * {@code nbits-1}. All bits are initially {@code false}.
     *
     * @param  nbits the initial size of the bit set
     * @throws NegativeArraySizeException if the specified initial size
     *         is negative
     */ // 根据指定的比特位数量创建BitSet示实例, nbits不能为负值, 0是合法的
    public BitSet(int nbits) {
        // nbits can't be negative; size 0 is OK
        if (nbits < 0)
            throw new NegativeArraySizeException("nbits < 0: " + nbits);

        initWords(nbits); // 初始化最小能装下指定个比特位的long数组
        sizeIsSticky = true; // 标记比特位数量是用户指定的
    }
    // 根据给定的比特位数量初始化一个最小能装下给定个比特位的long数组
    private void initWords(int nbits) {
        words = new long[wordIndex(nbits-1) + 1]; // wordIndex方法计算出指定的比特位中, 最大一个比特位所在word的下标, 其值加一得到数组的长度
    }

    /**
     * Creates a bit set using words as the internal representation.
     * The last word (if there is one) must be non-zero.
     */ // 内部工具方法, 根据给定的long数组初始化BitSet, 这里的long数组中最后一个元素必须非0
    private BitSet(long[] words) {
        this.words = words; // 为word数组赋值
        this.wordsInUse = words.length; // 将wordsInUse已使用的word计数值置为给给定的数组长度
        checkInvariants(); // 检测计数值与word数组实际情况是否一致
    }

    /**
     * Returns a new bit set containing all the bits in the given long array.
     *
     * <p>More precisely,
     * <br>{@code BitSet.valueOf(longs).get(n) == ((longs[n/64] & (1L<<(n%64))) != 0)}
     * <br>for all {@code n < 64 * longs.length}.
     *
     * <p>This method is equivalent to
     * {@code BitSet.valueOf(LongBuffer.wrap(longs))}.
     *
     * @param longs a long array containing a little-endian representation
     *        of a sequence of bits to be used as the initial bits of the
     *        new bit set
     * @return a {@code BitSet} containing all the bits in the long array
     * @since 1.7
     */ // 根据给定的long数组的值构建相应的BitSet实例
    public static BitSet valueOf(long[] longs) {
        int n;
        for (n = longs.length; n > 0 && longs[n - 1] == 0; n--) // 计算出数组中最后一个不为0的元素, 即这个元素以及前方元素包含的比特位可能被赋值
            ; // 空语句, 此循环只是为了找出数组中最后一个不为0的元素, 即找到了有意义的数组元素个数
        return new BitSet(Arrays.copyOf(longs, n)); // 将有意义的数组元素复制出单独的一个数组, 并用其构造BitSet实例返回
    }

    /**
     * Returns a new bit set containing all the bits in the given long
     * buffer between its position and limit.
     *
     * <p>More precisely,
     * <br>{@code BitSet.valueOf(lb).get(n) == ((lb.get(lb.position()+n/64) & (1L<<(n%64))) != 0)}
     * <br>for all {@code n < 64 * lb.remaining()}.
     *
     * <p>The long buffer is not modified by this method, and no
     * reference to the buffer is retained by the bit set.
     *
     * @param lb a long buffer containing a little-endian representation
     *        of a sequence of bits between its position and limit, to be
     *        used as the initial bits of the new bit set
     * @return a {@code BitSet} containing all the bits in the buffer in the
     *         specified range
     * @since 1.7
     */ // 构建一个BitSet实例, 其中word数组数据来源于nio中的LongBuffer
    public static BitSet valueOf(LongBuffer lb) {
        lb = lb.slice(); // 得到LongBuffer当前position之后的子LongBuffer
        int n;
        for (n = lb.remaining(); n > 0 && lb.get(n - 1) == 0; n--) // 计算出数组中最后一个不为0的元素, 即这个元素以及前方元素包含的比特位可能被赋值
            ;
        long[] words = new long[n]; // 创建一个新的long数组
        lb.get(words); // 将LongBuffer中元素装入long数组
        return new BitSet(words); // 返回用其构建的BitSet实例
    }

    /**
     * Returns a new bit set containing all the bits in the given byte array.
     *
     * <p>More precisely,
     * <br>{@code BitSet.valueOf(bytes).get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)}
     * <br>for all {@code n <  8 * bytes.length}.
     *
     * <p>This method is equivalent to
     * {@code BitSet.valueOf(ByteBuffer.wrap(bytes))}.
     *
     * @param bytes a byte array containing a little-endian
     *        representation of a sequence of bits to be used as the
     *        initial bits of the new bit set
     * @return a {@code BitSet} containing all the bits in the byte array
     * @since 1.7
     */ // 用给定的字节数组构建一个BitSet实例
    public static BitSet valueOf(byte[] bytes) {
        return BitSet.valueOf(ByteBuffer.wrap(bytes));
    }

    /**
     * Returns a new bit set containing all the bits in the given byte
     * buffer between its position and limit.
     *
     * <p>More precisely,
     * <br>{@code BitSet.valueOf(bb).get(n) == ((bb.get(bb.position()+n/8) & (1<<(n%8))) != 0)}
     * <br>for all {@code n < 8 * bb.remaining()}.
     *
     * <p>The byte buffer is not modified by this method, and no
     * reference to the buffer is retained by the bit set.
     *
     * @param bb a byte buffer containing a little-endian representation
     *        of a sequence of bits between its position and limit, to be
     *        used as the initial bits of the new bit set
     * @return a {@code BitSet} containing all the bits in the buffer in the
     *         specified range
     * @since 1.7
     */ // 根据给定的ByteBuffer构造BitSet实例
    public static BitSet valueOf(ByteBuffer bb) {
        bb = bb.slice().order(ByteOrder.LITTLE_ENDIAN); // 根据当前值字节缓存位置获得新的字节缓存并升序排序
        int n;
        for (n = bb.remaining(); n > 0 && bb.get(n - 1) == 0; n--)// 计算出数组中最后一个不为0的元素, 即这个元素以及前方元素包含的比特位可能被赋值
            ;
        long[] words = new long[(n + 7) / 8]; // 为了防止整除后数组元素装不下比特位, 需要加七, 得到字节数, 字节数除以八得到需要多少个long元素
        bb.limit(n);
        int i = 0;
        while (bb.remaining() >= 8) // 如果字节缓存中剩余字节大于等于8
            words[i++] = bb.getLong(); // 直接获取一个long内容出来
        for (int remaining = bb.remaining(), j = 0; j < remaining; j++) // 处理ByteBuffer中剩下的不足八个的字节元素
            words[i] |= (bb.get() & 0xffL) << (8 * j); // 将获取到的元素与上long类型掩码0xffL, 低八位是字节值, 高五十六位置为0, 左移操作让每个字节放到long中正确的比特位, 表达式中的或操作将处理完成的比特值赋给word数组中的long元素
        return new BitSet(words); // 用赋值完成的word数组构造BitSet实例返回
    }

    /**
     * Returns a new byte array containing all the bits in this bit set.
     *
     * <p>More precisely, if
     * <br>{@code byte[] bytes = s.toByteArray();}
     * <br>then {@code bytes.length == (s.length()+7)/8} and
     * <br>{@code s.get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)}
     * <br>for all {@code n < 8 * bytes.length}.
     *
     * @return a byte array containing a little-endian representation
     *         of all the bits in this bit set
     * @since 1.7
    */ // 返回一个包含了当前实例所有比特位信息的字节数组
    public byte[] toByteArray() {
        int n = wordsInUse; // 获得当前word数组使用元素数量
        if (n == 0) // 如果使用了0个元素
            return new byte[0]; // 直接返回长度为0的空字节数组
        int len = 8 * (n-1); // word数组中有被使用记录比特位信息的元素时, 先算出第n-1个word数组元素相当于几个字节信息
        for (long x = words[n - 1]; x != 0; x >>>= 8) // 处理word数组中第n个元素包含的比特位信息, 每次右移8位, 如果还不等于0, 证明还有比特位信息需要处理, 每次移位后字节计数加一
            len++;
        byte[] bytes = new byte[len]; // 通过最终计算出的字节数量, 初始化一个指定长度的字节数组
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN); // 将字节数组放入字节缓存, 并设置升序
        for (int i = 0; i < n - 1; i++) // 将word数组中前n-1个已使用的元素放入字节缓存
            bb.putLong(words[i]);
        for (long x = words[n - 1]; x != 0; x >>>= 8) // word数组元素中第n个元素, 只将其中有有效比特位信息的, 以字节为单位放入直接花奴才能
            bb.put((byte) (x & 0xff));
        return bytes; // 返回持有当前BitSet实例所有有效比特位信息的字节数组
    }

    /**
     * Returns a new long array containing all the bits in this bit set.
     *
     * <p>More precisely, if
     * <br>{@code long[] longs = s.toLongArray();}
     * <br>then {@code longs.length == (s.length()+63)/64} and
     * <br>{@code s.get(n) == ((longs[n/64] & (1L<<(n%64))) != 0)}
     * <br>for all {@code n < 64 * longs.length}.
     *
     * @return a long array containing a little-endian representation
     *         of all the bits in this bit set
     * @since 1.7
    */ // 返回一个包含了当前BitSet实例所有有效比特位信息的long类型数组
    public long[] toLongArray() {
        return Arrays.copyOf(words, wordsInUse); // 复制当前实例word数组中已经使用的元素到新数组, 并返回
    }

    /**
     * Ensures that the BitSet can hold enough words.
     * @param wordsRequired the minimum acceptable number of words.
     */ // 确保当前实例中的word数组可以装入所需要的比特位信息, 如果不够会在扩容到当前word数组长度2倍, 如果二倍也不能满足要求, 直接扩容到参数指定的所需word长度
    private void ensureCapacity(int wordsRequired) {
        if (words.length < wordsRequired) { // 如果当前word数组小于需要的word数量, 下方逻辑进行扩容
            // Allocate larger of doubled size or required size
            int request = Math.max(2 * words.length, wordsRequired); // 扩容到当前word数组长度2倍, 如果二倍也不能满足要求, 直接扩容到参数指定的所需word长度
            words = Arrays.copyOf(words, request); // 将旧的word数组内容复制到新的长度的数组, 并将新数组设置为word数组
            sizeIsSticky = false; // 标记此时比特位数量非用户指定
        }
    }

    /**
     * Ensures that the BitSet can accommodate a given wordIndex,
     * temporarily violating the invariants.  The caller must
     * restore the invariants before returning to the user,
     * possibly using recalculateWordsInUse().
     * @param wordIndex the index to be accommodated.
     */ // 将word数组至少扩容到包含给定的wordIndex下标
    private void expandTo(int wordIndex) {
        int wordsRequired = wordIndex+1; // 计算得出给定的wordIndex情况下所需的最少word数量
        if (wordsInUse < wordsRequired) { // 如果当前的已使用word计数小于计算出的需要的word数量
            ensureCapacity(wordsRequired); // 确保word数组能装的下当前计算出的所需word数量
            wordsInUse = wordsRequired; // 暂时将word计数赋值为计算出的所需word数量, 此时是违背变量校验规则的, 调用方应该计算好属性值再返回给用户(比如调用recalculateWordsInUse())
        }
    }

    /**
     * Checks that fromIndex ... toIndex is a valid range of bit indices.
     */ // 检测fromIndex和toIndex两个参数的合法性, 即两个参数都不能小于0, 且fromIndex不能大于toIndex
    private static void checkRange(int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        if (toIndex < 0)
            throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
        if (fromIndex > toIndex)
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex +
                                                " > toIndex: " + toIndex);
    }

    /**
     * Sets the bit at the specified index to the complement of its
     * current value.
     *
     * @param  bitIndex the index of the bit to flip
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since  1.4
     */ // 将指定下标的比特位值取反
    public void flip(int bitIndex) {
        if (bitIndex < 0) // 校验指定比特位下标不能小于0
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex); // 根据比特位下标获得其所在的word数组下标
        expandTo(wordIndex); // 确保当前word数组范围包含了指定的比特位下标

        words[wordIndex] ^= (1L << bitIndex); // 先将1L左移指定的bitIndex位(如果index超过了long的位数, 即超过64位, 编译器会对其进行区模, 即移位65位相当于移位1位), word元素对其进行异或达到取反效果

        recalculateWordsInUse(); // 重新计算wordsInUse
        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
    }

    /**
     * Sets each bit from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the complement of its current
     * value.
     *
     * @param  fromIndex index of the first bit to flip
     * @param  toIndex index after the last bit to flip
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code toIndex} is negative, or {@code fromIndex} is
     *         larger than {@code toIndex}
     * @since  1.4
     */ // 将比特位下标范围为[fromIndex, toIndex)的比特位值反转
    public void flip(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex); // 检测下标范围是否合法

        if (fromIndex == toIndex) // 如果fromIndex等于toIndex直接返回
            return;

        int startWordIndex = wordIndex(fromIndex); // 获得第一个需要处理的比特位所在word数组下标
        int endWordIndex   = wordIndex(toIndex - 1); // 获得最后一个需要处理的比特位所在word数组下标
        expandTo(endWordIndex); // 确保word数组包含了指定的下标范围

        long firstWordMask = WORD_MASK << fromIndex; // 获得第一个需要处理的word的掩码
        long lastWordMask  = WORD_MASK >>> -toIndex; // 获得最后一个需要处理的word的掩码, 右移负值相当于左移数据比特位长度的补码, 比如此处64-toIndex
        if (startWordIndex == endWordIndex) { // 如果两个word下标相等, 证明是范围在同一个word元素中
            // Case 1: One word 第一种情况, toIndex和fromIndex在同一个word元素中
            words[startWordIndex] ^= (firstWordMask & lastWordMask); // 两个掩码相与, 得到最终的掩码, 即64位掩码中间需要翻转值的部分是1, 其余两端是0, 然后与word元素相异或,掩码0对word本身比特位值无影响, 掩码1对其值造成了取反的结果
        } else {
            // Case 2: Multiple words 第二种情况, toIndex和fromIndex分散在不同的word元素中
            // Handle first word
            words[startWordIndex] ^= firstWordMask; // 将第一个word元素中需要处理的比特位即fromIndex以及其左侧的高比特位取反(比特位由右向左排列)

            // Handle intermediate words, if any
            for (int i = startWordIndex+1; i < endWordIndex; i++) // 将所有中间的word元素整体比特位取反
                words[i] ^= WORD_MASK;

            // Handle last word
            words[endWordIndex] ^= lastWordMask; // 将最后一个需要处理的word元素包含的比特位, 不包含toIndex到右侧的低比特位取反
        }

        recalculateWordsInUse(); // 重新计算wordsInUse已使用word元素计数
        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
    }

    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param  bitIndex a bit index
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since  JDK1.0
     */ // 将指定比特位下标的比特位设置为真
    public void set(int bitIndex) {
        if (bitIndex < 0) // 校验给定比特位下标不能为0
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex); // 获得比特位下标所在的word元素下标
        expandTo(wordIndex); // 确保word数组范围包含了给定下标

        words[wordIndex] |= (1L << bitIndex); // Restores invariants 将指定的比特位下标对应的比特位设置为true

        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
    }

    /**
     * Sets the bit at the specified index to the specified value.
     *
     * @param  bitIndex a bit index
     * @param  value a boolean value to set
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since  1.4
     */ // 将给定的比特位下标对应的比特位值设置为给定的value值
    public void set(int bitIndex, boolean value) {
        if (value)
            set(bitIndex); // 将其设置为真
        else
            clear(bitIndex); // 将其设置为假
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param  fromIndex index of the first bit to be set
     * @param  toIndex index after the last bit to be set
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code toIndex} is negative, or {@code fromIndex} is
     *         larger than {@code toIndex}
     * @since  1.4
     */ /** 将比特位下标范围为[fromIndex, toIndex)的比特位设置为true, 范围赋值原理可以参考{@link #flip(int, int)} */
    public void set(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex); // 检测下标合法性

        if (fromIndex == toIndex) // 如果fromIndex等于toIndex
            return; // 什么也不做直接返回

        // Increase capacity if necessary
        int startWordIndex = wordIndex(fromIndex); // 获得fromIndex所在word元素的下标
        int endWordIndex   = wordIndex(toIndex - 1); // 获得toIndex所在word元素的下标
        expandTo(endWordIndex);

        long firstWordMask = WORD_MASK << fromIndex; // 获取第一个word所需比特位设置为true的掩码
        long lastWordMask  = WORD_MASK >>> -toIndex; // 获取最后一个word所需比特位设置为true的掩码
        if (startWordIndex == endWordIndex) { // 如果两个下标在一个word元素里
            // Case 1: One word
            words[startWordIndex] |= (firstWordMask & lastWordMask); // 先与操作获得最终的掩码, 然后与word元素或操作, 对其比特下标范围内的比特位置为true
        } else {
            // Case 2: Multiple words 两个下标分散在多个word元素里
            // Handle first word
            words[startWordIndex] |= firstWordMask; // 将第一个word中需要赋值的比特位设置为true

            // Handle intermediate words, if any
            for (int i = startWordIndex+1; i < endWordIndex; i++) // 将中间所有word比特位设置为true
                words[i] = WORD_MASK;

            // Handle last word (restores invariants)
            words[endWordIndex] |= lastWordMask; // 将最后一个word中需要赋值的比特位设置为true
        }

        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the specified value.
     *
     * @param  fromIndex index of the first bit to be set
     * @param  toIndex index after the last bit to be set
     * @param  value value to set the selected bits to
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code toIndex} is negative, or {@code fromIndex} is
     *         larger than {@code toIndex}
     * @since  1.4
     */ // 将比特下标为[fromIndex, toIndex)的比特位设置成指定的value值
    public void set(int fromIndex, int toIndex, boolean value) {
        if (value)
            set(fromIndex, toIndex); // 将范围内比特位设置为true
        else
            clear(fromIndex, toIndex); // 将范围内比特位设置为false
    }

    /**
     * Sets the bit specified by the index to {@code false}.
     *
     * @param  bitIndex the index of the bit to be cleared
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since  JDK1.0
     */ // 将指定比特下标的比特位值设置为false
    public void clear(int bitIndex) {
        if (bitIndex < 0) // 校验比特下标不能小于0
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex); // 根据比特下标获得其所在的word元素下标
        if (wordIndex >= wordsInUse) // 如果得到的word下标大于当前已使用word元素计数, 后方的word没被赋值默认值就是false
            return; // 直接返回

        words[wordIndex] &= ~(1L << bitIndex); // 否则将指定比特位赋值为false

        recalculateWordsInUse(); // 重新计算wordsInUse已使用word元素计数
        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param  fromIndex index of the first bit to be cleared
     * @param  toIndex index after the last bit to be cleared
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code toIndex} is negative, or {@code fromIndex} is
     *         larger than {@code toIndex}
     * @since  1.4
     */ /** 将指定比特下标范围[fromIndex, toIndex)的比特位值设置为false, 范围赋值原理可以参考{@link #flip(int, int)} */
    public void clear(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex); // 检测下标合法性

        if (fromIndex == toIndex) // 如果fromIndex等于toIndex
            return; // 直接返回

        int startWordIndex = wordIndex(fromIndex); // 获得开始比特下标对应的word元素下标
        if (startWordIndex >= wordsInUse) // 如果开始word下标大于当前已使用word元素计数, 后方word比特位本身默认值就是false
            return; // 直接返回

        int endWordIndex = wordIndex(toIndex - 1); // 根据比特位下标获得最后一个word元素下标
        if (endWordIndex >= wordsInUse) { // 如果得到的下标大于当前已使用word元素计数, 即toIndex大于实际使用的比特位最大下标
            toIndex = length(); // 获得当前比特位长度
            endWordIndex = wordsInUse - 1; // 根据已使用word元素计数得到最后一个word元素下标
        }

        long firstWordMask = WORD_MASK << fromIndex; // 获得第一个word元素的掩码
        long lastWordMask  = WORD_MASK >>> -toIndex; // 获得最后一个word元素的掩码
        if (startWordIndex == endWordIndex) { // 如果两个word元素下标相等, 证明比特下标范围在一个word元素中
            // Case 1: One word
            words[startWordIndex] &= ~(firstWordMask & lastWordMask); // 将范围内比特位设置为false
        } else {
            // Case 2: Multiple words 比特位下标范围分散在不同的word元素
            // Handle first word
            words[startWordIndex] &= ~firstWordMask; // 第一个元素需要处理的比特位设置为false

            // Handle intermediate words, if any
            for (int i = startWordIndex+1; i < endWordIndex; i++) // 中间所有元素的比特位设置为false
                words[i] = 0;

            // Handle last word
            words[endWordIndex] &= ~lastWordMask; // 最后一个元素需要处理的比特位设置为false
        }

        recalculateWordsInUse(); // 重新计算wordsInUse已使用word元素计数
        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
    }

    /**
     * Sets all of the bits in this BitSet to {@code false}.
     *
     * @since 1.4
     */ // 将所有已使用word元素所有比特位设置为false
    public void clear() {
        while (wordsInUse > 0)
            words[--wordsInUse] = 0; // 将遍历到的word元素包含的所有比特位设置为false
    }

    /**
     * Returns the value of the bit with the specified index. The value
     * is {@code true} if the bit with the index {@code bitIndex}
     * is currently set in this {@code BitSet}; otherwise, the result
     * is {@code false}.
     *
     * @param  bitIndex   the bit index
     * @return the value of the bit with the specified index
     * @throws IndexOutOfBoundsException if the specified index is negative
     */ // 返回给定比特下标对应的比特位值, 如果是0返回false, 如果是1返回true
    public boolean get(int bitIndex) {
        if (bitIndex < 0) // 校验比特下标值不能小于0
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组

        int wordIndex = wordIndex(bitIndex); // 根据给定的比特下标获得到word元素的下标
        return (wordIndex < wordsInUse) // 得到的word元素下标大于等于已使用word元素计数时直接返回false
            && ((words[wordIndex] & (1L << bitIndex)) != 0); // 否则通过与操作, 返回指定比特位值是否为0
    }

    /**
     * Returns a new {@code BitSet} composed of bits from this {@code BitSet}
     * from {@code fromIndex} (inclusive) to {@code toIndex} (exclusive).
     *
     * @param  fromIndex index of the first bit to include
     * @param  toIndex index after the last bit to include
     * @return a new {@code BitSet} from a range of this {@code BitSet}
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code toIndex} is negative, or {@code fromIndex} is
     *         larger than {@code toIndex}
     * @since  1.4
     */ // 用当前BitSet实例中比特范围[fromIndex, toIndex)的值构建一个新的BitSet实例
    public BitSet get(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex); // 检测下标范围合法性

        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组

        int len = length(); // 获得当前比特位长度

        // If no set bits in range return empty bitset
        if (len <= fromIndex || fromIndex == toIndex) // 如果比特位长度小于等于fromIndex或者fromIndex等于toIndex
            return new BitSet(0); // 直接返回一个包含0个比特位的BitSet实例

        // An optimization
        if (toIndex > len) // 如果toIndex大于比特位长度
            toIndex = len; // 将toIndex重新赋值为比特位长度

        BitSet result = new BitSet(toIndex - fromIndex); // 构造一个包含了下标范围内个数个比特位的BitSet实例
        int targetWords = wordIndex(toIndex - fromIndex - 1) + 1; // 计算出下标范围包含的word元素数量
        int sourceIndex = wordIndex(fromIndex); // 得到fromIndex对应的word元素下标
        boolean wordAligned = ((fromIndex & BIT_INDEX_MASK) == 0); // 判断fromIndex是否是64位的整倍数

        // Process all words but the last word 处理除了最后一个word之外所有需要复制的word元素
        for (int i = 0; i < targetWords - 1; i++, sourceIndex++) // 根据targetWords判断需要循环赋值多少次
            result.words[i] = wordAligned ? words[sourceIndex] : // 如果起始下标是64的倍数, 那么直接复制对应的word元素到新的BitSet实例
                (words[sourceIndex] >>> fromIndex) | // 如果不对齐, 每次需要将当前sourceIndex和下一个元素有效比特位拼接成一个完整的long元素
                (words[sourceIndex+1] << -fromIndex); // 拼接后的元素存储到新BitSet实例的word数组中

        // Process the last word 处理最后一个需要复制的word元素
        long lastWordMask = WORD_MASK >>> -toIndex; // 获得最后一个word元素的掩码
        result.words[targetWords - 1] =
            ((toIndex-1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK)
            ? /* straddles source words */ // 最后一个需要复制的word在原始实例里跨两个word元素, 通过或操作拼接两个word中需要复制的比特位
            ((words[sourceIndex] >>> fromIndex) |
             (words[sourceIndex+1] & lastWordMask) << -fromIndex)
            : // 最后一个需要复制的word在原始实例里一个word元素中, 直接通过掩码和移位得到最终需要复制的值
            ((words[sourceIndex] & lastWordMask) >>> fromIndex);

        // Set wordsInUse correctly
        result.wordsInUse = targetWords; // 设置新实例中已经使用word元素计数值
        result.recalculateWordsInUse(); // 重新计算wordsInUse已使用word元素计数
        result.checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组

        return result; // 返回新实例
    }

    /**
     * Returns the index of the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * <p>To iterate over the {@code true} bits in a {@code BitSet},
     * use the following loop:
     *
     *  <pre> {@code
     * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
     *     // operate on index i here
     *     if (i == Integer.MAX_VALUE) {
     *         break; // or (i+1) would overflow
     *     }
     * }}</pre>
     *
     * @param  fromIndex the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there
     *         is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since  1.4
     */ // 从指定的比特下标(包含)开始, 返回下一个第一个找到的比特位值为true的比特下标, 如果没有符合条件的比特位, 返回-1代表没有找到
    public int nextSetBit(int fromIndex) {
        if (fromIndex < 0) // 校验比特下标不能小于0
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组

        int u = wordIndex(fromIndex); // 根据给定的比特下标得到其所在word元素下标
        if (u >= wordsInUse) // 如果得到的下标大于当前已使用word元素计数
            return -1; // 返回-1, 其后方已经没有值为true的比特位

        long word = words[u] & (WORD_MASK << fromIndex); // 将比特下标所在的word元素与左移位的掩码与操作, 将这个word元素右侧fromIndex位比特位置为false

        while (true) { // 循环找到符合条件的比特位下标
            if (word != 0) // 如果当前word元素不为0, 证明其中包含了值为true的比特位
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word); // 返回比特下标乘以每个word代表的比特位数加上当前word中, 尾部低位值为false的比特位数量
            if (++u == wordsInUse) // 当前word为0时, 判断下一个word元素下标是否等于已使用word元素计数, 如果等于证明后方没有值为true的比特位
                return -1; // 返回-1代表未找到
            word = words[u]; // 否则后面的word元素还在wordsInUse范围内, 切换到下一个word元素进行比特位值为true的下标的寻找
        }
    }

    /**
     * Returns the index of the first bit that is set to {@code false}
     * that occurs on or after the specified starting index.
     *
     * @param  fromIndex the index to start checking from (inclusive)
     * @return the index of the next clear bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since  1.4
     */ // 从指定的比特下标(包含)开始, 返回下一个第一个找到的比特位值为false的比特下标, 如果没有符合条件的比特位, 返回-1代表没有找到
    public int nextClearBit(int fromIndex) {
        // Neither spec nor implementation handle bitsets of maximal length.
        // See 4816253.
        if (fromIndex < 0) // 校验比特下标不能小于0
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组

        int u = wordIndex(fromIndex); // 根据给定的比特下标得到其所在word元素下标
        if (u >= wordsInUse) // 如果word元素下标的元素位置数量已经超过了已使用word元素计数
            return fromIndex; // 那么fromIndex就是值为false, 直接返回fromIndex
        // 将fromIndex属于的word元素取反, 巧妙的把找值为false的比特位问题变成了nextSetBit方法中找值为true的比特位问题
        long word = ~words[u] & (WORD_MASK << fromIndex); // 将比特下标所在的word元素取反与左移位的掩码与操作, 将这个word元素右侧fromIndex位比特位置为false

        while (true) { // 循环找到符合条件的比特位下标
            if (word != 0) // 如果当前word元素不为0, 证明其中包含了值为true的比特位(寻找值为false的比特位已经转变成了寻找值为true的比特位)
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word); // 返回比特下标乘以每个word代表的比特位数加上当前word中, 尾部低位值为false的比特位数量
            if (++u == wordsInUse) // 当前word为0时, 判断下一个word元素下标是否等于已使用word元素计数, 如果等于证明后方没有值为true的比特位
                return wordsInUse * BITS_PER_WORD; // 直接返回wordsInUse * BITS_PER_WORD即是值为false的比特位下标
            word = ~words[u]; // 否则后面的word元素还在wordsInUse范围内, 切换到下一个word元素并将其取反, 继续进行比特位值为true的下标的寻找
        }
    }

    /**
     * Returns the index of the nearest bit that is set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * <p>To iterate over the {@code true} bits in a {@code BitSet},
     * use the following loop:
     *
     *  <pre> {@code
     * for (int i = bs.length(); (i = bs.previousSetBit(i-1)) >= 0; ) {
     *     // operate on index i here
     * }}</pre>
     *
     * @param  fromIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there
     *         is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *         than {@code -1}
     * @since  1.7
     */ // 从指定的比特下标(包含)开始, 返回上一个第一个找到的比特位值为true的比特下标, 如果没有符合条件的比特位, 返回-1代表没有找到
    public int previousSetBit(int fromIndex) {
        if (fromIndex < 0) { // 校验给定的比特下标不能小于-1
            if (fromIndex == -1)
                return -1;
            throw new IndexOutOfBoundsException(
                "fromIndex < -1: " + fromIndex);
        }

        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组

        int u = wordIndex(fromIndex); // 根据给定的比特下标得到其所在word元素下标
        if (u >= wordsInUse) // 如果计算出的所属word元素下标大于等于已使用word元素计数
            return length() - 1; // 返回总共比特位长度减一, 即使前一个要选找的值为true的比特位下标

        long word = words[u] & (WORD_MASK >>> -(fromIndex+1)); // 将给定比特下标所属word元素, 下标之外的高位比特设置为false

        while (true) { // 循环寻找符合条件的比特下标值
            if (word != 0) // 如果当前word元素不为0, 证明此word元素fromIndex或之前还有值为true的比特位
                return (u+1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word); // 返回此word元素中, 给定fromIndex或之前的比特下标值
            if (u-- == 0) // 如果当前word下标u已经为0, 证明没有找到符合条件的比特位
                return -1; // 直接返回-1
            word = words[u]; // 否则继续寻找前一个word元素
        }
    }

    /**
     * Returns the index of the nearest bit that is set to {@code false}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param  fromIndex the index to start checking from (inclusive)
     * @return the index of the previous clear bit, or {@code -1} if there
     *         is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *         than {@code -1}
     * @since  1.7
     */ // 从指定的比特下标(包含)开始, 返回上一个第一个找到的比特位值为false的比特下标, 如果没有符合条件的比特位, 返回-1代表没有找到
    public int previousClearBit(int fromIndex) {
        if (fromIndex < 0) { // 校验给定的比特下标不能小于-1
            if (fromIndex == -1)
                return -1;
            throw new IndexOutOfBoundsException(
                "fromIndex < -1: " + fromIndex);
        }

        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组

        int u = wordIndex(fromIndex); // 根据给定的比特下标得到其所在word元素下标
        if (u >= wordsInUse) // 如果计算出的所属word元素下标大于等于已使用word元素计数
            return fromIndex; // 那么当前给定的比特位值就是false, 返回当前给定下标

        long word = ~words[u] & (WORD_MASK >>> -(fromIndex+1)); // 依旧是将word元素取反, 将寻找值为false的比特位问题转换成寻找值为true的比特位问题, 然后将低fromIndex+1位比特位设置为false

        while (true) { // 循环寻找符合条件的比特下标值
            if (word != 0) // 如果当前word元素不为0, 证明此word元素fromIndex或之前还有值为true的比特位
                return (u+1) * BITS_PER_WORD -1 - Long.numberOfLeadingZeros(word); // 返回此word元素中, 给定fromIndex或之前的比特下标值
            if (u-- == 0) // 如果当前word下标u已经为0, 证明没有找到符合条件的比特位
                return -1; // 直接返回-1
            word = ~words[u]; // 否则继续寻找前一个word元素
        }
    }

    /**
     * Returns the "logical size" of this {@code BitSet}: the index of
     * the highest set bit in the {@code BitSet} plus one. Returns zero
     * if the {@code BitSet} contains no set bits.
     *
     * @return the logical size of this {@code BitSet}
     * @since  1.2
     */ // 返回当前实例比特位长度(最高一位值为true的比特位下标加一)
    public int length() {
        if (wordsInUse == 0) // 如果已使用word元素计数为0
            return 0; // 直接返回0

        return BITS_PER_WORD * (wordsInUse - 1) + // 计算除了最后一个已使用的word之外已使用的word元素包含的比特位
            (BITS_PER_WORD - Long.numberOfLeadingZeros(words[wordsInUse - 1])); // 加上最后一个已使用word元素包含的有效比特位
    }

    /**
     * Returns true if this {@code BitSet} contains no bits that are set
     * to {@code true}.
     *
     * @return boolean indicating whether this {@code BitSet} is empty
     * @since  1.4
     */ // 返回此BitSet实例逻辑长度是否为0
    public boolean isEmpty() {
        return wordsInUse == 0;
    }

    /**
     * Returns true if the specified {@code BitSet} has any bits set to
     * {@code true} that are also set to {@code true} in this {@code BitSet}.
     *
     * @param  set {@code BitSet} to intersect with
     * @return boolean indicating whether this {@code BitSet} intersects
     *         the specified {@code BitSet}
     * @since  1.4
     */ // 返回当前BitSet实例与给定的BitSet参数是否有交集, 存在交集返回true
    public boolean intersects(BitSet set) {
        for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--) // 取两个BitSet已使用word计数较小的值为循环次数
            if ((words[i] & set.words[i]) != 0) // 如果存在对应位置word元素与操作后值不为0, 证明有交集
                return true; // 返回true
        return false; // 如果每一个word元素都没有交集, 返回false
    }

    /**
     * Returns the number of bits set to {@code true} in this {@code BitSet}.
     *
     * @return the number of bits set to {@code true} in this {@code BitSet}
     * @since  1.4
     */ // 返回当前BitSet实例中值为true的比特位数
    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < wordsInUse; i++) // 遍历没一个已使用的word元素
            sum += Long.bitCount(words[i]); // 对每一个word元素中包含的值为true的比特位数量进行累加
        return sum; // 返回值为true的比特位总数
    }

    /**
     * Performs a logical <b>AND</b> of this target bit set with the
     * argument bit set. This bit set is modified so that each bit in it
     * has the value {@code true} if and only if it both initially
     * had the value {@code true} and the corresponding bit in the
     * bit set argument also had the value {@code true}.
     *
     * @param set a bit set
     */ // 对当前BitSet实例和给定BitSet执行与操作
    public void and(BitSet set) {
        if (this == set) // 如果是与自身进行与操作
            return; // 不发生任何改变, 直接返回

        while (wordsInUse > set.wordsInUse) // 如果参数BitSet的已使用word元素数量比当前实例少
            words[--wordsInUse] = 0; // 将当前BitSet实例多使用的word元素比特位置为0

        // Perform logical AND on words in common
        for (int i = 0; i < wordsInUse; i++) // 对两个BitSet中对应的比特位进行与操作, 并将结果赋值给当前实例
            words[i] &= set.words[i];

        recalculateWordsInUse(); // 重新计算wordsInUse已使用word元素计数
        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
    }

    /**
     * Performs a logical <b>OR</b> of this bit set with the bit set
     * argument. This bit set is modified so that a bit in it has the
     * value {@code true} if and only if it either already had the
     * value {@code true} or the corresponding bit in the bit set
     * argument has the value {@code true}.
     *
     * @param set a bit set
     */ // 对当前BitSet实例和给定BitSet执行或操作
    public void or(BitSet set) {
        if (this == set) // 如果是与自身进行与操作
            return; // 不发生任何改变, 直接返回

        int wordsInCommon = Math.min(wordsInUse, set.wordsInUse); // 获得两个实例同样使用的word元素数量

        if (wordsInUse < set.wordsInUse) { // 如果当前实例已使用word元素比给定参数使用的少
            ensureCapacity(set.wordsInUse); // 那么将当前实例扩容到与参数实例一样大
            wordsInUse = set.wordsInUse; // 并且更新已使用word计数为参数实例word计数
        }

        // Perform logical OR on words in common
        for (int i = 0; i < wordsInCommon; i++) // 对相同长度部分的word元素, 每个对应的元素执行或操作
            words[i] |= set.words[i]; // 并将结果赋值给当前实例word数组

        // Copy any remaining words
        if (wordsInCommon < set.wordsInUse) // 将参数多使用的word元素部分直接复制到当前实例(和执行或操作结果是一样的)
            System.arraycopy(set.words, wordsInCommon,
                             words, wordsInCommon,
                             wordsInUse - wordsInCommon);

        // recalculateWordsInUse() is unnecessary 不需要重新计算已使用word元素计数, 因为本身参数实例自己维护的wordsInUse就是正确的
        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
    }

    /**
     * Performs a logical <b>XOR</b> of this bit set with the bit set
     * argument. This bit set is modified so that a bit in it has the
     * value {@code true} if and only if one of the following
     * statements holds:
     * <ul>
     * <li>The bit initially has the value {@code true}, and the
     *     corresponding bit in the argument has the value {@code false}.
     * <li>The bit initially has the value {@code false}, and the
     *     corresponding bit in the argument has the value {@code true}.
     * </ul>
     *
     * @param  set a bit set
     */ // 对当前实例与给定BitSet参数执行异或(exclusive or对应位不同则为true)操作
    public void xor(BitSet set) {
        int wordsInCommon = Math.min(wordsInUse, set.wordsInUse); // 获得两个实例同样使用的word元素数量

        if (wordsInUse < set.wordsInUse) { // 如果当前实例已使用word元素比给定参数使用的少
            ensureCapacity(set.wordsInUse); // 那么将当前实例扩容到与参数实例一样大
            wordsInUse = set.wordsInUse; // 并且更新已使用word计数为参数实例word计数
        }

        // Perform logical XOR on words in common
        for (int i = 0; i < wordsInCommon; i++) // 对相同长度部分的word元素, 每个对应的元素执行异或操作
            words[i] ^= set.words[i]; // 并将结果赋值给当前实例word数组

        // Copy any remaining words
        if (wordsInCommon < set.wordsInUse) // 将参数多使用的word元素部分直接复制到当前实例(和执行异或操作结果是一样的)
            System.arraycopy(set.words, wordsInCommon,
                             words, wordsInCommon,
                             set.wordsInUse - wordsInCommon);

        recalculateWordsInUse(); // @QUESTION 这一块应该与or方法一样, 并不需要重新计算已使用word元素计数, 因为本身参数实例自己维护的wordsInUse就是正确的
        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
    }

    /**
     * Clears all of the bits in this {@code BitSet} whose corresponding
     * bit is set in the specified {@code BitSet}.
     *
     * @param  set the {@code BitSet} with which to mask this
     *         {@code BitSet}
     * @since  1.2
     */ // 将当前实例与给定的BitSet参数取反后的结果进行与操作, 即当前实例减去给定实例
    public void andNot(BitSet set) {
        // Perform logical (a & !b) on words in common
        for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--) // 对两个BitSet较小的wordsInUse部分进行逻辑处理
            words[i] &= ~set.words[i];

        recalculateWordsInUse(); // 重新计算wordsInUse已使用word元素计数
        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
    }

    /**
     * Returns the hash code value for this bit set. The hash code depends
     * only on which bits are set within this {@code BitSet}.
     *
     * <p>The hash code is defined to be the result of the following
     * calculation:
     *  <pre> {@code
     * public int hashCode() {
     *     long h = 1234;
     *     long[] words = toLongArray();
     *     for (int i = words.length; --i >= 0; )
     *         h ^= words[i] * (i + 1);
     *     return (int)((h >> 32) ^ h);
     * }}</pre>
     * Note that the hash code changes if the set of bits is altered.
     *
     * @return the hash code value for this bit set
     */ // 根据一定规则通过每一个word元素计算出当前BitSet实例的hashCode
    public int hashCode() {
        long h = 1234;
        for (int i = wordsInUse; --i >= 0; )
            h ^= words[i] * (i + 1);

        return (int)((h >> 32) ^ h);
    }

    /**
     * Returns the number of bits of space actually in use by this
     * {@code BitSet} to represent bit values.
     * The maximum element in the set is the size - 1st element.
     *
     * @return the number of bits currently in this bit set
     */ // 返回当前BitSet实例此时的word数组能容纳多少比特位信息
    public int size() {
        return words.length * BITS_PER_WORD;
    }

    /**
     * Compares this object against the specified object.
     * The result is {@code true} if and only if the argument is
     * not {@code null} and is a {@code Bitset} object that has
     * exactly the same set of bits set to {@code true} as this bit
     * set. That is, for every nonnegative {@code int} index {@code k},
     * <pre>((BitSet)obj).get(k) == this.get(k)</pre>
     * must be true. The current sizes of the two bit sets are not compared.
     *
     * @param  obj the object to compare with
     * @return {@code true} if the objects are the same;
     *         {@code false} otherwise
     * @see    #size()
     */ // 判断给定对象是否与当前BitSet实例相等
    public boolean equals(Object obj) {
        if (!(obj instanceof BitSet)) // 如果给定对象不是BitSrt实例
            return false; // 直接返回false
        if (this == obj) // 如果给定对象就是当前实例同一个对象
            return true; // 返回true

        BitSet set = (BitSet) obj;

        checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
        set.checkInvariants(); // 校验wordsInUse是否正确表达了参数实例中的word数组

        if (wordsInUse != set.wordsInUse) // 如果两个实例的wordsInUse计数不相等
            return false; // 返回false

        // Check words in use by both BitSets
        for (int i = 0; i < wordsInUse; i++) // 检测两个实例中每个对应的word元素是否存在不相等
            if (words[i] != set.words[i]) // 如果存在不相等
                return false; // 返回false

        return true; // 如果对应word全都相等, 返回true
    }

    /**
     * Cloning this {@code BitSet} produces a new {@code BitSet}
     * that is equal to it.
     * The clone of the bit set is another bit set that has exactly the
     * same bits set to {@code true} as this bit set.
     *
     * @return a clone of this bit set
     * @see    #size()
     */ // 克隆复制一个与当前实例相等的BitSet实例
    public Object clone() {
        if (! sizeIsSticky)
            trimToSize();

        try {
            BitSet result = (BitSet) super.clone();
            result.words = words.clone();
            result.checkInvariants();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * Attempts to reduce internal storage used for the bits in this bit set.
     * Calling this method may, but is not required to, affect the value
     * returned by a subsequent call to the {@link #size()} method.
     */ // 尝试减少当前BitSet实例内部word数组占用的空间, 只将数组已经使用的word数量个位置保留
    private void trimToSize() {
        if (wordsInUse != words.length) { // 如果已经使用word元素数量还没等于word数组长度
            words = Arrays.copyOf(words, wordsInUse); // 将已经使用的word元素复制到新的数组并设置为当前word数组
            checkInvariants(); // 校验wordsInUse是否正确表达了当前word数组
        }
    }

    /**
     * Save the state of the {@code BitSet} instance to a stream (i.e.,
     * serialize it).
     */ // 将当前实例序列化到输出流
    private void writeObject(ObjectOutputStream s)
        throws IOException {

        checkInvariants();

        if (! sizeIsSticky)
            trimToSize();

        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("bits", words);
        s.writeFields();
    }

    /**
     * Reconstitute the {@code BitSet} instance from a stream (i.e.,
     * deserialize it).
     */ // 从输入流反序列化BitSet实例
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {

        ObjectInputStream.GetField fields = s.readFields();
        words = (long[]) fields.get("bits", null);

        // Assume maximum length then find real length
        // because recalculateWordsInUse assumes maintenance
        // or reduction in logical size
        wordsInUse = words.length;
        recalculateWordsInUse();
        sizeIsSticky = (words.length > 0 && words[words.length-1] == 0L); // heuristic
        checkInvariants();
    }

    /**
     * Returns a string representation of this bit set. For every index
     * for which this {@code BitSet} contains a bit in the set
     * state, the decimal representation of that index is included in
     * the result. Such indices are listed in order from lowest to
     * highest, separated by ",&nbsp;" (a comma and a space) and
     * surrounded by braces, resulting in the usual mathematical
     * notation for a set of integers.
     *
     * <p>Example:
     * <pre>
     * BitSet drPepper = new BitSet();</pre>
     * Now {@code drPepper.toString()} returns "{@code {}}".
     * <pre>
     * drPepper.set(2);</pre>
     * Now {@code drPepper.toString()} returns "{@code {2}}".
     * <pre>
     * drPepper.set(4);
     * drPepper.set(10);</pre>
     * Now {@code drPepper.toString()} returns "{@code {2, 4, 10}}".
     *
     * @return a string representation of this bit set
     */ // 默认的toString方法, 类似集合的表现形式, 返回比特位为true的比特位的下标构成的集合, 如{2, 4, 10}
    public String toString() {
        checkInvariants();

        int numBits = (wordsInUse > 128) ?
            cardinality() : wordsInUse * BITS_PER_WORD;
        StringBuilder b = new StringBuilder(6*numBits + 2);
        b.append('{');

        int i = nextSetBit(0);
        if (i != -1) {
            b.append(i);
            while (true) {
                if (++i < 0) break;
                if ((i = nextSetBit(i)) < 0) break;
                int endOfRun = nextClearBit(i);
                do { b.append(", ").append(i); }
                while (++i != endOfRun);
            }
        }

        b.append('}');
        return b.toString();
    }

    /**
     * Returns a stream of indices for which this {@code BitSet}
     * contains a bit in the set state. The indices are returned
     * in order, from lowest to highest. The size of the stream
     * is the number of bits in the set state, equal to the value
     * returned by the {@link #cardinality()} method.
     *
     * <p>The bit set must remain constant during the execution of the
     * terminal stream operation.  Otherwise, the result of the terminal
     * stream operation is undefined.
     *
     * @return a stream of integers representing set indices
     * @since 1.8
     */ /** 获得当前BitSet实例中每个为true的比特位下标组成的流, 留中元素个数等于{@link #cardinality()}返回值 */
    public IntStream stream() {
        class BitSetIterator implements PrimitiveIterator.OfInt { // 定义一个BitSet的Iterator, 此迭代器返回值为true的比特位下标
            int next = nextSetBit(0); // 下一个值为true的比特位下标
            /** 见{@link Iterator#hasNext()} */
            @Override
            public boolean hasNext() {
                return next != -1;
            }
            /** 见{@link OfInt#nextInt()} */
            @Override
            public int nextInt() {
                if (next != -1) {
                    int ret = next;
                    next = nextSetBit(next+1);
                    return ret;
                } else {
                    throw new NoSuchElementException();
                }
            }
        }
        // 用上方定义的迭代器构造一个IntStream返回
        return StreamSupport.intStream(
                () -> Spliterators.spliterator(
                        new BitSetIterator(), cardinality(),
                        Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED),
                Spliterator.SIZED | Spliterator.SUBSIZED |
                        Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED,
                false);
    }
}
