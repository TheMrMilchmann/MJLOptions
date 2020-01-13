/*
 * Copyright 2018-2020 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.themrmilchmann.mjl.options;

import java.util.Set;

/**
 * Indicates that one or more options in a pool are unreachable.
 *
 * <p>An options is considered to be unreachable when no valid input containing the option exists such that all
 * restrictions are fulfilled. This may only be caused by misconfiguring overlapping restrictions.</p>
 *
 * @since   0.4.0
 *
 * @author  Leon Linhart
 */
public final class UnreachableOptionException extends ConfigurationException {

    private final Set<Option<?>> unreachable;

    UnreachableOptionException(Set<Option<?>> unreachable) {
        super();
        this.unreachable = unreachable;
    }

    /**
     * Returns the set of options that are unreachable.
     *
     * @return  the set of options that are unreachable
     *
     * @since   0.4.0
     */
    public Set<Option<?>> getUnreachableOptions() {
        return this.unreachable;
    }

}