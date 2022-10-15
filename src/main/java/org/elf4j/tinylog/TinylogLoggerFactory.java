/*
 * MIT License
 *
 * Copyright (c) 2022 elf4j-tinylog
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.elf4j.tinylog;

import org.elf4j.Logger;
import org.elf4j.spi.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Service Provider Interface implementation.
 * <p>
 * Refer to <a href="https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html">Introduction to the Service Provider
 * Interfaces</a>
 */
public class TinylogLoggerFactory implements LoggerFactory {
    @Override
    public Logger logger(@Nullable String name) {
        return TinylogJlfLogger.instance(name);
    }

    @Override
    public Logger logger(Class<?> clazz) {
        return clazz == null ? logger((String) null) : logger(clazz.getName());
    }
}
