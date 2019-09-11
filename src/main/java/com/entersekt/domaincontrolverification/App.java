package com.entersekt.domaincontrolverification;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
	private static final Logger log = LoggerFactory.getLogger(App.class);

	public static Map<String, String> challengeDataStore = new HashMap<>();

	public static Object password;;

	public static void main(String[] args) throws Exception {
		usage(args);
		new App().start();
	}

	public void start() throws Exception {

		final HandlerList handlers = new HandlerList();

		// URL has form: http://<host>:<port>/
		handlers.addHandler(buildWebUI(App.class, null, "angularjs-ui", "AngularJS based Web UI"));

		buildSwaggerBean("DomainControlVerifier", "DomainControlVerifier API", RestService.class.getPackage().getName());

		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.register(MultiPartFeature.class);

		resourceConfig.packages(RestService.class.getPackage().getName());

		attachSwagger(handlers, App.class, resourceConfig);

		ServletContainer servletContainer = new ServletContainer(resourceConfig);
		ServletHolder jerseyServlet = new ServletHolder(servletContainer);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(jerseyServlet, "/*");

		handlers.addHandler(context);

		Server jettyServer = new Server(80);

		jettyServer.setHandler(handlers);

		try {
			jettyServer.start();
			jettyServer.join();
		} finally {
			jettyServer.destroy();
		}

	}

	public static void buildSwaggerBean(String title, String description, Class<?> class1) {
		buildSwaggerBean(title, description, class1.getPackage().getName());
	}

	public static void buildSwaggerBean(String title, String description, String multiplePaths) {
		// This configures Swagger
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion("1.0.0");
		beanConfig.setResourcePackage(multiplePaths);
		beanConfig.setScan(true);
		beanConfig.setBasePath("/");
		beanConfig.setDescription(description);
		beanConfig.setTitle(title);
	}

	public static void attachSwagger(final HandlerList handlers, Class jettyMainClass, ResourceConfig resourceConfig)
			throws Exception {
		// this puts swagger UI at: http://host:port/swagger-ui/?url=%2Fswagger.json
		handlers.addHandler(buildWebUI(jettyMainClass, "swagger-ui", "REST Service API documentation"));

		// this one is for Swagger
		resourceConfig.packages(ApiListingResource.class.getPackage().getName());
	}

	public static ContextHandler buildWebUI(Class<?> jettyMainClass, String resourcePath, String webUiTitle)
			throws Exception {
		return buildWebUI(jettyMainClass, resourcePath, resourcePath, webUiTitle);

	}

	public static ContextHandler buildWebUI(Class jettyMainClass, String contextPath, String resourcePath,
			String webUiTitle) throws URISyntaxException {
		// this puts a <>-ui at: http://host:port/<>-ui
		final ResourceHandler angularJsUIResourceHandler = new ResourceHandler();
		URL angularJsUIResourcePath = jettyMainClass.getClassLoader().getResource(resourcePath);
		log.info("Embedded " + resourcePath + " resource path is '" + angularJsUIResourcePath + "'");
		final ContextHandler angularJsUIContext = new ContextHandler();
		if (angularJsUIResourcePath != null) {
			angularJsUIResourceHandler.setResourceBase(angularJsUIResourcePath.toURI().toString());
			angularJsUIContext.setContextPath("/" + ((contextPath != null) ? (contextPath + "/") : ""));
			angularJsUIContext.setHandler(angularJsUIResourceHandler);
		} else {
			log.error("No resource named " + resourcePath + " means no " + webUiTitle + " capability");
		}
		return angularJsUIContext;
	}

	private static void usage(String[] args) {

		if (args.length < 1) {
			log.error("Usage requires command line parameter PASSWORD");
			System.exit(0);
		} else {
			password = args[0];
		}
		log.info("Starting DomainControlVerifier with parameters: password = <HIDDEN>");

	}

}
