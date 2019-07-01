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
 * Elements marked with this annotation are mapped to options.
 *
 * @since   0.4.0
 *
 * @author  Leon Linhart
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionHolder {

    /**
     * Returns the long token for the option.
     *
     * @return  the long token for the option
     *
     * @since   0.4.0
     */
    String longToken();

    /**
     * Returns the short token for the option (or {@code '\0'}.
     *
     * @return  the short token for the option
     *
     * @since   0.4.0
     */
    char shortToken() default '\0';

}