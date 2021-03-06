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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * Composite exception for errors that accumulate during dynamic {@link OptionPool} discovery.
 *
 * @since   0.4.0
 *
 * @author  Leon Linhart
 */
public final class ClassPoolConfigurationException extends ConfigurationException {

    private final List<Throwable> errors;

    ClassPoolConfigurationException(List<Throwable> errors) {
        this.errors = Collections.unmodifiableList(errors);
    }

    /**
     * Returns an unmodifiable view of the accumulated errors.
     *
     * @return  an unmodifiable view of the accumulated errors
     *
     * @since   0.4.0
     */
    public List<Throwable> getErrors() {
        return this.errors;
    }

    /**
     * {@inheritDoc}
     *
     * @since   0.4.0
     */
    @Override
    public void printStackTrace(PrintStream s) {
        this.errors.forEach(Throwable::printStackTrace);
    }

    /**
     * {@inheritDoc}
     *
     * @since   0.4.0
     */
    @Override
    public void printStackTrace(PrintWriter s) {
        this.errors.forEach(Throwable::printStackTrace);
    }

}