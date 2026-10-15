/*******************************************************************************
 * Copyright (c) 2025 Raden Solutions and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.terminal.control;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.terminal.internal.preferences.TerminalPreferenceInitializer;

/**
 * Holder of the shared terminal preference store.
 * <p>
 * Replaces the preference store of the former bundle activator. Applications
 * may install their own JFace {@link IPreferenceStore} with
 * {@link #setDefault(IPreferenceStore)} (for example a store backed by the
 * application's settings file); defaults are installed into it by
 * {@link #initializeDefaults(IPreferenceStore)}. If no store is installed, an
 * in-memory {@link PreferenceStore} is created on first use.
 */
public final class TerminalPreferences {
	private static IPreferenceStore defaultStore;

	private TerminalPreferences() {
	}

	/**
	 * Install the terminal default values into the given store. This should be
	 * called once for any store that is passed to the terminal widget.
	 *
	 * @param store the store to initialize
	 * @return the same store, for chaining
	 */
	public static IPreferenceStore initializeDefaults(IPreferenceStore store) {
		TerminalPreferenceInitializer.initializeDefaultPreferences(store);
		return store;
	}

	/**
	 * @return the shared preference store used by terminals created with
	 *         <code>useCommonPrefs == true</code>.
	 */
	public static synchronized IPreferenceStore getDefault() {
		if (defaultStore == null) {
			defaultStore = initializeDefaults(new PreferenceStore());
		}
		return defaultStore;
	}

	/**
	 * Install the shared preference store. Defaults are initialized in the
	 * given store.
	 *
	 * @param store the store to use, or <code>null</code> to revert to an
	 *              in-memory store
	 */
	public static synchronized void setDefault(IPreferenceStore store) {
		defaultStore = store == null ? null : initializeDefaults(store);
	}
}
