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
package org.measure.platform.service.smmengine.impl.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.measure.platform.core.catalogue.api.IMeasureCatalogueService;
import org.measure.platform.core.data.api.IMeasureInstanceService;
import org.measure.platform.core.data.api.IMeasurePropertyService;
import org.measure.platform.core.data.entity.MeasureInstance;
import org.measure.platform.core.data.entity.MeasureProperty;
import org.measure.platform.service.agent.api.IAgentManager;
import org.measure.platform.service.smmengine.api.ILoggerService;
import org.measure.platform.service.smmengine.api.IMeasureExecutionService;
import org.measure.platform.service.smmengine.api.ISchedulingService;
import org.measure.smm.log.MeasureLog;
import org.measure.smm.measure.api.IMeasure;
import org.measure.smm.remote.RemoteMeasureInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class SchedulingService implements ISchedulingService {
    @Autowired
    private TaskScheduler taskScheduler;

    @Inject
    private IMeasureExecutionService measureExecutionService;

    @Inject
    private IMeasureCatalogueService measureCatalogue;

    @Inject
    private IMeasureInstanceService measureInstanceService;

    @Inject
    private IMeasurePropertyService measurePropertyService;

    @Inject
    IAgentManager agentService;

    @Inject
    private ILoggerService logger;

    private Map<String, List<Long>> remotsJobs;

    private Map<Long, ScheduledFuture> jobs;

    @PostConstruct
    public void doSomething() {
        this.jobs = new HashMap<>();
        this.remotsJobs = new HashMap<>();
    }

    @Override
    public  Boolean scheduleMeasure(MeasureInstance measure) {
        if (measure.isIsShedule() != null && measure.isIsShedule() && measure.getShedulingExpression() != null
                && measure.getShedulingExpression().matches("\\d+")) {
            if (measure.isIsRemote()) {        
                if(!agentService.isAlive(measure.getRemoteLabel())){
                    return false;
                }
                scheduleRemoteMeasure(measure);
            } else {
                scheduleLocalExecution(measure);
            }
            return true;
        }
        return false;
    }

    @Override
    public List<RemoteMeasureInstance> getSheduledRemoteMeasure(String agentId) {
        List<RemoteMeasureInstance> result = new ArrayList<>();
        
        List<Long> instances =  this.remotsJobs.get(agentId);
        if(instances != null){
            for (Long instanceId : instances) {
                MeasureInstance instance = measureInstanceService.findOne(instanceId);
                RemoteMeasureInstance remoteM = new RemoteMeasureInstance();
                remoteM.setInstanceName(instance.getInstanceName());
                remoteM.setMeasureName(instance.getMeasureName());
                remoteM.setMeasureVersion(instance.getMeasureVersion());
                remoteM.setShedulingExpression(instance.getShedulingExpression());
                remoteM.setMeasureId(instance.getId());
        
                for (MeasureProperty prop : measurePropertyService.findByInstance(instance)) {
                    remoteM.getProperties().put(prop.getPropertyName(), prop.getPropertyValue());
                }
        
                result.add(remoteM);
        
            }
        }
        return result;
    }

    private void scheduleRemoteMeasure(MeasureInstance measure) {
        List<Long> measures = this.remotsJobs.get(measure.getRemoteLabel());
        if (measures == null) {
            measures = new ArrayList<>();
            this.remotsJobs.put(measure.getRemoteLabel(), measures);
        }
        measures.add(measure.getId());
    }

    private void scheduleLocalExecution(MeasureInstance measure) {
        Integer rate = Integer.valueOf(measure.getShedulingExpression());
        
    	String application = null;
		if(measure.getApplication() != null) {
			application = measure.getApplication().getApplicationType();
		}
        
        IMeasure measureImpl = measureCatalogue.getMeasureImplementation(application,measure.getMeasureName());
        
        ScheduledFuture job = taskScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
        
                MeasureLog log = measureExecutionService.executeMeasure(measure, measureImpl);
                logger.addMeasureExecutionLog(log);
        
                if (!log.isSuccess()) {
                    removeMeasure(measure.getId());
                }
        
            }
        }, rate);
        this.jobs.put(measure.getId(), job);
    }

    @Override
    public Boolean removeMeasure(Long measureInstanceId) {
        // Stop Measures executed Localy
        ScheduledFuture job = jobs.get(measureInstanceId);
        if (job != null) {
            job.cancel(true);
            this.jobs.remove(measureInstanceId);
        } else {
            // Stop Measures executed Remotely
        
            for (List<Long> agentMeasures : this.remotsJobs.values()) {
                if (agentMeasures.contains(measureInstanceId)) {
                    agentMeasures.remove(measureInstanceId);
                }
            }
        
        }
        return true;
    }

    @Override
    public Boolean isShedule(Long measureInstanceId) {
        if (jobs.containsKey(measureInstanceId)) {
            return true;
        }
        for (String agentId: this.remotsJobs.keySet()) {
            if (this.remotsJobs.get(agentId).contains(measureInstanceId)) {                    
                return agentService.isAlive(agentId);
            }
        }
        return false;
    }

}
