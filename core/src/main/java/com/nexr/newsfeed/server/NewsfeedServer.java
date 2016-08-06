package com.nexr.newsfeed.server;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.nexr.newsfeed.Context;
import com.nexr.newsfeed.NewsfeedException;
import com.nexr.newsfeed.jetty.JettyWebServer;
import com.nexr.newsfeed.jpa.ActivityQueryExceutor;
import com.nexr.newsfeed.jpa.FriendQueryExceutor;
import com.nexr.newsfeed.jpa.JPAService;
import com.nexr.newsfeed.jpa.UserQueryExceutor;
import com.nexr.newsfeed.service.FeedService;
import com.nexr.newsfeed.service.FollowingService;
import com.nexr.newsfeed.service.UserService;
import com.nexr.newsfeed.util.FollowCache;
import com.nexr.newsfeed.util.LocalFollowCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NewsfeedServer extends AbstractModule {

    private static Logger log = LoggerFactory.getLogger(NewsfeedServer.class);

    @Inject
    private Context context;

    @Inject
    private JPAService JPAService;

    @Inject
    private JettyWebServer jettyWebServer;


    public NewsfeedServer() {

    }

    public static void main(String[] args) {
        String cmd = args[0];

        if ("start".equals(cmd)) {

            Injector injector = Guice.createInjector(ImmutableList.of(new NewsfeedServer()));
            final NewsfeedServer app = injector.getInstance(NewsfeedServer.class);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("==== Call the shutdown routine ====");
                    try {
                        app.shutdown();
                    } catch (NewsfeedException e) {
                        e.printStackTrace();
                    }
                }
            });
            try {
                app.start();
            } catch (NewsfeedException e) {
                e.printStackTrace();
            }
        } else if ("stop".equals(cmd)) {

        }
    }

    @Override
    protected void configure() {
        String persistUnit = System.getProperty("persistenceUnit") == null ? "newsfeed-master-mysql" : System.getProperty
                ("persistenceUnit");
        bindConstant().annotatedWith(Names.named("persistenceUnit")).to(persistUnit);
        bindConstant().annotatedWith(Names.named("persistenceName")).to("newsfeed");
        bindConstant().annotatedWith(Names.named("siteConfig")).to("newsfeed.conf");
        bindConstant().annotatedWith(Names.named("defaultConfig")).to("newsfeed-default.conf");
        bind(Context.class).in(Singleton.class);
        bind(JPAService.class).in(Singleton.class);
        bind(JettyWebServer.class).in(Singleton.class);
        bind(UserQueryExceutor.class).in(Singleton.class);
        bind(FriendQueryExceutor.class).in(Singleton.class);
        bind(ActivityQueryExceutor.class).in(Singleton.class);
        bind(FollowCache.class).to(LocalFollowCache.class).in(Singleton.class);
        bind(UserService.class).in(Singleton.class);
        bind(FollowingService.class).in(Singleton.class);
        bind(FeedService.class).in(Singleton.class);
    }

    public void start() throws NewsfeedException {
        log.info("========= Newsfeed Starting ......   ========");

        try {
            jettyWebServer.start();
            log.info("Newsfeed Started !! ");
            jettyWebServer.join();

        } catch (Exception e) {
            log.error("Error starting NewsfeedServer. It may not be available.", e);
        }

    }

    public void shutdownServices() {
        if (JPAService != null) {
            JPAService.shutdown();
            JPAService = null;
        }
    }

    public void shutdown() throws NewsfeedException {

        shutdownServices();
        try {
            jettyWebServer.shutdown();
        } catch (Exception ex) {
            log.error("Error stopping Jetty. Newsfeed may not be available.", ex);
        }
        log.info("========= Newsfeed Shutdown ======== \n");

    }

    public JPAService getJPAService() {
        return JPAService;
    }

}
