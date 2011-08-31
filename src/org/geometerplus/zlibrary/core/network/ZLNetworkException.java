/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.network;


public class ZLNetworkException extends Exception {
	private static final long serialVersionUID = 4272384299121648643L;

	// Messages with no parameters:
	public static final String ERROR_UNKNOWN_ERROR = "unknownErrorMessage";
	public static final String ERROR_TIMEOUT = "operationTimedOutMessage";
	public static final String ERROR_CONNECT_TO_NETWORK = "couldntConnectToNetworkMessage";
	public static final String ERROR_UNSUPPORTED_PROTOCOL = "unsupportedProtocol";
	public static final String ERROR_INVALID_URL = "invalidURL";
	public static final String ERROR_AUTHENTICATION_FAILED = "authenticationFailed";

	// Messages with one parameter:
	public static final String ERROR_SOMETHING_WRONG = "somethingWrongMessage";
	public static final String ERROR_CREATE_DIRECTORY = "couldntCreateDirectoryMessage";
	public static final String ERROR_CREATE_FILE = "couldntCreateFileMessage";
	public static final String ERROR_CONNECT_TO_HOST = "couldntConnectMessage";
	public static final String ERROR_RESOLVE_HOST = "couldntResolveHostMessage";
	public static final String ERROR_HOST_CANNOT_BE_REACHED = "hostCantBeReached";
	public static final String ERROR_CONNECTION_REFUSED = "connectionRefused";

	public ZLNetworkException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ZLNetworkException(String message) {
		super(message);
	}
}
