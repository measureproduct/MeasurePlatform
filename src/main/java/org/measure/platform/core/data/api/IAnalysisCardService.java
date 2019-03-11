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

import org.measure.platform.core.data.entity.AnalysisCard;
import org.measure.platform.core.data.entity.Project;
import org.measure.platform.core.data.entity.ProjectAnalysis;

/**
 * Service Interface for managing AnalysisCard.
 */
public interface IAnalysisCardService {
    /**
     * Save a an AnalysisCard.
     * @param card the entity to save
     * @return the persisted entity
     */
	AnalysisCard save(AnalysisCard card);

    /**
     * Get all the AnalysisCard.
     * @return the list of entities
     */
    List<AnalysisCard> findAll();

    /**
     * Get all the AnalysisCard of current project analysis.
     * @return the list of entities
     */
    List<AnalysisCard> findAllByProjectAnalysis(ProjectAnalysis projectAnalysis);

    /**
     * Get the "id" AnalysisCard.
     * @param id the id of the entity
     * @return the entity
     */
    AnalysisCard findOne(Long id);

    /**
     * Delete the "id" project.
     * @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Get all the AnalysisCard of current project.
     * @return the list of entities
     */
	List<AnalysisCard> findAllByProject(Project project);


}
