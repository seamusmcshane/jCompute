package jCompute.Server;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsFTP
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(StatsFTP.class);
	
	private FtpServer server;
	private final int TIMEOUT = 3600;
	
	public StatsFTP(int port)
	{
		log.info("Creating StatsFTP Server on Port " + port);
		
		FtpServerFactory serverFactory = new FtpServerFactory();
		
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		
		UserManager um = userManagerFactory.createUserManager();
		
		BaseUser stats = new BaseUser();
		stats.setName("stats");
		stats.setPassword("stats");
		stats.setHomeDirectory("stats/");
		
		try
		{
			um.save(stats);
		}
		catch(FtpException e1)
		{
			log.error("Error Adding User : " + stats.getName());
		}
		
		ListenerFactory factory = new ListenerFactory();
		
		factory.setPort(port);
		
		// 1hr
		factory.setIdleTimeout(TIMEOUT);
		log.info("Set server timout to " + TIMEOUT);
		
		serverFactory.addListener("default", factory.createListener());
		serverFactory.setUserManager(um);
		
		server = serverFactory.createServer();
		log.info("Server Created");
	}
	
	public void start()
	{
		try
		{
			server.start();
		}
		catch(FtpException e)
		{
			e.printStackTrace();
		}
	}
	
	public void stop()
	{
		server.stop();
	}
}
