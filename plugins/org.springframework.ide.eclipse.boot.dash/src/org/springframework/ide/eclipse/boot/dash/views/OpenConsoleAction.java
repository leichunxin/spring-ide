/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.util.LaunchUtil;

@SuppressWarnings("restriction")
public class OpenConsoleAction extends AbstractBootDashAction {

	public OpenConsoleAction(MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(selection, ui);
		this.setText("Open Console");
		this.setToolTipText("Open Console");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console.gif"));
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console_disabled.gif"));
	}

	@Override
	public void run() {
		final Collection<BootDashElement> selecteds = getSelectedElements();

		Display.getCurrent().asyncExec(new Runnable() {

			@Override
			public void run() {
				doShowConsoles(selecteds);
			}
		});
	}

	protected void doShowConsoles(Collection<BootDashElement> selectedElements) {

		if (selectedElements != null) {

			Iterator<BootDashElement> it = selectedElements.iterator();

			// Show first element for now
			if (it.hasNext()) {
				BootDashElement element = selectedElements.iterator().next();
				IConsoleManager manager = ConsolePlugin.getDefault()
						.getConsoleManager();

				IConsole appConsole = null;

				IConsole[] activeConsoles = manager.getConsoles();
				if (activeConsoles != null) {
					List<ILaunch> launches = LaunchUtil.getLaunches(element
							.getProject());

					for (ILaunch launch : launches) {

						IProcess[] processes = launch.getProcesses();

						if (processes != null) {

							int total = processes.length;

							for (int i = 0; appConsole == null && i < total; i++) {
								appConsole = getConsole(processes[i],
										activeConsoles);
							}

							if (appConsole != null) {
								break;
							}
						}
					}
				}

				if (appConsole != null) {
					manager.showConsoleView(appConsole);
				} else {
					ui.errorPopup("Open Console",
							"Failed to open console for: "
									+ element.getName()
									+ ". Either a process console may not exist or the application is not running.");
				}
			}
		}
	}

	/**
	 * @return IConsole that is associated with the given process. Null if no
	 *         console is found.
	 */
	protected IConsole getConsole(IProcess process, IConsole[] consoles) {

		for (IConsole console : consoles) {

			if (console instanceof ProcessConsole) {
				IProcess consoleProcess = ((ProcessConsole) console)
						.getProcess();

				if (consoleProcess != null && consoleProcess.equals(process)) {
					return console;
				}
			}
		}
		return null;
	}
}
