/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 * Martin Oberhuber (Wind River) - [436612] Restore Eclipse 3.4 compatibility
 *******************************************************************************/
package org.eclipse.terminal.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.terminal.internal.preferences.TerminalColorPresets.Preset;
import org.eclipse.terminal.model.TerminalColor;

/**
 * Terminal Preference Initializer.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 */
public final class TerminalPreferenceInitializer {

	private TerminalPreferenceInitializer() {
	}

	/**
	 * Install the terminal default values into the given preference store.
	 */
	public static void initializeDefaultPreferences(IPreferenceStore defaultPrefs) {
		defaultPrefs.setDefault(ITerminalConstants.PREF_INVERT_COLORS, ITerminalConstants.DEFAULT_INVERT_COLORS);
		defaultPrefs.setDefault(ITerminalConstants.PREF_BUFFERLINES, ITerminalConstants.DEFAULT_BUFFERLINES);
		defaultPrefs.setDefault(ITerminalConstants.PREF_RESTORE_TERMINALS, ITerminalConstants.DEFAULT_PREF_RESTORE_TERMINALS);
		defaultPrefs.setDefault(ITerminalConstants.PREF_FONT_DEFINITION, ITerminalConstants.DEFAULT_FONT_DEFINITION);

		Preset defaultPresets = TerminalColorPresets.INSTANCE.getDefaultPreset();
		TerminalColor[] colors = TerminalColor.values();
		for (TerminalColor color : colors) {
			defaultPrefs.setDefault(ITerminalConstants.getPrefForTerminalColor(color),
					StringConverter.asString(defaultPresets.getRGB(color)));
		}
	}
}
