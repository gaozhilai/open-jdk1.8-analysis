/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.reflect;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;

/**
 * A method annotated @CallerSensitive is sensitive to its calling class,
 * via {@link sun.reflect.Reflection#getCallerClass Reflection.getCallerClass},
 * or via some equivalent.
 *
 * @author John R. Rose
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD}) // 由 GaoZhilai 进行分析注释, 不正确的地方敬请斧正, 希望帮助大家节省阅读源代码的时间 2020/7/16 14:48
public @interface CallerSensitive { /** 此注解专门为方法{@link Reflection#getCallerClass()}服务, 并且只有类由系统类加载器或者扩展加载器加载时此注解才生效 */
} // getCallerClass()方法返回调用链第一个不被此注解注释的方法对应的调用者Class对象
