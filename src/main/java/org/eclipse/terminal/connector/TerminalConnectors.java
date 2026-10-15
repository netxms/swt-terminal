/*******************************************************************************
 * Copyright (c) 2025 Raden Solutions and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.terminal.connector;

import java.util.function.Supplier;

import org.eclipse.terminal.connector.provider.AbstractTerminalConnector;
import org.eclipse.terminal.internal.connector.TerminalConnector;

/**
 * Creates {@link ITerminalConnector} instances from
 * {@link AbstractTerminalConnector} implementations.
 * <p>
 * Replaces the extension registry based
 * <code>TerminalConnectorExtension</code>: connectors are supplied
 * programmatically instead of being discovered through plugin.xml.
 */
public final class TerminalConnectors {
	private TerminalConnectors() {
	}

	/**
	 * Wrap a connector implementation. The implementation is created lazily on
	 * first use and {@link AbstractTerminalConnector#initialize()} is invoked.
	 *
	 * @param id      unique id of the connector
	 * @param name    user visible name
	 * @param hidden  <code>true</code> if the connector should not be offered in UI
	 * @param factory creates the connector implementation
	 * @return the connector
	 */
	public static ITerminalConnector create(String id, String name, boolean hidden,
			Supplier<? extends AbstractTerminalConnector> factory) {
		return new TerminalConnector(factory::get, id, name, hidden);
	}

	/**
	 * Wrap a connector implementation that is not hidden.
	 */
	public static ITerminalConnector create(String id, String name,
			Supplier<? extends AbstractTerminalConnector> factory) {
		return create(id, name, false, factory);
	}
}
