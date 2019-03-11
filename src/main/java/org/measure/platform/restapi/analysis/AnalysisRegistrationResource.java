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
package org.measure.platform.restapi.analysis;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.measure.platform.core.data.api.IAnalysisCardService;
import org.measure.platform.core.data.api.IProjectAnalysisService;
import org.measure.platform.core.data.entity.AnalysisCard;
import org.measure.platform.core.data.entity.ProjectAnalysis;
import org.measure.platform.restapi.analysis.dto.AnalysisConfiguration;
import org.measure.platform.restapi.analysis.dto.CardConfiguration;
import org.measure.platform.service.analysis.api.IAnalysisCatalogueService;
import org.measure.platform.service.analysis.data.analysis.AnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

@RestController
@RequestMapping(value = "api/analysis")
public class AnalysisRegistrationResource {

    private final Logger log = LoggerFactory.getLogger(AnalysisRegistrationResource.class);
    
    
    @Inject
    IAnalysisCatalogueService analysisRepository;
    
    @Inject
	private IProjectAnalysisService projectAnalysisService;
	
	@Inject
	private IAnalysisCardService analysisCardService;

    /**
     * PUT /register : Register an external analysis tool
     * @param analysisTool the AnalysisTool to update
     * @throws java.net.URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/register")
    @Timed
    public void registerAnalysisTool(@Valid @RequestBody AnalysisService analysisTool) throws URISyntaxException {
        log.debug("REST request to update RegisterAnalysisTool : {}", analysisTool);
        analysisRepository.registerAnalysisService(analysisTool);       
    }
    
    
    /**
     * GET  /list : get all the registred analysis services.
     * @return the ResponseEntity with status 200 (OK) and the list of analysis service in body
     */
    @GetMapping("/list")
    @Timed
    public List<AnalysisService> getAllAnalysisService() {
        log.debug("REST request to get all AnalysisService");
        return new ArrayList<AnalysisService>(analysisRepository.getAllAnalysisService());
    }

    /**
     *  GET /list/{id} : get an analysis service by is name
     * @param id id of the required analysis service
     * @return  the ResponseEntity with status 200 (OK) and the requested analysis service in body
     */
    @GetMapping("/list/{id}")
    @Timed
    public AnalysisService getAnalysisByName(@PathVariable String id) {
        return this.analysisRepository.getAnalysisServiceByName(id);
    }
    
	

	/**
	 * PUT /configure : Configure a project analysis
	 * 
	 * @param analysisTool the AnalysisTool to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated AnalysisTool, or with status 400 (Bad Request) if the project is
	 *         not valid, or with status 500 (Internal Server Error) if the project couldnt be updated
	 * @throws java.net.URISyntaxException if the Location URI syntax is incorrect
	 */
	@PutMapping("/configure")
	@Timed
	public void configureAnalysisTool(@Valid @RequestBody AnalysisConfiguration analysisConfiguration) throws URISyntaxException {
		log.debug("REST request to configure the analysis servicee : {}", analysisConfiguration);
		ProjectAnalysis analysis = projectAnalysisService.findOne(analysisConfiguration.getProjectAnalysisId());
		analysis.setViewUrl(analysisConfiguration.getViewUrl());
		analysis.setConfigurationUrl(analysisConfiguration.getConfigurationUrl());
		projectAnalysisService.save(analysis);
		
		for(CardConfiguration card : analysisConfiguration.getCards()){
			AnalysisCard mcard = new AnalysisCard();
			mcard.setCardLabel(card.getLabel());
			mcard.setCardUrl(card.getCardUrl());
			mcard.setPreferedWidth(card.getPreferedWidth());
			mcard.setPreferedHeight(card.getPreferedHeight());
			mcard.setProjectanalysis(analysis);
			analysisCardService.save(mcard);
		}
	}

}
