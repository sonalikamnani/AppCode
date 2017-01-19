package com.pivotal.cf.broker.controller;


import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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

import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceDoesNotExistException;
import com.pivotal.cf.broker.exception.ServiceInstanceBindingExistsException;
import com.pivotal.cf.broker.exception.ServiceInstanceBindingDoesNotExistException;
import com.pivotal.cf.broker.model.ErrorMessage;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import com.pivotal.cf.broker.model.ServiceInstanceBindingRequest;
import com.pivotal.cf.broker.model.ServiceInstanceBindingResponse;
import com.pivotal.cf.broker.service.ServiceInstanceBindingService;
import com.pivotal.cf.broker.service.ServiceInstanceService;

@Controller
public class ServiceInstanceBindingController extends BaseController {

	public static final String BASE_PATH = "/v2/service_instances/{instanceId}/service_bindings";
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceBindingController.class);

    private ServiceInstanceBindingService instanceBindingService;
	private ServiceInstanceService instanceService;
	
	@Autowired
	public ServiceInstanceBindingController(ServiceInstanceBindingService instanceBindingService,
			ServiceInstanceService instanceService) {
		this.instanceBindingService = instanceBindingService;
		this.instanceService = instanceService;
	}
	
	@RequestMapping(value = BASE_PATH + "/{bindingId}", method = RequestMethod.PUT, consumes = "application/json")
	public ResponseEntity<ServiceInstanceBindingResponse> bindServiceInstance(
			@PathVariable("instanceId") String instanceId, 
			@PathVariable("bindingId") String bindingId,
			@Valid @RequestBody ServiceInstanceBindingRequest request)
                throws ServiceInstanceDoesNotExistException, ServiceInstanceBindingExistsException, ServiceBrokerException {
		logger.info( "PUT: " + BASE_PATH + "/{bindingId}"
				+ ", bindServiceInstance(), serviceInstance.id = " + instanceId 
				+ ", bindingId = " + bindingId);

        if(!instanceService.serviceInstanceExists(instanceId)) {
            throw new ServiceInstanceDoesNotExistException(instanceId);
        }

        ServiceInstanceBinding binding = instanceBindingService.createServiceInstanceBinding(
				bindingId,
				instanceId,
				request.getServiceDefinitionId(),
				request.getPlanId(),
				request.getAppGuid());
		logger.info("ServiceInstanceBinding Created: " + binding);
        return new ResponseEntity<ServiceInstanceBindingResponse>(
        		new ServiceInstanceBindingResponse(binding), 
        		HttpStatus.CREATED);
	}
	
	@RequestMapping(value = BASE_PATH + "/{bindingId}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteServiceInstanceBinding(
			@PathVariable("instanceId") String instanceId, 
			@PathVariable("bindingId") String bindingId,
			@RequestParam(value="service_id", defaultValue = "") String serviceId,
			@RequestParam(value="plan_id", defaultValue = "") String planId)
                throws ServiceBrokerException, ServiceInstanceBindingDoesNotExistException {
		logger.info( "DELETE: " + BASE_PATH + "/{bindingId}"
				+ ", deleteServiceInstanceBinding(),  serviceInstance.id = " + instanceId 
				+ ", bindingId = " + bindingId 
				+ ", serviceId = " + serviceId
				+ ", planId = " + planId);

        String deletedBindingId = instanceBindingService.deleteServiceInstanceBinding(bindingId);
		if (deletedBindingId == null) {
			return new ResponseEntity<String>("{}", HttpStatus.NOT_FOUND);
		}
		logger.info("ServiceInstanceBinding Deleted: " + bindingId);
        return new ResponseEntity<String>("{}", HttpStatus.OK);
	}
	
	@ExceptionHandler(ServiceInstanceDoesNotExistException.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(
			ServiceInstanceDoesNotExistException ex, 
			HttpServletResponse response) {
	    return getErrorResponse(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); // 422
	}
	
	@ExceptionHandler(ServiceInstanceBindingExistsException.class)
	@ResponseBody
	public ResponseEntity<ErrorMessage> handleException(
			ServiceInstanceBindingExistsException ex, 
			HttpServletResponse response) {
	    return getErrorResponse(ex.getMessage(), HttpStatus.CONFLICT); // 409
	}

    @ExceptionHandler(ServiceInstanceBindingDoesNotExistException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(
            ServiceInstanceBindingDoesNotExistException ex,
            HttpServletResponse response) {
        return new ResponseEntity<String>("{}", HttpStatus.GONE); // 410
    }
}
