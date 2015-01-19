package de.hpi.dpdc.dubstep.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import de.hpi.dpdc.dubstep.detection.address.Address;

public class HibernateUtil {

	private static SessionFactory sessionFactory;
	private static ServiceRegistry serviceRegistry;

	public static SessionFactory buildSessionFactory() {
		Configuration config = new Configuration()
		.addAnnotatedClass(Address.class)
		.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
		.setProperty("hibernate.connection.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
		.setProperty("hibernate.current_session_context_class", "thread")
		.setProperty("hibernate.show_sql", "true")
		.setProperty("hibernate.hbm2ddl.auto", "create");
		serviceRegistry = new StandardServiceRegistryBuilder().applySettings(config.getProperties()).build();
		return config.buildSessionFactory(serviceRegistry);
	}

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			sessionFactory = buildSessionFactory();
		}
		return sessionFactory;
	}

	public static void shutdown() {
		// Close caches and connection pools
		getSessionFactory().close();
	}

}
