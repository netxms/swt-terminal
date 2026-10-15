# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A fork of Eclipse Platform's `org.eclipse.terminal.control` bundle (VT100 terminal widget) stripped of
OSGi/Equinox/workbench dependencies so it can be embedded in plain SWT/JFace applications. It is a
plain Maven jar, Java 17. Package names (`org.eclipse.terminal.*`) are deliberately kept identical
to upstream so changes can be merged with `diff`/`patch`; do not rename or move packages.

`UPSTREAM` records the upstream commit the sources were taken from; update it when syncing.
`README.md` has the full list of what was removed/changed relative to upstream and the sync recipe.
Keep that list current when making further de-Eclipse-ification changes.

## Build and run

```sh
mvn package                      # compile + jar (no unit tests exist)
mvn -o package                   # offline, once dependencies are cached
mvn package -Dswt.artifact=org.eclipse.swt.win32.win32.x86_64   # other SWT platform fragment
```

There is no test suite. `src/test/java` contains only a runnable demo (`TerminalDemo` plus a
`LoopbackConnector` that echoes input). To run it:

```sh
mvn -q test-compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt -Dmdep.includeScope=test
java -cp target/classes:target/test-classes:$(cat target/cp.txt) org.eclipse.terminal.demo.TerminalDemo
```

Headless smoke check (feeds input, saves a PNG of the shell, exits; needs `xvfb-run` on Linux):

```sh
xvfb-run -a java -Ddemo.screenshot=/path/out.png -cp target/classes:target/test-classes:$(cat target/cp.txt) org.eclipse.terminal.demo.TerminalDemo
```

Emulator trace output is enabled with system properties mirroring the old Eclipse trace options,
e.g. `-Dorg.eclipse.terminal.control/debug/log/char=true`; it goes to the SLF4J logger
`org.eclipse.terminal` at TRACE level (`slf4j-simple` is on the test classpath).

## Architecture

Data flows in two directions through `VT100TerminalControl` (`internal/emulator`), the one class
that implements all three control interfaces (`ITerminalViewControl` for the embedding app,
`ITerminalControl` for connectors, `ITerminalControlForText` for the emulator):

**Remote -> screen.** A connector writes bytes to `ITerminalControl.getRemoteToTerminalOutputStream()`,
which is the write side of an internal `PipedInputStream`. A daemon `ReaderThread` (replaces the
upstream Eclipse `Job`) pulls from it and drives `VT100Emulator`, which parses escape sequences and
mutates the text model via `IVT100EmulatorBackend`/`VT100EmulatorBackend`. The model is
`ITerminalTextData` (`internal/model`): `TerminalTextData` wraps a `TerminalTextDataFastScroll`
store, is wrapped in `SynchronizedTerminalTextData` by `TerminalTextDataFactory`, and hands out
`ITerminalTextDataSnapshot`s. The UI never reads the live model: `PollingTextCanvasModel`
(`internal/textcanvas`) polls a snapshot on a display timer and `TextCanvas` (extends
`GridCanvas` -> `VirtualCanvas` -> SWT `Canvas`) repaints changed lines through `TextLineRenderer`
and `StyleMap`.

**Keyboard -> remote.** `VT100TerminalControl.TerminalKeyHandler` translates SWT key events into
VT100 byte sequences and writes them to the connector's `getTerminalToRemoteStream()`, optionally
through `TerminalToRemoteInjectionOutputStream` (used by `CommandInputFieldWithHistory`).

**Connectors.** Applications subclass `connector.provider.AbstractTerminalConnector` and wrap it with
`TerminalConnectors.create(id, name, supplier)`, which returns the lazily-instantiating
`internal.connector.TerminalConnector` wrapper. This replaces the upstream extension-registry
lookup (`TerminalConnectorExtension`); there is no plugin.xml. `ISettingsStore`
(`InMemorySettingsStore`, `NullSettingsStore`) and `ISettingsPage`/`AbstractSettingsPage` are the
connector settings persistence/UI hooks.

**Preferences.** `control.TerminalPreferences` is the static holder that replaces the bundle
activator's store: `getDefault()` lazily creates a JFace `PreferenceStore` populated by
`TerminalPreferenceInitializer.initializeDefaultPreferences`; `setDefault(store)` installs an
application-provided store. Preference keys live in `internal/preferences/ITerminalConstants`;
`TerminalPreferencePage` is a plain JFace `FieldEditorPreferencePage`.

**Remaining Eclipse dependencies** are intentionally minimal: only `Assert`, `IAdaptable` and
`ListenerList` from `org.eclipse.equinox.common` (transitively `org.eclipse.core.commands`), plus
JFace. Do not introduce `org.eclipse.core.runtime.Platform`, `org.eclipse.ui.*` or OSGi APIs;
the fork substitutes `Logger.isDebugOptionEnabled`, SWT system colors (`SystemDefaultColors`)
and hardcoded accelerators (`EditActionAccelerators`) for them.

## Conventions

- Upstream Eclipse code style: tabs for indentation, `f`-prefixed fields, `//$NON-NLS-1$` markers
  on non-translatable strings. Keep the original EPL-2.0 copyright headers on upstream files.
- User-visible strings go through `TerminalMessages` (`ResourceBundle`-backed static fields,
  `bind()` for placeholders) with the `.properties` file under `src/main/resources`.
- Keep diffs against upstream small and local to ease future syncs; prefer adding new classes
  (as was done with `TerminalConnectors` and `TerminalPreferences`) over restructuring existing ones.
