/*******************************************************************************
 * Copyright (C) 2019 Softeam
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.measure.platform.core.measurement.impl;

public class IndexFormat {

	public static final String ES_BASE_INDEX = "measure.default.init";
	public static final String KIBANA_BASE_INDEX = "measure.*";
	public static final String PREFIX_INDEX = "measure.";

	public static String getMeasureInstanceIndex(String instanceName) {
		return IndexFormat.PREFIX_INDEX + instanceName.toLowerCase();
	}
}
