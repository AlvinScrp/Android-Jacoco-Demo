/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core;

import java.util.ResourceBundle;

/**
 * Static Meta information about JaCoCo.
 */
public final class JaCoCo {

	/** Qualified build version of the JaCoCo core library. */
	public static final String VERSION;

	/** Absolute URL of the current JaCoCo home page */
	public static final String HOMEURL;

	/** Name of the runtime package of this build */
	public static final String RUNTIMEPACKAGE;

	static {
		final ResourceBundle bundle = ResourceBundle
				.getBundle("org.jacoco.core.jacoco");
		VERSION = "0.8.7";
		HOMEURL = "http://www.jacoco.org/jacoco";
		RUNTIMEPACKAGE = "org.jacoco.agent.rt.internal";
	}

	private JaCoCo() {
	}

}
