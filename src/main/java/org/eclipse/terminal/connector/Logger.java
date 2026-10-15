/*******************************************************************************
 * Copyright (c) 2005, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Fran Litterio (Wind River) - initial API and implementation
 * Ted Williams (Wind River) - refactored into org.eclipse namespace
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.terminal.connector;

import org.slf4j.LoggerFactory;

/**
 * A simple logger class. Every method in this class is static, so they can be
 * called from both class and instance methods. To use this class, write code
 * like this:
 *
 * <pre>
 * Logger.log(&quot;something has happened&quot;);
 * Logger.log(&quot;counter is &quot; + counter);
 * </pre>
 *
 * This fork routes everything to SLF4J (logger name
 * <code>org.eclipse.terminal</code>). Trace output ({@link #log(String)}) is
 * emitted at TRACE level; the additional debug options are enabled through
 * system properties named after the former Eclipse trace options, e.g.
 * <code>-Dorg.eclipse.terminal.control/debug/log/char=true</code>.
 */
public final class Logger {
	public static final String TRACE_DEBUG_LOG = "org.eclipse.terminal.control/debug/log"; //$NON-NLS-1$
	public static final String TRACE_DEBUG_LOG_CHAR = "org.eclipse.terminal.control/debug/log/char"; //$NON-NLS-1$
	public static final String TRACE_DEBUG_LOG_VT100BACKEND = "org.eclipse.terminal.control/debug/log/VT100Backend"; //$NON-NLS-1$
	/**	@since 5.2 */
	public static final String TRACE_DEBUG_LOG_HOVER = "org.eclipse.terminal.control/debug/log/hover"; //$NON-NLS-1$

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("org.eclipse.terminal"); //$NON-NLS-1$

	private static boolean underTest = false;

	/**
	 * When underTest we want exception that are deep inside the code to be surfaced to the test
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void setUnderTest(boolean underTest) {
		Logger.underTest = underTest;
	}

	/**
	 * Checks whether a debug option (formerly an Eclipse trace option) is enabled.
	 * An option is enabled when the system property of the same name is <code>true</code>.
	 * @param option one of the <code>TRACE_*</code> constants
	 * @return <code>true</code> if the option is enabled
	 */
	public static boolean isDebugOptionEnabled(String option) {
		return Boolean.getBoolean(option);
	}

	/**
	 * Encodes a String such that non-printable control characters are
	 * converted into user-readable escape sequences for logging.
	 * @param message String to encode
	 * @return encoded String
	 */
	public static final String encode(String message) {
		boolean encoded = false;
		StringBuffer buf = new StringBuffer(message.length() + 32);
		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);
			switch (c) {
			case '\\':
			case '\'':
				buf.append('\\');
				buf.append(c);
				encoded = true;
				break;
			case '\r':
				buf.append('\\');
				buf.append('r');
				encoded = true;
				break;
			case '\n':
				buf.append('\\');
				buf.append('n');
				encoded = true;
				break;
			case '\t':
				buf.append('\\');
				buf.append('t');
				encoded = true;
				break;
			case '\f':
				buf.append('\\');
				buf.append('f');
				encoded = true;
				break;
			case '\b':
				buf.append('\\');
				buf.append('b');
				encoded = true;
				break;
			default:
				if (c <= '\u000f') {
					buf.append('\\');
					buf.append('x');
					buf.append('0');
					buf.append(Integer.toHexString(c));
					encoded = true;
				} else if (c >= ' ' && c < '\u007f') {
					buf.append(c);
				} else if (c <= '\u00ff') {
					buf.append('\\');
					buf.append('x');
					buf.append(Integer.toHexString(c));
					encoded = true;
				} else {
					buf.append('\\');
					buf.append('u');
					if (c <= '\u0fff') {
						buf.append('0');
					}
					buf.append(Integer.toHexString(c));
					encoded = true;
				}
			}
		}
		if (encoded) {
			return buf.toString();
		}
		return message;
	}

	/**
	 * Checks if trace logging is enabled.
	 * @return true if logging is enabled.
	 */
	public static final boolean isLogEnabled() {
		return LOG.isTraceEnabled();
	}

	/**
	 * Logs the specified message at TRACE level.
	 *
	 * @param message           A String containing the message to log.
	 */
	public static final void log(String message) {
		LOG.trace(message);
	}

	/**
	 * Writes an error message to the log
	 * @since 5.2
	 */
	public static final void logError(String message) {
		LOG.error(message);
	}

	/**
	 * Writes a warning with exception to the log
	 */
	public static final void logWarning(String message, Throwable t) {
		LOG.warn(message, t);
	}

	/**
	 * Writes an exception to the log
	 */
	public static final void logException(Exception ex) {
		if (underTest) {
			throw new RuntimeException("Terminal Under Test - examine cause for real failure", ex); //$NON-NLS-1$
		}
		LOG.error(ex.getMessage(), ex);
	}
}
