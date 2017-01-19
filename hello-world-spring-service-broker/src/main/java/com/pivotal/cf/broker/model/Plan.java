package com.pivotal.cf.broker.model;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * A service plan available for a ServiceDefinition
 * 
 * @author sgreenberg@gopivotal.com
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class Plan {

	@NotEmpty
	@JsonSerialize
	@JsonProperty("id")
	private String id;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("name")
	private String name;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("description")
	private String description;
	
	@JsonSerialize
    @JsonProperty("metadata")
    private Map<String,Object> metadata;

	public Plan(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public Plan(String id, String name, String description, Map<String,Object> metadata) {
		this(id, name, description);
		setMetadata(metadata);
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}
	
	public void setMetadata(Map<String, Object> metadata) {
	    this.metadata = metadata;
	}
	
}
