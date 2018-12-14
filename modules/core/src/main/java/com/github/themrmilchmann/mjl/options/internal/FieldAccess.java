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
package com.github.themrmilchmann.mjl.options.internal;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import javax.annotation.Nullable;

/**
 * <b>Do NOT use this interface in external code. It is strictly UNSUPPORTED and WILL BREAK when using this library as
 * module!</b>
 *
 * @author  Leon Linhart
 */
public final class FieldAccess {

    @SuppressWarnings("unchecked")
    public static <T> T getStatic(Field field, MethodHandles.Lookup lookup) {
        boolean wasAccessible = field.isAccessible();

        if (!wasAccessible) field.setAccessible(true);

        try {
            return (T) field.get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(String.format("Failed to get value from field %s.", field));
        } finally {
            if (!wasAccessible) field.setAccessible(false);
        }
    }

    public static void set(Field field, Object instance, @Nullable Object value, MethodHandles.Lookup lookup) {
        boolean wasAccessible = field.isAccessible();

        if (!wasAccessible) field.setAccessible(true);

        Class<?> type = field.getType();

        try {
            if (type == boolean.class) {
                field.setBoolean(instance, (boolean) value);
            } else if (type == byte.class) {
                field.setByte(instance, (byte) value);
            } else if (type == char.class) {
                field.setChar(instance, (char) value);
            } else if (type == short.class) {
                field.setShort(instance, (short) value);
            } else if (type == int.class) {
                field.setInt(instance, (int) value);
            } else if (type == long.class) {
                field.setLong(instance, (long) value);
            } else if (type == float.class) {
                field.setFloat(instance, (float) value);
            } else if (type == double.class) {
                field.setDouble(instance, (double) value);
            } else {
                field.set(instance, value);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NullPointerException | ExceptionInInitializerError e) {
            throw new RuntimeException(String.format("Failed to inject value %s into field %s of object %s.", value, field, instance), e);
        }

        if (!wasAccessible) field.setAccessible(false);
    }

}