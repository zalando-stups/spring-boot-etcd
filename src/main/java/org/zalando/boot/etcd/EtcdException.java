/*
 * Copyright 2015 Zalando SE
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
