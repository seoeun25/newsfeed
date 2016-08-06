package com.nexr.newsfeed.jetty;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.nexr.newsfeed.Context;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.jpa.JPAService;
import com.nexr.newsfeed.rest.Activities;
import com.nexr.newsfeed.rest.Feeds;
import com.nexr.newsfeed.rest.Followings;
import com.nexr.newsfeed.rest.Users;
import com.nexr.newsfeed.service.FeedService;
import com.nexr.newsfeed.service.FollowingService;
import com.nexr.newsfeed.service.UserService;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;

public class JettyWebServer {

    public static int DEFAULT_PORT = 19191;
    private static Logger log = LoggerFactory.getLogger(JettyWebServer.class);
    private Server jettyServer;

    @Inject
    private Context context;

    private Injector baseInjector;

    public JettyWebServer() {

    }

    @Inject
    public void setInjector(Injector injector) {
        log.info(" Jetty setInjector : {} ", injector);
        this.baseInjector = injector;
    }

    public void start() throws NewsfeedException {
        jettyServer = new Server(context.getInt("newsfeed.port", DEFAULT_PORT));

        ServletContextHandler sch = new ServletContextHandler(jettyServer, "/");

        // Add our Guice listener that includes bindings
        sch.addEventListener(new GuiceServletConfig(baseInjector));

        // Then add GuiceFilter and configure the server to
        // reroute all requests through this filter.
        sch.addFilter(GuiceFilter.class, "/*", null);

        // Must add DefaultServlet for embedded Jetty.
        // This is not needed if web.xml is used instead.
        sch.addServlet(DefaultServlet.class, "/");

        try {
            // Start the server
            jettyServer.start();
        } catch (Exception e) {
            throw new NewsfeedException(e);
        }
    }

    public void shutdown() throws NewsfeedException {
        try {
            jettyServer.stop();
        } catch (Exception e) {
            throw new NewsfeedException(e);
        }
    }

    public void join() throws NewsfeedException {
        if (jettyServer == null) {
            throw new NewsfeedException("jettyServer is null");
        }
        try {
            jettyServer.join();
        } catch (InterruptedException e) {
            throw new NewsfeedException(e);
        }
    }

    private static class GuiceServletConfig extends GuiceServletContextListener {

        private final Injector bInjector;

        GuiceServletConfig(Injector injector) {
            this.bInjector = injector;
            log.info(" GuiceServletConfig, injector : {} ", injector);
        }

        @Override
        public void contextInitialized(ServletContextEvent servletContextEvent) {
            super.contextInitialized(servletContextEvent);
        }

        @Override
        protected Injector getInjector() {
            return Guice.createInjector(new JerseyServletModule() {
                @Override
                protected void configureServlets() {
                    bind(Users.class);
                    bind(Followings.class);
                    bind(Activities.class);
                    bind(Feeds.class);
                    serve("/*").with(GuiceContainer.class);
                }

                @Provides
                JPAService provideJPAService() {
                    return bInjector.getInstance(JPAService.class);
                }

                @Provides
                UserService provideUserService() {
                    return bInjector.getInstance(UserService.class);
                }

                @Provides
                FollowingService provideFollowingService() {
                    return bInjector.getInstance(FollowingService.class);
                }

                @Provides
                FeedService provideFeedService() {
                    return bInjector.getInstance(FeedService.class);
                }
            });
        }
    }

}
