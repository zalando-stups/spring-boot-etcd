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
 * The etcd error codes as defined in the error codes documentation.
 *
 * @see <a href="https://coreos.com/etcd/docs/2.1.0/api.html">https://coreos.com
 *      /etcd/docs/2.1.0/api.html</a>
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
