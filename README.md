# Eclipse Terminal Control, plain SWT/JFace fork

Fork of `org.eclipse.terminal.control` (Eclipse Platform Terminal, formerly
TM Terminal / `org.eclipse.tm.terminal.control`) for use in plain SWT/JFace
desktop applications without OSGi, Equinox or the Eclipse workbench.

Upstream: <https://github.com/eclipse-platform/eclipse.platform>, directory
`terminal/bundles/org.eclipse.terminal.control`. Package names are kept so
that upstream changes can be merged with ordinary diff/patch tools.

License: EPL-2.0 (see `about.html`). Original copyright headers are preserved.

## Dependencies

* SWT (platform specific artifact, e.g. `org.eclipse.swt.gtk.linux.x86_64`)
* `org.eclipse.jface`, `org.eclipse.equinox.common`, `org.eclipse.core.commands`
  (the latter two are JFace's own dependencies; the fork uses only `Assert`,
  `IAdaptable` and `ListenerList` from them)
* `slf4j-api`

Build with `mvn package`. The pom pins the Linux GTK x86_64 SWT fragment for
compilation; override `swt.artifact` for other platforms.

## Usage

```java
AbstractTerminalConnector impl = new MyConnector();          // your transport
ITerminalConnector connector = TerminalConnectors.create("my", "My connector", () -> impl);

ITerminalViewControl terminal = TerminalViewControlFactory.makeControl(
        listener, parentComposite, new ITerminalConnector[] { connector }, true);
terminal.setConnector(connector);
terminal.connectTerminal();
```

A connector extends `AbstractTerminalConnector`: in `connect()` it stores the
`ITerminalControl`, sets the state to `CONNECTED` and writes remote output to
`control.getRemoteToTerminalOutputStream()`; `getTerminalToRemoteStream()`
returns the stream that receives keyboard input. See
`src/test/java/org/eclipse/terminal/demo/LoopbackConnector.java` and
`TerminalDemo.java` for a complete, runnable example.

### Preferences

`useCommonPrefs == true` makes the widget use the shared store returned by
`TerminalPreferences.getDefault()`. Install your own JFace `IPreferenceStore`
with `TerminalPreferences.setDefault(store)` (defaults are initialized into it),
or pass a store directly to the `makeControl` overload that takes an
`IPreferenceStore`. `TerminalPreferencePage` is a plain JFace
`FieldEditorPreferencePage` that can be shown in a `PreferenceDialog`.

### Logging

Everything goes to the SLF4J logger `org.eclipse.terminal`. Trace output of
the emulator is at TRACE level. The former Eclipse trace options are system
properties, e.g. `-Dorg.eclipse.terminal.control/debug/log/char=true`.

### Key bindings

Local copy/paste: Ctrl+Insert or Ctrl+Shift+C, Shift+Insert or Ctrl+Shift+V
(plus Cmd+C / Cmd+V on macOS). Everything else goes to the terminal.

## Changes relative to upstream

Removed:

* `TerminalPlugin` (bundle activator), `plugin.xml`, extension point schema,
  help contexts, workbench contexts/commands/bindings, theme font definition.
* `TerminalConnectorExtension` (extension registry lookup). Replaced by
  `TerminalConnectors.create(...)`.

Changed:

* `Logger`: SLF4J instead of the Eclipse log and trace file.
* `TerminalMessages`: `ResourceBundle` instead of `org.eclipse.osgi.util.NLS`;
  adds `bind()`.
* `TerminalPreferenceInitializer`: static method filling a JFace
  `IPreferenceStore` instead of `AbstractPreferenceInitializer`. New public
  `TerminalPreferences` holder replaces the activator's store.
* `TerminalPreferencePage`: no longer implements `IWorkbenchPreferencePage`.
* `VT100TerminalControl`: reader `Job` replaced by a daemon thread; workbench
  context/binding/handler service calls and help registration removed;
  display lookup via the widget's `Display`.
* `EditActionAccelerators`: hardcoded accelerators instead of
  `IBindingService`.
* `SystemDefaultColors`: SWT system colors instead of the text editor
  preference store and `ColorUtil`.
* `CommandInputFieldWithHistory`: JFace `ContentProposalAdapter` instead of
  `ContentAssistCommandAdapter`.
* `VirtualCanvas`, `TextLineRenderer`, `AbstractTextCanvasModel`,
  `VT100Emulator`, `TerminalStyle`: `Platform.getDebugBoolean` replaced by
  `Logger.isDebugOptionEnabled`.
* `TerminalConnector` (internal wrapper): no `Platform.getAdapterManager()`.

## Syncing with upstream

```
git clone --depth 1 --filter=blob:none --sparse https://github.com/eclipse-platform/eclipse.platform.git
cd eclipse.platform && git sparse-checkout set terminal
diff -ru terminal/bundles/org.eclipse.terminal.control/src/org <fork>/src/main/java/org
```
