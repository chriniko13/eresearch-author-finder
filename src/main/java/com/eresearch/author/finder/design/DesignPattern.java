package com.eresearch.author.finder.design;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE) // it is used only as documentation.
public @interface DesignPattern {

	DesingPatterns[] value();
}
