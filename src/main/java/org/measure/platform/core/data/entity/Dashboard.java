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
package org.measure.platform.core.data.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.measure.platform.utils.domain.User;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A Dashboard.
 */
@Entity
@Table(name = "dashboard")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Dashboard implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "dashboard_name", nullable = false)
    private String dashboardName;

    @Column(name = "dashboard_description")
    private String dashboardDescription;

    @Column(name = "mode")
    private String mode;

    @Column(name = "kibana_id")
    private String kibanaId;

    @Column(name = "dashboard_content")
    private String content;

    @Column(name = "auto")
    private Boolean auto;
    
    @Column(name = "editable")
    private Boolean editable;
  
    @Column(name = "size")
    private String size;

    @Column(name = "time_periode")
    private String timePeriode;

    @ManyToOne
    private Project project;
    
    @ManyToOne
    private Application application;

    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<MeasureView> views = new HashSet<>();
    
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    @JoinTable(
    		name = "user_viewed_dashboard",
			joinColumns = {@JoinColumn(name = "dashboard_id", referencedColumnName = "id") },
			inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id") })
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<User> users = new HashSet<>();
    
    @ManyToOne
    private User manager;
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDashboardName() {
        return dashboardName;
    }

    public Dashboard dashboardName(String dashboardName) {
        this.dashboardName = dashboardName;
        return this;
    }

    public void setDashboardName(String dashboardName) {
        this.dashboardName = dashboardName;
    }

    public String getDashboardDescription() {
        return dashboardDescription;
    }

    public Dashboard dashboardDescription(String dashboardDescription) {
        this.dashboardDescription = dashboardDescription;
        return this;
    }

    public void setDashboardDescription(String dashboardDescription) {
        this.dashboardDescription = dashboardDescription;
    }

    public String getMode() {
        return mode;
    }

    public Dashboard mode(String mode) {
        this.mode = mode;
        return this;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getKibanaId() {
        return kibanaId;
    }

    public Dashboard kibanaId(String kibanaId) {
        this.kibanaId = kibanaId;
        return this;
    }

    public void setKibanaId(String kibanaId) {
        this.kibanaId = kibanaId;
    }

    public String getContent() {
        return content;
    }

    public Dashboard content(String content) {
        this.content = content;
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean isAuto() {
        return auto;
    }

    public Dashboard auto(Boolean auto) {
        this.auto = auto;
        return this;
    }

    public void setAuto(Boolean auto) {
        this.auto = auto;
    }
        
    public Boolean isEditable() {
        return editable;
    }

    public Dashboard editable(Boolean editable) {
        this.editable = editable;
        return this;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }
    
    public String getTimePeriode() {
        return timePeriode;
    }

    public Dashboard timePeriode(String timePeriode) {
        this.timePeriode = timePeriode;
        return this;
    }

    public void setTimePeriode(String timePeriode) {
        this.timePeriode = timePeriode;
    }

    public String getSize() {
        return size;
    }

    public Dashboard size(String size) {
        this.size = size;
        return this;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Project getProject() {
        return project;
    }

    public Dashboard project(Project project) {
        this.project = project;
        return this;
    }

    public void setProject(Project project) {
        this.project = project;
    }

	public Set<MeasureView> getViews() {
        return views;
    }

    public Dashboard views(Set<MeasureView> measureViews) {
        this.views = measureViews;
        return this;
    }

    public Dashboard addViews(MeasureView measureView) {
        views.add(measureView);
        measureView.setDashboard(this);
        return this;
    }

    public Dashboard removeViews(MeasureView measureView) {
        views.remove(measureView);
        measureView.setDashboard(null);
        return this;
    }

    public void setViews(Set<MeasureView> measureViews) {
        this.views = measureViews;
    }
    
    public Application getApplication() {
        return application;
    }

    public Dashboard application(Application application) {
        this.application = application;
        return this;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
    
    public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}
	
	public Dashboard users(Set<User> users) {
        this.users = users;
        return this;
    }
	
	public Dashboard addUsers(User user) {
    	users.add(user);
    	user.getViewedDashboards().add(this);
        return this;
    }

    public Dashboard removeUsers(User user) {
    	this.users.remove(user);
        user.getViewedDashboards().remove(this);
        return this;
    }
        
	public User getManager() {
		return manager;
	}

	public void setManager(User manager) {
		this.manager = manager;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Dashboard dashboard = (Dashboard) o;
        if(dashboard.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, dashboard.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Dashboard{" +
                    "id=" + id +
                    ", dashboardName='" + dashboardName + "'" +
                    ", dashboardDescription='" + dashboardDescription + "'" +
                    '}';
    }

}
