/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.annotation;

/**
 * Annotation retention policy.  The constants of this enumerated type
 * describe the various policies for retaining annotations.  They are used
 * in conjunction with the {@link Retention} meta-annotation type to specify
 * how long annotations are to be retained.
 *
 * @author  Joshua Bloch
 * @since 1.5
 */ // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/4/11 11:26
public enum RetentionPolicy { /** 注解上用元注解{@link Retention}将此枚举当做参数, 用于标记注解保留的策略 */
    /**
     * Annotations are to be discarded by the compiler.
     */ /** 处理源码级别的注解方法见{@link javax.annotation.processing.Processor}, 大名鼎鼎的lombok就应用了源码级别注解 */
    SOURCE, /** 标记注解只在源代码中保留, 编译后的class文件不包含. 源码级别的注解可以用做文档的补充如{@link Override}, 也可以作为源码生成器的标识 */

    /**
     * Annotations are to be recorded in the class file by the compiler
     * but need not be retained by the VM at run time.  This is the default
     * behavior.
     */
    CLASS, /** {@link Retention}注解默认行为, 标记注解保留在class文件中, 但是运行时内存不包含. class级别的注解可以作为动态代理工具修改字节码的依据 */

    /**
     * Annotations are to be recorded in the class file by the compiler and
     * retained by the VM at run time, so they may be read reflectively.
     *
     * @see java.lang.reflect.AnnotatedElement
     */
    RUNTIME // 标记注解从始至终都保留, 包括运行时内存中, 可以被反射机制获取到
}
