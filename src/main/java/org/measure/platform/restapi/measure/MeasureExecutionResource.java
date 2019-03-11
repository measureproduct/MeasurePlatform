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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.measure.platform.core.data.api.IMeasureInstanceService;
import org.measure.platform.core.data.entity.MeasureInstance;
import org.measure.platform.service.analysis.api.IAlertEngineService;
import org.measure.platform.service.analysis.data.alert.AlertData;
import org.measure.platform.service.analysis.data.alert.AlertProperty;
import org.measure.platform.service.analysis.data.alert.AlertType;
import org.measure.platform.service.smmengine.api.ILoggerService;
import org.measure.platform.service.smmengine.api.IMeasureExecutionService;
import org.measure.platform.service.smmengine.api.ISchedulingService;
import org.measure.smm.log.MeasureLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

@RestController
@RequestMapping(value = "api/measure-instance/sheduling")
public class MeasureExecutionResource {
    @Inject
    private IMeasureExecutionService measureExecutionService;

    @Inject
    private ISchedulingService shedulingService;

    @Inject
    private IMeasureInstanceService instanceService;

	@Inject
	private IAlertEngineService alertEngineService;
	
    @Inject
    private ILoggerService logger;

    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public Boolean startMeasureSheduling(@RequestParam("id") String id) {
        if (id.matches("\\d+")) {
            Long instanceId = Long.valueOf(id);
            if (!shedulingService.isShedule(instanceId)) {
                MeasureInstance measure = instanceService.findOne(instanceId);

        		AlertData alert = new AlertData();
        		alert.setAlertType(AlertType.MEASURE_SCHEDULED.name());
        		alert.setProjectId(measure.getProject().getId());		
        		alert.getProperties().add(new AlertProperty(AlertType.MEASURE_SCHEDULED.getResponsProperties().get(0), measure.getInstanceName()));
        		alertEngineService.alert(alert);
        		
                return shedulingService.scheduleMeasure(measure);
            }
        }
        return null;
    }

    @RequestMapping(value = "/stop", method = RequestMethod.GET)
    public Boolean stopMeasureSheduling(@RequestParam("id") String id) {
        if (id.matches("\\d+")) {
            Long instanceId = Long.valueOf(id);
            if (shedulingService.isShedule(instanceId)) {
                shedulingService.removeMeasure(instanceId);
                
                MeasureInstance measure = instanceService.findOne(instanceId);
                
        		AlertData alert = new AlertData();
        		alert.setAlertType(AlertType.MEASURE_UNSCHEDULED.name());
        		alert.setProjectId(measure.getProject().getId());		
        		alert.getProperties().add(new AlertProperty(AlertType.MEASURE_UNSCHEDULED.getResponsProperties().get(0), measure.getInstanceName()));
        		alertEngineService.alert(alert);
            }
        }
        return false;
    }

    @RequestMapping(value = "/isshedule", method = RequestMethod.GET)
    public Boolean isSheduledMeasure(@RequestParam("id") String id) {
        if (id.matches("\\d+")) {
            Long instanceId = Long.valueOf(id);
            return shedulingService.isShedule(instanceId);
        }
        return false;
    }

    @Timed
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ResponseEntity<MeasureLog> testMeasure(@RequestParam("id") String id) {
        if (id.matches("\\d+")) {
            Long instanceId = Long.valueOf(id);
            MeasureLog measurement = measureExecutionService.testMeasure(instanceId);
            return Optional.ofNullable(measurement).map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Timed
    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    public ResponseEntity<MeasureLog> executeMeasure(@RequestParam("id") String id) {
        if (id.matches("\\d+")) {
            Long instanceId = Long.valueOf(id);
            MeasureLog measurement = measureExecutionService.executeMeasure(instanceId);
            logger.addMeasureExecutionLog(measurement);
            return Optional.ofNullable(measurement).map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    
    @Timed
    @RequestMapping(value = "/externalexecution", method = RequestMethod.GET)
    public ResponseEntity<MeasureLog> executeMeasure(@RequestParam("id") String id,@RequestParam("date") String date,@RequestParam("dateField") String dateField) {
        if (id.matches("\\d+")) {
            Long instanceId = Long.valueOf(id);
            
            String format = "yyyy-MM-dd'T'HH:mm:ssz";
            SimpleDateFormat parser=new SimpleDateFormat(format);
            
            Date logDate;
			try {
				logDate = parser.parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
            
            MeasureLog measurement = measureExecutionService.executeMeasure(instanceId,logDate,dateField);
            logger.addMeasureExecutionLog(measurement);
            return Optional.ofNullable(measurement).map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}
