package org.jvalue.ods.filter;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.jvalue.ods.data.DataSource;
import org.jvalue.ods.db.DataRepository;
import org.jvalue.ods.db.DataRepositoryCache;
import org.jvalue.ods.db.DataSourceRepository;
import org.jvalue.ods.filter.reference.FilterChainReference;
import org.jvalue.ods.filter.reference.FilterReference;
import org.jvalue.ods.utils.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public final class FilterChainFactory {

	private final FilterFactory filterFactory;
	private final DataSourceRepository dataSourceRepository;
	private final DataRepositoryCache dataRepositoryCache;

	@Inject
	public FilterChainFactory(
			FilterFactory filterFactory,
			DataSourceRepository dataSourceRepository,
			DataRepositoryCache dataRepositoryCache) {

		this.filterFactory = filterFactory;
		this.dataSourceRepository = dataSourceRepository;
		this.dataRepositoryCache = dataRepositoryCache;
	}


	@SuppressWarnings("unchecked")
	public Filter<Void, ArrayNode> createFilterChain(FilterChainReference chainReference) {
		Assert.assertNotNull(chainReference);

		String sourceId = chainReference.getDataSourceId();
		List<DataSource> sources = dataSourceRepository.findBySourceId(sourceId);
		Assert.assertFalse(sources.isEmpty() || sources.size() > 1, "found more than one source for id " + sourceId);
		DataSource dataSource = sources.get(0);

		DataRepository dataRepository = dataRepositoryCache.getRepositoryForSourceId(sourceId);
		Assert.assertFalse(dataRepository == null, "no DataRepository found for id + " + sourceId);


		Filter<Void, ArrayNode> firstFilter = null;
		Filter<?, ArrayNode> lastFilter = null;

		for (FilterReference filterReference : chainReference.getFilterReferences()) {
			Filter filter = createFilterFromAnnotation(filterReference.getFilterName(), dataSource, dataRepository);
			if (firstFilter == null) {
				firstFilter = (Filter<Void, ArrayNode>) filter;
				lastFilter = (Filter<?, ArrayNode>) filter;
			} else {
				lastFilter = (Filter<?, ArrayNode>) lastFilter.setNextFilter(filter);
			}
		}

		return firstFilter;
	}


	@SuppressWarnings("rawTypes")
	private Filter createFilterFromAnnotation(
			String annotationValue,
			DataSource dataSource,
			DataRepository dataRepository) {

		for (Method method : FilterFactory.class.getDeclaredMethods()) {
			Named named = method.getAnnotation(Named.class);
			if (named == null) throw new IllegalArgumentException("named annotation not found");
			if (!annotationValue.equals(named.value())) continue;

			List<Object> arguments = new LinkedList<>();
			for (Class<?> parameterType : method.getParameterTypes()) {
				if (parameterType.equals(DataSource.class)) arguments.add(dataSource);
				else if (parameterType.equals(DataRepository.class)) arguments.add(dataRepository);
				else throw new IllegalStateException("what to do with parameter " + parameterType.toString());
			}

			try {
				return (Filter) method.invoke(filterFactory, arguments.toArray());
			} catch (IllegalAccessException | InvocationTargetException ie) {
				throw new IllegalStateException(ie);
			}
		}

		return null;
	}

}