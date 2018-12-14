/*
 * Copyright 2018-2019 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.themrmilchmann.mjl.options.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields marked with this annotation are mapped to arguments.
 *
 * @since   0.4.0
 *
 * @author  Leon Linhart
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgumentHolder {

    /**
     * Returns the index of the argument represented by this field.
     *
     * @return  the index of the argument represented by this field
     *
     * @since   0.4.0
     */
    int index();

    /**
     * Returns whether or not the argument represented by this field is optional.
     *
     * @return  whether or not the argument represented by this field is optional
     *
     * @since   0.4.0
     */
    boolean optional() default false;

}