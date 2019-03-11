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
package org.measure.platform.core.data.api;

import java.util.List;

import org.measure.platform.core.data.api.dto.RightAccessDTO;
import org.measure.platform.core.data.api.dto.UserProjectDTO;
import org.measure.platform.core.data.entity.Project;

/**
 * Service Interface for managing Project.
 */
public interface IProjectService {
    /**
     * Save a project.
     * @param project the entity to save
     * @return the persisted entity
     */
    Project save(Project project);

    /**
     * Get all the projects.
     * @return the list of entities
     */
    List<Project> findAll();

    /**
     * Get all the projects of current owner.
     * @return the list of entities
     */
    List<Project> findAllByOwner();

    /**
     * Get the "id" project.
     * @param id the id of the entity
     * @return the entity
     */
    Project findOne(Long id);

    /**
     * Delete the "id" project.
     * @param id the id of the entity
     */
    void delete(Long id);
    
    /**
     * Invite user into Project
     * @param project
     * @return
     */
    Project inviteToProject(RightAccessDTO rightAccess);
    
    /**
     * Get all the users by project.
     * @return the list of entities
     */
    public List<UserProjectDTO> findAllUsersByProject(Long projectId);
    
    /**
     * Get the candidates users to a project.
     * @return the list of entities
     */
    public List<UserProjectDTO> findCandidateUsersByProject(Long projectId);
    
    /**
     * Transfer inviter to manager user
     * @param projectId
     * @param userId
     * @return
     */
   public Project transformUserRole(Long projectId, Long userId);
    
    /**
     * Transfer user role
     * @param projectId
     * @param userId
     * @return
     */
    public Project deleteUserFromProject(Long projectId, Long userId);
    
    /**
     * Check if current user has manager role on project.
     * @param projectId
     */
    public boolean isCurrentUserHasManagerRole(Long projectId);
    
}
