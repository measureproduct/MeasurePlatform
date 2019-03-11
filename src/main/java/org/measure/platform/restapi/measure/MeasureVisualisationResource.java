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

import org.measure.platform.core.catalogue.api.IMeasureCatalogueService;
import org.measure.platform.core.catalogue.api.IMeasureVisaulisationManagement;
import org.measure.platform.core.data.api.IMeasureInstanceService;
import org.measure.platform.core.data.api.IMeasureViewService;
import org.measure.platform.core.data.entity.MeasureInstance;
import org.measure.platform.core.data.entity.MeasureView;
import org.measure.smm.measure.model.SMMMeasure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/measure-visualisation")
public class MeasureVisualisationResource {

	@Value("${measure.kibana.adress}")
	private String kibanaAdress;

	@Inject
	private IMeasureVisaulisationManagement visualisationManagement;

	@Inject
	private IMeasureInstanceService instanceService;

	@Inject
	private IMeasureViewService measureViewService;

	@Inject
	private IMeasureCatalogueService measureCatalogueService;

	@RequestMapping(value = "/create-default", method = RequestMethod.GET)
	public MeasureView createDefaultVisualisation(@RequestParam("id") String id) {
		if (id.matches("\\d+")) {
			Long instanceId = Long.valueOf(id);
			MeasureInstance mInstance = instanceService.findOne(instanceId);
			
			List<MeasureView> defaultViews =  measureViewService.findDefaulsByMeasureInstance(mInstance.getId());
			
			if (defaultViews.size() == 0) {
				SMMMeasure measure = measureCatalogueService.getMeasure(null,mInstance.getMeasureName());
				List<MeasureView> views = visualisationManagement.createDefaultMeasureView(measure, instanceId);
				for(MeasureView view : views) {
					measureViewService.save(view);
				}
		
				return views.get(0);
			}
		}
		return null;
	}
	
	@RequestMapping(value = "/get-default", method = RequestMethod.GET)
	public MeasureView getDefaultVisualisation(@RequestParam("id") String id) {
		if (id.matches("\\d+")) {
			Long instanceId = Long.valueOf(id);
			MeasureInstance mInstance = instanceService.findOne(instanceId);
			
			List<MeasureView> defaultViews =  measureViewService.findDefaulsByMeasureInstance(mInstance.getId());
			
			if (defaultViews.size() > 0) {
				return defaultViews.get(0);
			}
		}
		return null;
	}
	
	
	@RequestMapping(value = "/delete-default", method = RequestMethod.DELETE)
	public void deleteDefaultVisualisation(@RequestParam("id") String id) {
		if (id.matches("\\d+")) {
			Long instanceId = Long.valueOf(id);
			MeasureInstance mInstance = instanceService.findOne(instanceId);		
			List<MeasureView> defaultViews =  measureViewService.findDefaulsByMeasureInstance(mInstance.getId());		
			for(MeasureView defaults : defaultViews) {
				measureViewService.delete(defaults.getId());
			}	
		}
	}
	

	@RequestMapping(value = "/create-view", method = RequestMethod.GET)
	public MeasureView createVisualisation(@RequestParam("id") String id, @RequestParam("view") String viewName) {
		if (id.matches("\\d+")) {
			Long instanceId = Long.valueOf(id);
			MeasureInstance mInstance = instanceService.findOne(instanceId);
			List<MeasureView> defaultViews =  measureViewService.findDefaulsByMeasureInstance(mInstance.getId());

			if (defaultViews.size() == 0) {
				SMMMeasure measure = measureCatalogueService.getMeasure(null,mInstance.getMeasureName());
				MeasureView view = visualisationManagement.createDefaultMeasureView(measure, instanceId, viewName);
				measureViewService.save(view);
				return view;
			}
		}
		return null;
	}
}
