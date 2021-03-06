package Assignment;

/**
 * Created by Alvin on 15/11/2017.
 */

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import javax.servlet.ServletException;
import java.io.File;
import java.net.Socket;

public class TomcatServer implements Runnable{

    public static final int TOMCAT_PORT = 8080;

    @Override
    public void run() {
        // JAX-RS (Jersey) configuration
        ResourceConfig config = new ResourceConfig();
        // Packages where Jersey looks for web service classes
        // Do not forget to add webserver package with Gson provider
        config.packages("Assignment", "webserver");
        // Tomcat configuration
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(TOMCAT_PORT);
        // Add web application
        Context context = null;
        try {
            context = tomcat.addWebapp("", new File("./WebContent").getAbsolutePath());
//            context = tomcat.addWebapp("/", new File("./WebContent").getAbsolutePath());
        } catch (ServletException e) {
            e.printStackTrace();
        }
        // Declare Jersey as a servlet
        Tomcat.addServlet(context, "jersey", new ServletContainer(config));
        // Map certain URLs to Jersey
        context.addServletMappingDecoded("/bot/*", "jersey");
        // Start server
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
        tomcat.getServer().await();

    }


/*    public static void main(String[] args) throws ServletException, LifecycleException {
        new TomcatServer().run();
    }*/


}
