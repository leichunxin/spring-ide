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

import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

/**
 * An implementation of 'UserInteractions' that uses real Dialogs, for use in 'production'.
 *
 * @author Kris De Volder
 */
public class DefaultUserInteractions implements UserInteractions {

	public interface UIContext {
		Shell getShell();
	}

	private UIContext context;

	public DefaultUserInteractions(UIContext context) {
		this.context = context;
	}

	@Override
	public ILaunchConfiguration chooseConfigurationDialog(String dialogTitle, String message, List<ILaunchConfiguration> configs) {
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		try {
			ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
			dialog.setElements(configs.toArray());
			dialog.setTitle(dialogTitle);
			dialog.setMessage(message);
			dialog.setMultipleSelection(false);
			int result = dialog.open();
			labelProvider.dispose();
			if (result == Window.OK) {
				return (ILaunchConfiguration) dialog.getFirstResult();
			}
			return null;
		} finally {
			labelProvider.dispose();
		}
	}

	private Shell getShell() {
		return context.getShell();
	}

	@Override
	public IType chooseMainType(final IType[] mainTypes, final String dialogTitle, final String message) {
		if (mainTypes.length==1) {
			return mainTypes[0];
		} else if (mainTypes.length>0) {
			//Take care the UI interactions don't bork if called from non-ui thread.
			final LiveVariable<IType> chosenType = new LiveVariable<IType>();
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
					try {
						ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
						dialog.setElements(mainTypes);
						dialog.setTitle(dialogTitle);
						dialog.setMessage(message);
						dialog.setMultipleSelection(false);
						int result = dialog.open();
						labelProvider.dispose();
						if (result == Window.OK) {
							chosenType.setValue((IType) dialog.getFirstResult());
						}
					} finally {
						labelProvider.dispose();
					}
				}
			});
			return chosenType.getValue();
		}
		return null;
	}

	@Override
	public void errorPopup(final String title, final String message) {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(getShell(), title, message);
			}
		});
	}

	@Override
	public void openLaunchConfigurationDialogOnGroup(final ILaunchConfiguration conf, final String launchGroup) {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				IStructuredSelection selection = new StructuredSelection(new Object[] {conf});
				DebugUITools.openLaunchConfigurationDialogOnGroup(getShell(), selection, launchGroup);
			}
		});
	}

}
