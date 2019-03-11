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
package org.measure.platform.restapi.measure;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import org.measure.platform.core.catalogue.api.IMeasureCatalogueService;
import org.measure.platform.core.measurement.impl.ElasticMeasurementStorage;
import org.measure.platform.restapi.framework.rest.util.HeaderUtil;
import org.measure.platform.service.agent.api.IRemoteCatalogueService;
import org.measure.smm.measure.model.SMMMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.codahale.metrics.annotation.Timed;

@RestController
@RequestMapping(value = "api/measure")
public class MeasureResource {
	private final Logger log = LoggerFactory.getLogger(ElasticMeasurementStorage.class);

	@Inject
	IMeasureCatalogueService measureCatalogue;

	@Inject
	IRemoteCatalogueService remoteCatalogue;

//	@RequestMapping(value = "/upload", method = RequestMethod.PUT)
//	public void handleFileUpload(@RequestBody String data) {
//		try {
//			byte[] fileData = DatatypeConverter.parseBase64Binary(data.substring(data.indexOf(",") + 1));
//			File tempFile = File.createTempFile(data.substring(0, 20), "measure");
//			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(tempFile));
//			stream.write(fileData);
//			stream.close();
//			measureCatalogue.storeMeasure(tempFile.toPath());
//			tempFile.delete();
//		} catch (IOException e) {
//			log.error("File Upload Error", e);
//		}
//	}

	@PostMapping("/upload")
	public void handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		try {
			byte[] fileData = file.getBytes();
			File tempFile = File.createTempFile(file.getOriginalFilename(), ".measure");
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(tempFile));
			stream.write(fileData);
			stream.close();
			measureCatalogue.storeMeasure(tempFile.toPath());
			tempFile.delete();
		} catch (IOException e) {
			log.error("File Upload Error", e);
		}
	}

	/**
	 * GET /measures : get all the measures.
	 * 
	 * @return the ResponseEntity with status 200 (OK) and the list of projects
	 *         in body
	 */
	@GetMapping("/findall")
	@Timed
	public List<SMMMeasure> getAllMeasures() {
		List<SMMMeasure> result = new ArrayList<>();
		result.addAll(this.measureCatalogue.getAllMeasures());
		result.addAll(this.remoteCatalogue.getAllMeasures());
		return result;
	}

	/**
	 * GET /measure/:id : get the "id" measure.
	 * 
	 * @param id
	 *            the id of the measure to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         measure, or with status 404 (Not Found)
	 */
	@GetMapping("/{id}")
	@Timed
	public ResponseEntity<SMMMeasure> getMeasure(@PathVariable String id) {

		if (id != null && id.contains("(") && id.contains(")")) {
			// is catalogue measure
			String agentId = id.substring(id.indexOf("(") + 1, id.indexOf(")"));
			
			SMMMeasure measure = remoteCatalogue.getMeasureByName(id, agentId);
			return Optional.ofNullable(measure).map(result -> new ResponseEntity<>(result, HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
			
		} else {
			SMMMeasure measure = measureCatalogue.getMeasure(null,id);
			return Optional.ofNullable(measure).map(result -> new ResponseEntity<>(result, HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
		}
	}

	/**
	 * DELETE /measure/:id : delete the "id" measure.
	 * 
	 * @param id
	 *            the id of the measure to delete
	 * @return the ResponseEntity with status 200 (OK)
	 */
	@DeleteMapping("/{id}")
	@Timed
	public ResponseEntity<Void> deleteMeasure(@PathVariable String id) {
		measureCatalogue.deleteMeasure(id);
		return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("measure", id)).build();
	}

}
