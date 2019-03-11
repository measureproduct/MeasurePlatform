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
import org.measure.platform.core.data.api.IMeasurePropertyService;
import org.measure.platform.core.data.entity.MeasureProperty;
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
 * REST controller for managing MeasureProperty.
 */
@RestController
@RequestMapping("/api")
public class MeasurePropertyResource {
	private final Logger log = LoggerFactory.getLogger(MeasurePropertyResource.class);

	private static final String HIDE = "**********";

	@Inject
	private IMeasurePropertyService measurePropertyService;

	@Inject
	private IMeasureInstanceService measureInstanceService;

	/**
	 * POST /measure-properties : Create a new measureProperty.
	 * 
	 * @param measureProperty
	 *            the measureProperty to create
	 * @return the ResponseEntity with status 201 (Created) and with body the
	 *         new measureProperty, or with status 400 (Bad Request) if the
	 *         measureProperty has already an ID
	 * @throws java.net.URISyntaxException
	 *             if the Location URI syntax is incorrect
	 */
	@PostMapping("/measure-properties")
	@Timed
	public ResponseEntity<MeasureProperty> createMeasureProperty(@Valid @RequestBody MeasureProperty measureProperty) throws URISyntaxException {
		log.debug("REST request to save MeasureProperty : {}", measureProperty);
		if (measureProperty.getId() != null) {
			return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("measureProperty", "idexists", "A new measureProperty cannot already have an ID")).body(null);
		}
		MeasureProperty result = measurePropertyService.save(measureProperty);
		return ResponseEntity.created(new URI("/api/measure-properties/" + result.getId())).headers(HeaderUtil.createEntityCreationAlert("measureProperty", result.getId().toString())).body(result);
	}

	/**
	 * PUT /measure-properties : Updates an existing measureProperty.
	 * 
	 * @param measureProperty
	 *            the measureProperty to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         measureProperty, or with status 400 (Bad Request) if the
	 *         measureProperty is not valid, or with status 500 (Internal Server
	 *         Error) if the measureProperty couldnt be updated
	 * @throws java.net.URISyntaxException
	 *             if the Location URI syntax is incorrect
	 */
	@PutMapping("/measure-properties")
	@Timed
	public ResponseEntity<MeasureProperty> updateMeasureProperty(@Valid @RequestBody MeasureProperty measureProperty) throws URISyntaxException {
		log.debug("REST request to update MeasureProperty : {}", measureProperty);
		if (measureProperty.getId() == null) {
			return createMeasureProperty(measureProperty);
		}

		if (!measureProperty.getPropertyType().equals("PASSWORD") || !measureProperty.getPropertyValue().equals(HIDE)) {
			measurePropertyService.save(measureProperty);
		}
		return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert("measureProperty", measureProperty.getId().toString())).body(measureProperty);
	}

	/**
	 * GET /measure-properties : get all the measureProperties.
	 * 
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         measureProperties in body
	 */
	@GetMapping("/measure-properties")
	@Timed
	public List<MeasureProperty> getAllMeasureProperties() {
		log.debug("REST request to get all MeasureProperties");
		List<MeasureProperty> properties = measurePropertyService.findAll();

		for (MeasureProperty prop : properties) {
			if (prop.getPropertyType().equals("PASSWORD")) {
				prop.setPropertyValue(HIDE);
			}
		}

		return properties;
	}

	/**
	 * GET /measure-properties/:id : get the "id" measureProperty.
	 * 
	 * @param id
	 *            the id of the measureProperty to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         measureProperty, or with status 404 (Not Found)
	 */
	@GetMapping("/measure-properties/{id}")
	@Timed
	public ResponseEntity<MeasureProperty> getMeasureProperty(@PathVariable Long id) {
		log.debug("REST request to get MeasureProperty : {}", id);
		MeasureProperty measureProperty = measurePropertyService.findOne(id);
		if (measureProperty.getPropertyType().equals("PASSWORD")) {
			measureProperty.setPropertyValue(HIDE);
		}

		return Optional.ofNullable(measureProperty).map(result -> new ResponseEntity<>(result, HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * DELETE /measure-properties/:id : delete the "id" measureProperty.
	 * 
	 * @param id
	 *            the id of the measureProperty to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/measure-properties/{id}")
	@Timed
	public ResponseEntity<Void> deleteMeasureProperty(@PathVariable Long id) {
		log.debug("REST request to delete MeasureProperty : {}", id);
		measurePropertyService.delete(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("measureProperty", id.toString())).build();
	}

	/**
	 * GET /measure-properties : get all the measureProperties.
	 * 
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         measureProperties in body
	 */
	@GetMapping("/measure-properties/byinstance/{id}")
	@Timed
	public List<MeasureProperty> getMeasurePropertiesByInstanceId(@PathVariable Long id) {

		List<MeasureProperty> properties = measurePropertyService.findByInstance(measureInstanceService.findOne(id));
		
		// This line looks useless !
		//measurePropertyService.findAll();

		for (MeasureProperty prop : properties) {
			if (prop.getPropertyType().equals("PASSWORD")) {
				prop.setPropertyValue(HIDE);
			}
		}

		return properties;
	}

}
