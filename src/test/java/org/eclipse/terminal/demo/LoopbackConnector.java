package org.eclipse.terminal.demo;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.terminal.connector.ITerminalControl;
import org.eclipse.terminal.connector.TerminalState;
import org.eclipse.terminal.connector.provider.AbstractTerminalConnector;

/**
 * Minimal connector that echoes everything typed back into the terminal.
 * Serves as a template for a real connector (SSH, agent tunnel, ...).
 */
public class LoopbackConnector extends AbstractTerminalConnector {
	private ITerminalControl control;

	@Override
	public void connect(ITerminalControl control) {
		super.connect(control);
		this.control = control;
		control.setCharset(StandardCharsets.UTF_8);
		control.setState(TerminalState.CONNECTED);
		write("\u001b[1;32mLoopback terminal ready.\u001b[0m Type something, it will be echoed.\r\n"
				+ "\u001b[31mred\u001b[0m \u001b[33myellow\u001b[0m \u001b[34mblue\u001b[0m \u001b[7minverse\u001b[0m\r\n$ ");
	}

	@Override
	protected void doDisconnect() {
		control = null;
	}

	@Override
	public OutputStream getTerminalToRemoteStream() {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				if (b == '\r') {
					LoopbackConnector.this.write("\r\n$ ");
				} else {
					LoopbackConnector.this.write(String.valueOf((char) b));
				}
			}
		};
	}

	@Override
	public String getSettingsSummary() {
		return "loopback";
	}

	@Override
	public boolean isLocalEcho() {
		return false;
	}

	void write(String s) {
		ITerminalControl c = control;
		if (c == null) {
			return;
		}
		try {
			c.getRemoteToTerminalOutputStream().write(s.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
