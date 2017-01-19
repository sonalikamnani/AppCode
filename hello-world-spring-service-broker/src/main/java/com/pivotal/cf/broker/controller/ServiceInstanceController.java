package com.pivotal.cf.broker.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.pivotal.cf.broker.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pivotal.cf.broker.model.CreateServiceInstanceRequest;
import com.pivotal.cf.broker.model.CreateServiceInstanceResponse;
import com.pivotal.cf.broker.model.ErrorMessage;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.service.CatalogService;
import com.pivotal.cf.broker.service.ServiceInstanceService;

/**
 * See: http://docs.cloudfoundry.com/docs/running/architecture/services/writing-service.html
 *
 * @author sgreenberg@gopivotal.com
 * @author ehoning@gopivotal.com
 */
@Controller
public class ServiceInstanceController extends BaseController {

	public static final String BASE_PATH = "/v2/service_instances";
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceController.class);

    private ServiceInstanceService instanceService;
	private CatalogService catalogService;
	
	@Autowired
 	public ServiceInstanceController(ServiceInstanceService instanceService, CatalogService catalogService) {
 		this.instanceService = instanceService;
 		this.catalogService = catalogService;
 	}

	@RequestMapping(value = BASE_PATH + "/{instanceId}", method = RequestMethod.PUT, consumes = "application/json")
	public ResponseEntity<CreateServiceInstanceResponse> createServiceInstance(
			@PathVariable("instanceId") String instanceId,
			@Valid @RequestBody CreateServiceInstanceRequest request)
                throws ServiceDefinitionDoesNotExistException, ServiceInstanceExistsException, ServiceBrokerException {
		logger.info("PUT: " + BASE_PATH + "/{instanceId}" + ", createServiceInstance(), instanceId = " + instanceId);

        ServiceDefinition svc = catalogService.getServiceDefinition(request.getServiceDefinitionId());
		if (svc == null) {
			throw new ServiceDefinitionDoesNotExistException(request.getServiceDefinitionId());
		}
		ServiceInstance instance = instanceService.createServiceInstance(
				svc,
                instanceId,
				request.getPlanId(),
				request.getOrganizationGuid(), 
				request.getSpaceGuid());

		logger.info("ServiceInstance Created: " + instance);
        return new ResponseEntity<CreateServiceInstanceResponse>(
        		new CreateServiceInstanceResponse(instance), 
        		HttpStatus.CREATED);
	}
	
	@RequestMapping(value = BASE_PATH + "/{instanceId}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteServiceInstance(
			@PathVariable("instanceId") String instanceId, 
			@RequestParam("service_id") String serviceId,
			@RequestParam("plan_id") String planId)
                throws ServiceBrokerException, ServiceInstanceDoesNotExistException, ServiceInstanceIsBoundException {
		logger.info("DELETE: " + BASE_PATH + "/{instanceId}"
               + ", deleteServiceInstanceBinding(), instanceId = " + instanceId
                + ", serviceId = " + serviceId
                + ", planId = " + planId);

		instanceService.deleteServiceInstance(instanceId);

		logger.info("ServiceInstance Deleted: " + instanceId);
        return new ResponseEntity<String>("{}", HttpStatus.OK);
	}
	
	@ExceptionHandler(ServiceDefinitionDoesNotExistException.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(
			ServiceDefinitionDoesNotExistException ex, 
			HttpServletResponse response) {
	    return getErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); // 422
	}
	
	@ExceptionHandler(ServiceInstanceExistsException.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(
			ServiceInstanceExistsException ex, 
			HttpServletResponse response) {
	    return getErrorResponse(ex.getMessage(), HttpStatus.CONFLICT); // 409
	}

    @ExceptionHandler(ServiceInstanceDoesNotExistException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(
            ServiceInstanceDoesNotExistException ex,
            HttpServletResponse response) {
        return new ResponseEntity<String>("{}", HttpStatus.GONE); // 410
    }

    @ExceptionHandler(ServiceInstanceIsBoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(
            ServiceInstanceIsBoundException ex,
            HttpServletResponse response) {
        return getErrorResponse(ex.getMessage(), HttpStatus.CONFLICT); // 409
    }

}
