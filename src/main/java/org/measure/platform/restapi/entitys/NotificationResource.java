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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.Valid;

import org.measure.platform.core.data.api.INotificationService;
import org.measure.platform.core.data.api.IProjectService;
import org.measure.platform.core.data.entity.Notification;
import org.measure.platform.core.data.entity.Project;
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
 * REST controller for managing Notification.
 */
@RestController
@RequestMapping("/api")
public class NotificationResource {
    private final Logger log = LoggerFactory.getLogger(NotificationResource.class);

    @Inject
    private INotificationService notificationService;

    @Inject
    private IProjectService projectService;

    /**
     * POST /notifications : Create a new notification.
     * @param notification the notification to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new notification, or with status 400 (Bad Request) if the notification has
     * already an ID
     * @throws java.net.URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/notifications")
    @Timed
    public ResponseEntity<Notification> createNotification(@Valid @RequestBody Notification notification) throws URISyntaxException {
        log.debug("REST request to save Notification : {}", notification);
        if (notification.getId() != null) {
            return ResponseEntity.badRequest().headers(
                    HeaderUtil.createFailureAlert("notification", "idexists", "A new notification cannot already have an ID"))
                    .body(null);
        }
        Notification result = notificationService.save(notification);
        return ResponseEntity.created(new URI("/api/notifications/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert("notification", result.getId().toString())).body(result);
    }

    /**
     * PUT /notifications : Updates an existing notification.
     * @param notification the notification to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * notification, or with status 400 (Bad Request) if the notification is not
     * valid, or with status 500 (Internal Server Error) if the notification
     * couldnt be updated
     * @throws java.net.URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/notifications")
    @Timed
    public ResponseEntity<Notification> updateNotification(@Valid @RequestBody Notification notification) throws URISyntaxException {
        log.debug("REST request to update Notification : {}", notification);
        if (notification.getId() == null) {
            return createNotification(notification);
        }
        Notification result = notificationService.save(notification);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert("notification", notification.getId().toString()))
                        .body(result);
    }

    /**
     * GET /notifications : get all the notifications.
     * @return the ResponseEntity with status 200 (OK) and the list of notifications
     * in body
     */
    @GetMapping("/notifications")
    @Timed
    public List<Notification> getAllNotifications() {
        log.debug("REST request to get all Notifications");
        return notificationService.findAll();
    }

    @GetMapping("/notifications/byproject/{id}")
    @Timed
    public List<Notification> getAllNotificationsByProject(@PathVariable Long id) {
        Project project = projectService.findOne(id);    
        
        List<Notification> notification = notificationService.findNewByProject(project);
        Collections.sort(notification, new Comparator<Notification>() {
        
            @Override
            public int compare(Notification o1, Notification o2) {
                return o1.getNotificationDate().compareTo(o2.getNotificationDate()) * -1;
            }
        });
        return notification;
    }

    /**
     * GET /notifications/:id : get the "id" notification.
     * @param id the id of the notification to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the
     * notification, or with status 404 (Not Found)
     */
    @GetMapping("/notifications/{id}")
    @Timed
    public ResponseEntity<Notification> getNotification(@PathVariable Long id) {
        log.debug("REST request to get Notification : {}", id);
        Notification notification = notificationService.findOne(id);
        return Optional.ofNullable(notification).map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE /notifications/:id : delete the "id" notification.
     * @param id the id of the notification to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/notifications/{id}")
    @Timed
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        log.debug("REST request to delete Notification : {}", id);
        notificationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("notification", id.toString())).build();
    }

}
