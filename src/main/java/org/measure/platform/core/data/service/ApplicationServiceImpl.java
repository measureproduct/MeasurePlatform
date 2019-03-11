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
package org.measure.platform.core.data.service;

import java.util.List;

import javax.inject.Inject;

import org.measure.platform.core.data.api.IApplicationService;
import org.measure.platform.core.data.api.IDashboardService;
import org.measure.platform.core.data.api.IMeasureInstanceService;
import org.measure.platform.core.data.api.IProjectService;
import org.measure.platform.core.data.entity.Application;
import org.measure.platform.core.data.entity.Dashboard;
import org.measure.platform.core.data.entity.MeasureInstance;
import org.measure.platform.core.data.entity.Project;
import org.measure.platform.core.data.querys.ApplicationRepository;
import org.measure.platform.core.data.querys.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing Application.
 */
@Service
@Transactional
public class ApplicationServiceImpl implements IApplicationService {
    private final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Inject
    private ApplicationRepository applicationInstanceRepository;

    @Inject
    private IProjectService projectService;
    
    
    @Inject
    private IMeasureInstanceService instanceService;
    
    
    @Inject
    private IDashboardService dashboardService;



    /**
     * Save a applicationInstance.
     * @param applicationInstance the entity to save
     * @return the persisted entity
     */
    public Application save(Application applicationInstance) {
        log.debug("Request to save Application instance : {}", applicationInstance); 

        Application result = applicationInstanceRepository.save(applicationInstance);

        return result;
    }

    /**
     * Get all the Application instances.
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Application> findAll() {
        log.debug("Request to get all Application instances");
        List<Application> result = applicationInstanceRepository.findAll();
        return result;
    }

    @Override
    public List<Application> findApplicationInstancesByProject(Long projectId) {
        Project project = projectService.findOne(projectId);
        List<Application> result = applicationInstanceRepository.findByProject(project);
        return result;
    }

    /**
     * Get one applicationInstance by id.
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Application findOne(Long id) {
        log.debug("Request to get Application : {}", id);
        Application applicationInstance = applicationInstanceRepository.findOne(id);
        return applicationInstance;
    }

    /**
     * Delete the Application by id.
     * @param id the id of the entity
     */
    public void delete(Long id) {
  	
    	Application application = findOne(id);
    	
    	for(MeasureInstance instance : application.getInstances()) {
    		instanceService.delete(instance.getId());
    	}
    	
    	
    	for(Dashboard dashboard : application.getDashboards()) {
    		dashboardService.delete(dashboard.getId());
    	}
        
        applicationInstanceRepository.delete(id);
    }

	@Override
	public List<Application> findApplicationInstancesByName(String name) {
		return  applicationInstanceRepository.findByName(name);
	}

	@Override
	public List<Application> findApplicationInstanceByApplicationType(String applicationtype) {
        List<Application> result = applicationInstanceRepository.findByApplicationType(applicationtype);
        return result;
	}


}
