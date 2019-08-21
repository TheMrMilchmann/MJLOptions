/*
 * Copyright 2018-2019 Leon Linhart
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
 * Indicates that an option is not recognized in the context of the given {@link OptionPool pool} when
 * {@link OptionParser#parseFragments(OptionPool, String...) parsing} parameters.
 *
 * @since   0.4.0
 *
 * @author  Leon Linhart
 */
public final class UnrecognizedOptionException extends ParsingException {

    private final String identifier;
    private final String unrecognized;

    UnrecognizedOptionException(String identifier) {
        super();
        this.identifier = String.format("--%s", identifier);
        this.unrecognized = identifier;
    }

    UnrecognizedOptionException(String identifier, char unrecognized) {
        super();
        this.identifier = String.format("-%s", identifier);
        this.unrecognized = String.valueOf(unrecognized);
    }

    /**
     * Returns the identifier of the unrecognized option.
     *
     * <p>The identifier is the prefixed token (or cluster of tokens).</p>
     *
     * @return  the identifier of the unrecognized option
     *
     * @since   0.4.0
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the token of the unrecognized option.
     *
     * @return  the token of the unrecognized option
     *
     * @since   0.4.0
     */
    public String getUnrecognized() {
        return this.unrecognized;
    }

}