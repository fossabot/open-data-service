package org.jvalue.ods.db;


import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;

public class DbModule extends AbstractModule {

	@Override
	protected void configure() {
		CouchDbInstance couchDbInstance = new StdCouchDbInstance(new StdHttpClient.Builder().build());
		CouchDbConnector dataSourceConnector = couchDbInstance.createConnector(DataSourceRepository.DATABASE_NAME, true);

		bind(CouchDbInstance.class).toInstance(couchDbInstance);
		bind(CouchDbConnector.class).annotatedWith(Names.named(DataSourceRepository.DATABASE_NAME)).toInstance(dataSourceConnector);
		install(new FactoryModuleBuilder()
				.implement(DataRepository.class, DataRepository.class)
				.implement(FilterChainReferenceRepository.class, FilterChainReferenceRepository.class)
				.implement(DataViewRepository.class, DataViewRepository.class)
				.implement(NotificationClientRepository.class, NotificationClientRepository.class)
				.build(DbFactory.class));

		bind(new TypeLiteral<RepositoryCache<DataRepository>>() { }).in(Singleton.class);
	}

}
