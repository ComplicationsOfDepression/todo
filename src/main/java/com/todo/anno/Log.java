// 定义一个名为 "Log" 的注解
package com.todo.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 指定注解的保留策略为运行时
@Retention(RetentionPolicy.RUNTIME)
// 指定注解的作用目标为模块（Module）
@Target(ElementType.METHOD)
public @interface Log {
}
