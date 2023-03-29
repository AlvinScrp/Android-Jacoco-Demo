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
package org.jacoco.report.internal.html.table;

import org.jacoco.core.analysis.CounterComparator;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.report.internal.ReportOutputFolder;
import org.jacoco.report.internal.html.HTMLElement;
import org.jacoco.report.internal.html.resources.Resources;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 分数形式 coveredCount / totalCount
 */
public class FractionColumn implements IColumnRenderer {

	private final CounterEntity entity;

	private final NumberFormat integerFormat;

	private final Comparator<ITableItem> comparator;

	public FractionColumn(final CounterEntity entity, final Locale locale) {
		this.entity = entity;
		this.integerFormat = NumberFormat.getIntegerInstance(locale);
		comparator = new TableItemComparator(
				CounterComparator.COVEREDITEMS.reverse().on(entity).second(
						CounterComparator.TOTALITEMS.reverse().on(entity)));
	}

	public boolean init(final List<? extends ITableItem> items,
			final ICoverageNode total) {
		return true;
	}

	public void footer(final HTMLElement td, final ICoverageNode total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		cell(td, total);
	}

//	public void footer(final HTMLElement td, final ICoverageNode total,
//					   final Resources resources, final ReportOutputFolder base)
//			throws IOException {
//		final ICounter counter = total.getCounter(entity);
//		td.text(integerFormat.format(counter.getTotalCount()-counter.getMissedCount()));
//		td.text(" / ");
//		td.text(integerFormat.format(counter.getTotalCount()));
//	}

	public void item(final HTMLElement td, final ITableItem item,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		cell(td, item.getNode());
	}

	private void cell(final HTMLElement td, final ICoverageNode node)
			throws IOException {
		final ICounter counter = node.getCounter(entity);
		final  int covered = counter.getCoveredCount();
		final int total = counter.getTotalCount();
		td.text(integerFormat.format(covered));
		td.text("/");
		td.text(integerFormat.format(total));
	}

	public Comparator<ITableItem> getComparator() {
		return comparator;
	}

}
