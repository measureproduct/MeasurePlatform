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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.measure.platform.core.data.api.IAlertEventService;
import org.measure.platform.core.data.api.IDashboardService;
import org.measure.platform.core.data.api.IMeasureInstanceService;
import org.measure.platform.core.data.api.IMeasureViewService;
import org.measure.platform.core.data.api.INotificationService;
import org.measure.platform.core.data.api.IProjectService;
import org.measure.platform.core.data.api.dto.RightAccessDTO;
import org.measure.platform.core.data.api.dto.UserProjectDTO;
import org.measure.platform.core.data.entity.AlertEvent;
import org.measure.platform.core.data.entity.Dashboard;
import org.measure.platform.core.data.entity.MeasureInstance;
import org.measure.platform.core.data.entity.MeasureView;
import org.measure.platform.core.data.entity.Notification;
import org.measure.platform.core.data.entity.Project;
import org.measure.platform.core.data.querys.ProjectRepository;
import org.measure.platform.core.measurement.api.IElasticsearchIndexManager;
import org.measure.platform.service.analysis.api.IAlertSubscriptionManager;
import org.measure.platform.service.analysis.api.IAnalysisCatalogueService;
import org.measure.platform.service.analysis.data.alert.AlertSubscription;
import org.measure.platform.service.analysis.data.alert.AlertType;
import org.measure.platform.service.analysis.data.analysis.RegistredAnalysisService;
import org.measure.platform.utils.domain.User;
import org.measure.platform.utils.security.ProjectsUsersRolesConstants;
import org.measure.platform.utils.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing Project.
 */
@Service
@Transactional
public class ProjectServiceImpl implements IProjectService {
    private final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Inject
    private ProjectRepository projectRepository;

    @Inject
    private IMeasureInstanceService measureInstanceService;
    
    @Inject
    private IElasticsearchIndexManager indexManager;

    @Inject
    private IDashboardService dashboardService;

    @Inject
    private IMeasureViewService viewService;

    @Inject
    private INotificationService notifService;
    
    
    @Inject
    private IAlertEventService alertEventService;
   
    
    @Inject
    private IAlertSubscriptionManager subscriptionManager;
    
    @Inject
    private IAnalysisCatalogueService analysisCatalogue;
    
    @Inject
    private UserService userService;
    

    /**
     * Save a project.
     * @param project the entity to save
     * @return the persisted entity
     */
    public Project save(Project project) {
        log.debug("Request to save Project : {}", project);
    	User user = userService.findByCurrentLoggedIn();
        project.addManagers(user);
        
        if(project.getId() == null) {
        	Project result = projectRepository.save(project);
        	
        	for(RegistredAnalysisService service : analysisCatalogue.getAllAnalysisService()){
        		AlertSubscription suscribtion = new AlertSubscription();
    			suscribtion.setAnalysisTool(service.getName());
    			suscribtion.setProjectId(result.getId());
    			suscribtion.setEventType(AlertType.ANALYSIS_ENABLE);
    			subscriptionManager.subscribe(suscribtion);
    			
    			
    			suscribtion = new AlertSubscription();
    			suscribtion.setAnalysisTool(service.getName());
    			suscribtion.setProjectId(result.getId());
    			suscribtion.setEventType(AlertType.ANALYSIS_DESABLE);
    			subscriptionManager.subscribe(suscribtion);
        	}
        	
        	Dashboard dashboard = new Dashboard();
        	dashboard.setProject(result);
        	dashboard.setEditable(false);
        	dashboard.setMode("OVERVIEW");
        	dashboard.setDashboardName("Overview");
        	dashboardService.save(dashboard);
        	
        	return result;
        }else{
        	return projectRepository.save(project);
        }

    }

    /**
     * Get all the projects.
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Project> findAll() {
        log.debug("Request to get all Projects");
        List<Project> result = projectRepository.findAll();
        return result;
    }

    /**
     * Get all the projects.
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Project> findAllByOwner() {
        return projectRepository.findByOwnerIsCurrentUser();
    }

    /**
     * Get one project by id.
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Project findOne(Long id) {
        log.debug("Request to get Project : {}", id);
        Project project = projectRepository.findOne(id);
        return project;
    }

    /**
     * Delete the  project by id.
     * @param id the id of the entity
     */
    public void delete(Long id) {
        Project project = projectRepository.findOne(id);
        for(Dashboard dashboard : dashboardService.findByProject(id)){
        	dashboardService.delete(dashboard.getId());
        }
        
        for(MeasureView view : viewService.findByProjectOverview(id)){
            viewService.delete(view.getId());
        }
        
        for(MeasureView view : viewService.findByProject(id)){
            viewService.delete(view.getId());
        }
                        
        for (MeasureInstance instance : measureInstanceService.findMeasureInstancesByProject(id)) {
            measureInstanceService.delete(instance.getId());
        }
        
        for (Notification notif : notifService.findAllByProject(project)) {
            notifService.delete(notif.getId());
        }
        
        for (AlertEvent event : alertEventService.findAllByProject(project)) {
        	alertEventService.delete(event.getId());
        }

        projectRepository.delete(id);
    }
    
    /**
     * Invite user into a project
     * @param RightAccessDTO
     * @return
     * @throws URISyntaxException
     */
    @Override
    public Project inviteToProject(RightAccessDTO rightAccess) {
    	log.debug("Request to invite user into Project : {}", rightAccess.getProjectId());
    	User user = userService.findOne(rightAccess.getUserId());
    	Project project = projectRepository.findOne(rightAccess.getProjectId());
    	if (rightAccess.getRole().equals(ProjectsUsersRolesConstants.INVITER)) {
    		if (project.getManagers().contains(user)) {
    			project.removeManagers(user);
    		}
        	project.addInviters(user);
		} else if (rightAccess.getRole().equals(ProjectsUsersRolesConstants.MANAGER)) {
			if(project.getInviters().contains(user)) {
				project.removeInviters(user);
			}
    		project.addManagers(user);
    	}
    	Project result = projectRepository.save(project);
    	return result;
    }
    
    /**
     * Transform user role
     * @param projectId
     * @param userId
     * @return
     * @throws URISyntaxException
     */
	@Override
	public Project transformUserRole(Long projectId, Long userId) {
		Project project = projectRepository.getOne(projectId);
		Set<User> inviters = project.getInviters();
		Set<User> managers = project.getManagers();
		for (User user : new HashSet<>(inviters)) {
			if(user.getId() == userId) {
				project.removeInviters(user);
				project.addManagers(user);
				return project;
			}
		}
		for (User user : new HashSet<>(managers)) {
			if (user.getId() == userId) {
				project.removeManagers(user);
				project.addInviters(user);
				return project;
			}
		}
		return project;
	}
	
	/**
     * GET /projects : get all the users by projects.
     * @return the ResponseEntity with status 200 (OK) and the list of projects
     * in body
     */
	@Override
    public List<UserProjectDTO> findAllUsersByProject(Long projectId) {
		Project project = projectRepository.findOne(projectId);
		List<UserProjectDTO> usersProject = new ArrayList<>();
		
		for (User manager : project.getManagers()) {
			usersProject.add(new UserProjectDTO(String.valueOf(manager.getId()), manager.getLogin(), manager.getFirstName(), manager.getLastName(), manager.getEmail(), "Manager"));
		}
		
		for (User inviter : project.getInviters()) {
			usersProject.add(new UserProjectDTO(String.valueOf(inviter.getId()), inviter.getLogin(), inviter.getFirstName(), inviter.getLastName(), inviter.getEmail(), "Inviter"));
		}
    	return usersProject;
	}
	
	/**
     * GET /projects : get all the candidate users to project.
     * @return the ResponseEntity with status 200 (OK) and the list of projects
     * in body
     */
	@Override
    public List<UserProjectDTO> findCandidateUsersByProject(Long projectId) {
    	List<Object[]> objs = projectRepository.findCandidateUsersByProjectId(projectId);
		List<UserProjectDTO> usersProject = new ArrayList<>();
		for (Object[] obj : objs) {
			usersProject.add(new UserProjectDTO(String.valueOf(obj[0]), (String)obj[1], (String)obj[2], (String)obj[3], (String)obj[4], ""));
		}
    	return usersProject;
	}

	/**
     * Delete user from a project
     * @param projectId
     * @param userId
     * @return
     * @throws URISyntaxException
     */
	@Override
	public Project deleteUserFromProject(Long projectId, Long userId) {
		Project project = projectRepository.getOne(projectId);
		User currentUser = userService.findByCurrentLoggedIn();
		if(currentUser.getId() != userId) {
			for (User user : new HashSet<>(project.getInviters())) {
				if (user.getId() == userId) {
					project.removeInviters(user);
				}
			}
			for (User manager : new HashSet<>(project.getManagers())) {
				if (manager.getId() == userId) {
					project.removeManagers(manager);
				}
			}
		}
		return project;
	}

	@Override
	public boolean isCurrentUserHasManagerRole(Long projectId) {
		Project project = projectRepository.findOne(projectId);
		User currentUser = userService.findByCurrentLoggedIn();
		for (User user : new HashSet<>(project.getInviters())) {
			if (user.getId() == currentUser.getId()) {
				return false;
			}
		}
		for (User manager : new HashSet<>(project.getManagers())) {
			if (manager.getId() == currentUser.getId()) {
				return true;
			}
		}
		return false;
	}

}
