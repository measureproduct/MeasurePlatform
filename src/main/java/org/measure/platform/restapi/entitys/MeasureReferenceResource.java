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
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.Valid;

import org.measure.platform.core.data.api.IMeasureInstanceService;
import org.measure.platform.core.data.api.IMeasureReferenceService;
import org.measure.platform.core.data.entity.MeasureReference;
import org.measure.platform.restapi.framework.rest.util.HeaderUtil;
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
 * REST controller for managing MeasureReference.
 */
@RestController
@RequestMapping("/api")
public class MeasureReferenceResource {
    private final Logger log = LoggerFactory.getLogger(MeasureReferenceResource.class);

    @Inject
    private IMeasureReferenceService measureReferenceService;

    @Inject
    private IMeasureInstanceService measureInstanceService;

    /**
     * POST  /measure-references : Create a new measureReference.
     * @param measureReference the measureReference to create
     * @return the ResponseEntity with status 201 (Created) and with body the new measureReference, or with status 400 (Bad Request) if the measureReference has already an ID
     * @throws java.net.URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/measure-references")
    @Timed
    public ResponseEntity<MeasureReference> createMeasureReference(@Valid @RequestBody MeasureReference measureReference) throws URISyntaxException {
        log.debug("REST request to save MeasureReference : {}", measureReference);
        if (measureReference.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("measureReference", "idexists", "A new measureReference cannot already have an ID")).body(null);
        }
        MeasureReference result = measureReferenceService.save(measureReference);
        return ResponseEntity.created(new URI("/api/measure-references/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert("measureReference", result.getId().toString()))
                    .body(result);
    }

    /**
     * PUT  /measure-references : Updates an existing measureReference.
     * @param measureReference the measureReference to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated measureReference,
     * or with status 400 (Bad Request) if the measureReference is not valid,
     * or with status 500 (Internal Server Error) if the measureReference couldnt be updated
     * @throws java.net.URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/measure-references")
    @Timed
    public ResponseEntity<MeasureReference> updateMeasureReference(@Valid @RequestBody MeasureReference measureReference) throws URISyntaxException {
        log.debug("REST request to update MeasureReference : {}", measureReference);
        if (measureReference.getId() == null) {
            return createMeasureReference(measureReference);
        }
        MeasureReference result = measureReferenceService.save(measureReference);
        return ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert("measureReference", measureReference.getId().toString()))
                    .body(result);
    }

    /**
     * GET  /measure-references : get all the measureReferences.
     * @return the ResponseEntity with status 200 (OK) and the list of measureReferences in body
     */
    @GetMapping("/measure-references")
    @Timed
    public List<MeasureReference> getAllMeasureReferences() {
        log.debug("REST request to get all MeasureReferences");
        return measureReferenceService.findAll();
    }

    /**
     * GET  /measure-references/:id : get the "id" measureReference.
     * @param id the id of the measureReference to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the measureReference, or with status 404 (Not Found)
     */
    @GetMapping("/measure-references/{id}")
    @Timed
    public ResponseEntity<MeasureReference> getMeasureReference(@PathVariable Long id) {
        log.debug("REST request to get MeasureReference : {}", id);
        MeasureReference measureReference = measureReferenceService.findOne(id);
        return Optional.ofNullable(measureReference)
                    .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /measure-references/:id : delete the "id" measureReference.
     * @param id the id of the measureReference to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/measure-references/{id}")
    @Timed
    public ResponseEntity<Void> deleteMeasureReference(@PathVariable Long id) {
        log.debug("REST request to delete MeasureReference : {}", id);
        measureReferenceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("measureReference", id.toString())).build();
    }

    /**
     * GET  /measure-references : get all the measureReferences.
     * @return the ResponseEntity with status 200 (OK) and the list of measureReferences in body
     */
    @GetMapping("/measure-references/byinstance/{id}")
    @Timed
    public List<MeasureReference> getMeasureReferencesByInstanceId(@PathVariable Long id) {
        return measureReferenceService.findByInstance(measureInstanceService.findOne(id));
    }

}
