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
package org.measure.platform.restapi.measure;

import java.util.List;

import javax.inject.Inject;

import org.measure.platform.core.data.api.IMeasureInstanceService;
import org.measure.platform.core.measurement.api.IMeasurementStorage;
import org.measure.platform.restapi.measure.dto.MeasureInstancePage;
import org.measure.platform.restapi.measure.dto.MeasurementQuery;
import org.measure.platform.restapi.measure.dto.MeasurementViewDataQuery;
import org.measure.smm.measure.api.IMeasurement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

@RestController
@RequestMapping(value = "api/measurement")
public class MeasurementResource {
	
	@Inject
	private IMeasurementStorage measurementStorage;
	

	/**
	 * Post /find find Measurements.
	 * @param : MeasurementQuery : Query
	 * @return the ResponseEntity with status 200 (OK) and the list of projects
	 *         in body
	 */
	@PostMapping("/find")
	@Timed
	public List<IMeasurement> getMeasurements(@RequestBody MeasurementQuery query) {
		return	this.measurementStorage.getMeasurementPage(query.getMeasureInstance(), query.getPageSize(), query.getPage(), query.getQuery());	
	}
	
	
	@PutMapping("/get-view-data")
	@Timed
	public MeasureInstancePage getMeasurementViewData(@RequestBody MeasurementViewDataQuery viewDataQuery) {		
		MeasureInstancePage result = new MeasureInstancePage();
		result.setMeasurements(this.measurementStorage.getMeasurementPage(viewDataQuery.getQuery().getMeasureInstance(), viewDataQuery.getQuery().getPageSize(), viewDataQuery.getQuery().getPage(), viewDataQuery.getQuery().getQuery()));
		result.setView(viewDataQuery.getView());
		result.setPage(viewDataQuery.getQuery().getPage());
		result.setPageSize(viewDataQuery.getQuery().getPageSize());	
		return	result;	
	}
	
    @GetMapping("/last-value/{measureInstance}")
    @Timed
    public IMeasurement getProject(@PathVariable String measureInstance) {
    	return measurementStorage.getLastMeasurement(measureInstance);
    }
}
