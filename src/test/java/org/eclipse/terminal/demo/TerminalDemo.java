package org.eclipse.terminal.demo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.terminal.connector.ITerminalConnector;
import org.eclipse.terminal.connector.TerminalConnectors;
import org.eclipse.terminal.connector.TerminalState;
import org.eclipse.terminal.control.ITerminalListener;
import org.eclipse.terminal.control.ITerminalViewControl;
import org.eclipse.terminal.control.TerminalTitleRequestor;
import org.eclipse.terminal.control.TerminalViewControlFactory;

/**
 * Plain SWT application embedding the terminal widget.
 * <p>
 * With <code>-Ddemo.screenshot=/path/file.png</code> the demo feeds some
 * input, saves a screenshot of the shell and exits (used for headless checks).
 */
public class TerminalDemo {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("SWT Terminal demo");
		shell.setLayout(new FillLayout());
		shell.setSize(800, 400);

		ITerminalListener listener = new ITerminalListener() {
			@Override
			public void setState(TerminalState state) {
				System.out.println("state: " + state);
			}

			@Override
			public void setTerminalSelectionChanged() {
			}

			@Override
			public void setTerminalTitle(String title, TerminalTitleRequestor requestor) {
				System.out.println("title: " + title);
			}
		};

		LoopbackConnector loopback = new LoopbackConnector();
		ITerminalConnector connector = TerminalConnectors.create("loopback", "Loopback", () -> loopback);
		ITerminalViewControl terminal = TerminalViewControlFactory.makeControl(listener, shell,
				new ITerminalConnector[] { connector }, true);
		terminal.setConnector(connector);
		terminal.connectTerminal();

		shell.open();
		terminal.setFocus();

		String screenshot = System.getProperty("demo.screenshot");
		if (screenshot != null) {
			display.timerExec(1500, () -> loopback.write("echo hello from the remote side\r\n$ "));
			display.timerExec(2000, () -> typeText(display, "ls -la\r"));
			display.timerExec(3000, () -> {
				saveScreenshot(shell, screenshot);
				System.out.println("buffer line limit: " + terminal.getBufferLineLimit());
				terminal.disposeTerminal();
				shell.close();
			});
		}

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/** Simulate typing through the OS event queue (exercises the key handler). */
	private static void typeText(Display display, String text) {
		for (char ch : text.toCharArray()) {
			Event down = new Event();
			down.type = SWT.KeyDown;
			down.character = ch;
			down.keyCode = ch == '\r' ? SWT.CR : Character.toLowerCase(ch);
			display.post(down);
			Event up = new Event();
			up.type = SWT.KeyUp;
			up.character = ch;
			up.keyCode = down.keyCode;
			display.post(up);
		}
	}

	private static void saveScreenshot(Shell shell, String path) {
		GC gc = new GC(shell);
		Image image = new Image(shell.getDisplay(), shell.getClientArea().width, shell.getClientArea().height);
		gc.copyArea(image, 0, 0);
		gc.dispose();
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { image.getImageData() };
		loader.save(path, SWT.IMAGE_PNG);
		image.dispose();
		System.out.println("screenshot saved: " + path);
	}
}
