/*******************************************************************************
 * Copyright (c) 2013, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.terminal.internal.emulator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;

/**
 * Key accelerators for local copy and paste in the terminal widget.
 * <p>
 * Without the Eclipse workbench there is no binding service to consult, so
 * the bindings formerly contributed through plugin.xml are hardcoded here:
 * Ctrl+Insert / Ctrl+Shift+C for copy, Shift+Insert / Ctrl+Shift+V for paste,
 * and additionally Command+C / Command+V on macOS.
 */
class EditActionAccelerators {
	private static final String COPY_COMMAND_ID = "org.eclipse.terminal.copy"; //$NON-NLS-1$
	private static final String PASTE_COMMAND_ID = "org.eclipse.terminal.paste"; //$NON-NLS-1$

	private final Map<Integer, String> commandIdsByAccelerator = new HashMap<>();

	private void load() {
		addAccelerator(COPY_COMMAND_ID, SWT.MOD1 | SWT.INSERT);
		addAccelerator(COPY_COMMAND_ID, SWT.MOD1 | SWT.MOD2 | 'C');
		addAccelerator(PASTE_COMMAND_ID, SWT.MOD2 | SWT.INSERT);
		addAccelerator(PASTE_COMMAND_ID, SWT.MOD1 | SWT.MOD2 | 'V');
		if ("cocoa".equals(SWT.getPlatform())) { //$NON-NLS-1$
			addAccelerator(COPY_COMMAND_ID, SWT.MOD1 | 'C');
			addAccelerator(PASTE_COMMAND_ID, SWT.MOD1 | 'V');
		}
	}

	private void addAccelerator(String commandId, int accelerator) {
		commandIdsByAccelerator.put(Integer.valueOf(accelerator), commandId);
	}

	boolean isCopyAction(int accelerator) {
		return isMatchingAction(accelerator, COPY_COMMAND_ID);
	}

	boolean isPasteAction(int accelerator) {
		return isMatchingAction(accelerator, PASTE_COMMAND_ID);
	}

	private boolean isMatchingAction(int accelerator, String commandId) {
		if (commandIdsByAccelerator.isEmpty()) {
			load();
		}
		return commandId.equals(commandIdsByAccelerator.get(Integer.valueOf(accelerator)));
	}
}
