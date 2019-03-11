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
package org.measure.platform.restapi.entitys;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.Valid;

import org.measure.platform.core.data.api.IDashboardService;
import org.measure.platform.core.data.api.dto.DashboardDTO;
import org.measure.platform.core.data.api.dto.MappingDashboardDTO;
import org.measure.platform.core.data.entity.Dashboard;
import org.measure.platform.restapi.framework.rest.util.HeaderUtil;
import org.measure.platform.utils.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

/**
 * REST controller for managing Dashboard.
 */
@RestController
@RequestMapping("/api")
public class DashboardResource {
    private final Logger log = LoggerFactory.getLogger(DashboardResource.class);

    @Inject
    private IDashboardService dashboardService;

    /**
     * POST  /dashboards : Create a new dashboard.
     * @param dashboardDTO the dashboard to create
     * @return the ResponseEntity with status 201 (Created) and with body the new dashboard, or with status 400 (Bad Request) if the dashboard has already an ID
     * @throws java.net.URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/dashboards")
    @Timed
    public ResponseEntity<Dashboard> createDashboard(@Valid @RequestBody DashboardDTO dashboardDTO) throws URISyntaxException {
        log.debug("REST request to save Dashboard : {}", dashboardDTO);
        if (dashboardDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("dashboard", "idexists", "A new dashboard cannot already have an ID")).body(null);
        }
        MappingDashboardDTO mapping = new MappingDashboardDTO();
        Dashboard dashboard = mapping.convertDashboardDtoToDashboard(dashboardDTO);
    	
        Dashboard result = dashboardService.save(dashboard);
        
        for (Long inviter : dashboardDTO.getInviters()) {
        	result = dashboardService.shareDashboardWithUser(result, inviter);
		}
        return ResponseEntity.created(new URI("/api/dashboards/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert("dashboard", result.getId().toString()))
                    .body(result);
    }

    /**
     * PUT  /dashboards : Updates an existing dashboard.
     * @param dashboardDTO the dashboard to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated dashboard,
     * or with status 400 (Bad Request) if the dashboard is not valid,
     * or with status 500 (Internal Server Error) if the dashboard couldnt be updated
     * @throws java.net.URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/dashboards")
    @Timed
    public ResponseEntity<Dashboard> updateDashboard(@Valid @RequestBody DashboardDTO dashboardDTO) throws URISyntaxException {
        log.debug("REST request to update Dashboard : {}", dashboardDTO);
        if (dashboardDTO.getId() == null) {
            return createDashboard(dashboardDTO);
        }
        MappingDashboardDTO mapping = new MappingDashboardDTO();
        Dashboard dashboard = mapping.convertDashboardDtoToDashboard(dashboardDTO);
    	
        for (User deletedUser : new HashSet<>(dashboard.getUsers())) {
        	dashboard = dashboardService.removeUserOnDashboard(dashboard, deletedUser.getId());
		}
        
        Dashboard result = dashboardService.save(dashboard);
        
        for (Long inviter : dashboardDTO.getInviters()) {
        	result = dashboardService.shareDashboardWithUser(result, inviter);
		}
        
        return ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert("dashboard", dashboardDTO.getId().toString()))
                    .body(result);
    }

    /**
     * GET  /dashboards : get all the dashboards.
     * @return the ResponseEntity with status 200 (OK) and the list of dashboards in body
     */
    @GetMapping("/dashboards")
    @Timed
    public List<Dashboard> getAllDashboards() {
        log.debug("REST request to get all Dashboards");
        return dashboardService.findAll();
    }
    
    @GetMapping("/dashboards/byproject/{id}")
    @Timed
    public List<Dashboard> getDashboardsByProject(@PathVariable Long id) {
        return dashboardService.findByProject(id);
    }

    @GetMapping("/dashboardDTOs/byproject/{id}")
    @Timed
    public List<DashboardDTO> getDashboardDTOsByProject(@PathVariable Long id) {
        return dashboardService.findDTOByProject(id);
    }

    /**
     * GET  /dashboards/:id : get the "id" dashboard.
     * @param id the id of the dashboard to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the dashboard, or with status 404 (Not Found)
     */
    @GetMapping("/dashboards/{id}")
    @Timed
    public ResponseEntity<Dashboard> getDashboard(@PathVariable Long id) {
        log.debug("REST request to get Dashboard : {}", id);
        Dashboard dashboard = dashboardService.findOne(id);
        return Optional.ofNullable(dashboard)
                    .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /dashboards/:id : delete the "id" dashboard.
     * @param id the id of the dashboard to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/dashboards/{id}")
    @Timed
    public ResponseEntity<Void> deleteDashboard(@PathVariable Long id) {
        log.debug("REST request to delete Dashboard : {}", id);
        dashboardService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("dashboard", id.toString())).build();
    }

}
