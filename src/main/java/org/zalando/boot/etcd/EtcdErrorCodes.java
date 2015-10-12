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
 * The etcd error codes as defined in the error codes documentation.
 *
 * @see  https://coreos.com/etcd/docs/2.1.0/errorcode.html
 */
public final class EtcdErrorCodes {

    /**
     * error code key not found.
     */
    public static final int ECODE_KEY_NOT_FOUND = 100;

    /**
     * error code compare failed.
     */
    public static final int ECODE_TEST_FAILED = 101;

    /**
     * error code not a file.
     */
    public static final int ECODE_NOT_FILE = 102;

    /**
     * error code not a directory.
     */
    public static final int ECODE_NOT_DIR = 104;

    /**
     * error code key already exists.
     */
    public static final int ECODE_NODE_EXIST = 105;

    /**
     * error code root is read only.
     */
    public static final int ECODE_ROOT_RONLY = 107;

    /**
     * error code directory is not empty.
     */
    public static final int ECODE_DIR_NOT_EMPTY = 108;

    /**
     * Creates a new EtcdErrorCodes. Hidden to prevent instantiation
     */
    private EtcdErrorCodes() {
        super();
    }
}
