package com.entersekt.domaincontrolverification;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/.well-known")
@Api(value = "/.well-known")
public class RestService {

	private static final Logger log = LoggerFactory.getLogger(RestService.class);

	@PUT
	@Path("acme-challenge")
	@ApiOperation(value = "Loads the data provided in a file by LetsEncrypt for HTTP verification")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Loaded successfully"),
			@ApiResponse(code = 400, message = "Bad request") })
	public Response putVerificationData(
			@ApiParam(value = "Uniquely identifies the challenge data", required = true) Secrets secrets)
			throws Exception {

		if (secrets.password.equals(App.password)) {
			App.challengeDataStore.put(secrets.verificationFileContents.split("\\.")[0],
					secrets.verificationFileContents);
			return Response.ok().entity("Loaded successfullyO").build();

		} else {
			return Response.status(Status.FORBIDDEN).entity("Sorry mate!").build();
		}
	}

	@GET
	@Path("acme-challenge/{name}")
	@ApiOperation(value = "Returns the data provided in a file by LetsEncrypt for HTTP verification")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Ok"), @ApiResponse(code = 400, message = "Bad request") })
	public Response getVerificationData(
			@ApiParam(value = "Uniquely identifies the challenge data", required = true) @PathParam("name") String name)
			throws Exception {

		return Response.ok().entity(App.challengeDataStore.get(name)).build();
	}

}