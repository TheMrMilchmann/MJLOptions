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

/**
 * A {@code ParsingException} indicates failure while {@link OptionParser parsing} a command.
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
public class ParsingException extends RuntimeException {

    /**
     * Creates a new {@code ParsingException}.
     *
     * @since   0.3.0
     */
    public ParsingException() {
        super();
    }

    /**
     * Creates a new {@code ParsingException}.
     *
     * @param message   a detailed error message
     *
     * @since   0.3.0
     */
    public ParsingException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code ParsingException}.
     *
     * @param message   a detailed error message
     * @param cause     the cause
     *
     * @since   0.4.0
     */
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

}