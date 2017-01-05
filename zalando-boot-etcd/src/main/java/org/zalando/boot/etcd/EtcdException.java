/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Zalando SE
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
 */
package org.zalando.boot.etcd;

/**
 * Exception indicating an error from the etcd server with encapsulated etcd error object.
 */
public class EtcdException extends Exception {

    /**
     * serial version UID
     */
    private static final long serialVersionUID = -7372680856482654227L;

    /**
     * error
     */
    private EtcdError error;

    /**
     * Creates new EtcdException with the given etcd error.
     * 
     * @param error
     *            the etcd error
     */
    public EtcdException(EtcdError error) {
        this.error = error;
    }

    /**
     * Creates a new EtcdException with the given etcd error and error message.
     * 
     * @param error
     *            the etcd error
     * @param message
     *            the error message
     */
    public EtcdException(EtcdError error, String message) {
        super(message);
        this.error = error;
    }

    /**
     * Creates a new EtcdException with the given etcd error, error message, and
     * cause.
     * 
     * @param error
     *            the etcd error
     * @param message
     *            the error message
     * @param cause
     *            the error cause
     */
    public EtcdException(EtcdError error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    /**
     * Creates a new EtcdException with the given etcd error and cause.
     * 
     * @param error
     *            the etcd error
     * @param cause
     *            the error cause
     */
    public EtcdException(EtcdError error, Throwable cause) {
        super(cause);
        this.error = error;
    }

    /**
     * @return the error
     */
    public EtcdError getError() {
        return error;
    }
    
}
