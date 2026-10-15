/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.terminal.internal.model;

import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Wrapper class to get standard colors from Eclipse trying to match existing theme where possible
 * by using standard editor colors.
 *
 * This class has an implied and optional dependency on org.eclipse.ui.editors bundle by reading
 * that bundles preferences.
 */
public enum SystemDefaultColors implements Supplier<RGB> {

	/**
	 * Standard text foreground. Typically black in Light theme.
	 */
	FOREGROUND("Foreground", "COLOR_LIST_FOREGROUND", new RGB(0, 0, 0)), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Standard text background. Typically white in Light theme.
	 */
	BACKGROUND("Background", "COLOR_LIST_BACKGROUND", new RGB(255, 255, 255)), //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Selection foreground. Typically white in Light theme.
	 */
	SELECTION_FOREGROUND("SelectionForeground", "COLOR_LIST_SELECTION_TEXT", //$NON-NLS-1$ //$NON-NLS-2$
			new RGB(255, 255, 255)),

	/**
	 * Selection background. Typically blue in Light theme.
	 */
	SELECTION_BACKGROUND("SelectionBackground", "COLOR_LIST_SELECTION", new RGB(74, 144, 9)); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * SWT Name of Color
	 *
	 * Values from SWT
	 */
	private final String swtColor;

	/**
	 * Preference name for color.
	 *
	 * Values from org.eclipse.ui.texteditor.AbstractTextEditor....
	 */
	private final String editorColor;

	/** If all else fails, use this standard color */
	private final RGB fallbackColor;

	SystemDefaultColors(String editorColor, String swtColor, RGB rgb) {
		this.editorColor = editorColor;
		this.swtColor = swtColor;
		this.fallbackColor = rgb;
	}

	/**
	 * Get the color for this enum value.
	 *
	 * @return the RGB color or a non-<code>null</code> color as a fallback.
	 */
	@Override
	public RGB get() {
		RGB rgb = null;
		Display display = Display.getCurrent();
		if (display != null) {
			try {
				int id = SWT.class.getField(swtColor).getInt(null);
				Color color = display.getSystemColor(id);
				if (color != null) {
					rgb = color.getRGB();
				}
			} catch (ReflectiveOperationException | RuntimeException e) {
				// fall through to the fallback color
			}
		}

		if (rgb == null) {
			rgb = fallbackColor;
		}

		return rgb;
	}
}
