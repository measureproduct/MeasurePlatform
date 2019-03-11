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

import org.measure.platform.core.data.api.IAlertEventService;
import org.measure.platform.core.data.api.IAnalysisCardService;
import org.measure.platform.core.data.api.IProjectAnalysisService;
import org.measure.platform.core.data.entity.AlertEvent;
import org.measure.platform.core.data.entity.AnalysisCard;
import org.measure.platform.core.data.entity.Project;
import org.measure.platform.core.data.entity.ProjectAnalysis;
import org.measure.platform.core.data.querys.ProjectAnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing Project.
 */
@Service
@Transactional
public class ProjectAnalysisServiceImpl implements  IProjectAnalysisService {
    private final Logger log = LoggerFactory.getLogger(ProjectAnalysisServiceImpl.class);

    @Inject
    private  ProjectAnalysisRepository  projectAnalysisRepository;

    @Inject
    private IAlertEventService alertEventService;
    
    @Inject
    private IAnalysisCardService analysisCardService;
    
    /**
     * Save a ProjectAnalysis.
     * @param project the entity to save
     * @return the persisted entity
     */
    public ProjectAnalysis save(ProjectAnalysis projectAnalysis) {
        log.debug("Request to save Project : {}", projectAnalysis);
        ProjectAnalysis result = projectAnalysisRepository.save(projectAnalysis);
        return result;
    }

    /**
     * Get all the ProjectAnalysis.
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<ProjectAnalysis> findAll() {
        log.debug("Request to get all ProjectAnalysis");
        List<ProjectAnalysis> result = projectAnalysisRepository.findAll();
        return result;
    }


    /**
     * Get one project by id.
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public ProjectAnalysis findOne(Long id) {
        log.debug("Request to get ProjectAnalysis : {}", id);
        ProjectAnalysis projectAnalysis = projectAnalysisRepository.findOne(id);
        return projectAnalysis;
    }

    /**
     * Delete the  project by id.
     * @param id the id of the entity
     */
    public void delete(Long id) {
    	ProjectAnalysis projectAnalysis = projectAnalysisRepository.findOne(id);
 	
        for(AnalysisCard card : projectAnalysis.getAnalysiscards()){
        	analysisCardService.delete(card.getId());
        }
        
        Project project = projectAnalysis.getProject();
        
        for(AlertEvent event : alertEventService.findAllByProject(project)){
        	alertEventService.delete(event.getId());
        }
        
        projectAnalysisRepository.delete(id);
    }

	@Override
	public List<ProjectAnalysis> findAllByProject(Project project) {
		return projectAnalysisRepository.findAllByProject(project);
	}
	
	@Override
	public List<ProjectAnalysis> findAllByAnalysisTool(String analysisToolId) {
		return projectAnalysisRepository.findAllByAnalysisTool(analysisToolId);
	}

}
