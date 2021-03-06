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
package org.springframework.ide.eclipse.boot.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.springframework.ide.eclipse.boot.core.BootActivator;

public class JavaProjectUtil {

	public static List<File> getNonSystemJarDependencies(IJavaProject jp, boolean reverse) {
		try {
			String[] paths = JavaRuntime.computeDefaultRuntimeClassPath(jp);
			if (paths!=null && paths.length>0) {
				LinkedList<File> jars = new LinkedList<File>();
				for (String path : paths) {
					if (path!=null) {
						File jar = new File(path);
						if (FileUtil.isJarFile(jar)) {
							if (reverse) {
								jars.addFirst(jar);
							} else {
								jars.add(jar);
							}
						}
					}
				}
				return jars;
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return Collections.emptyList();
	}

	/**
	 * Get IFile handle relative to project's default output folder.
	 */
	public static IFile getOutputFile(IJavaProject jp, String relativePath) {
		return getOutputFile(jp, new Path(relativePath));
	}

	public static IFile getOutputFile(IJavaProject jp, IPath relativePath) {
		try {
			IPath loc = jp.getOutputLocation().append(relativePath);
			String pname = loc.segment(0);
			return ResourcesPlugin.getWorkspace().getRoot().getProject(pname).getFile(loc.removeFirstSegments(1));
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	/**
	 * Retrieve the source folders in a given IProject. When IProject is
	 * not an accessible {@link IJavaProject} this returns an empty array.
	 */
	public static IContainer[] getSourceFolders(IProject p) {
		try {
			if (p!=null && p.isAccessible() && p.hasNature(JavaCore.NATURE_ID)) {
				return getSourceFolders(JavaCore.create(p));
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return new IContainer[0];
	}

	/**
	 * Retrieve the source folders in a given IJavaProject. Note that the source folders
	 * might not all be IFolder instances because it is possible for an IJavaProject's root to
	 * be a source folder. In this case the 'folder' will be an IProject instance instead of
	 * a IFolder.
	 */
	public static IContainer[] getSourceFolders(IJavaProject jp) {
		try {
			ArrayList<IContainer> sourceFolders = new ArrayList<IContainer>();
			IClasspathEntry[] cp = jp.getResolvedClasspath(true);
			for (IClasspathEntry cpe : cp) {
				try {
					if (cpe.getEntryKind()==IClasspathEntry.CPE_SOURCE) {
						IContainer sf = getProjectOrFolder(cpe.getPath());
						if (sf!=null && sf.exists()) {
							sourceFolders.add(sf);
						}
					}
				} catch (Exception e) {
					BootActivator.log(e);
				}
			}
			return sourceFolders.toArray(new IContainer[sourceFolders.size()]);
		} catch (Exception e) {
			BootActivator.log(e);
			return new IContainer[0];
		}
	}

	public static IContainer getProjectOrFolder(IPath path) {
		if (path.segmentCount()>1) {
			return ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
		} else if (path.segmentCount()==1) {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
		}
		return null;
	}

}
