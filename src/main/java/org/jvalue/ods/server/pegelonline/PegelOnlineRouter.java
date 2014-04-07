/*  Open Data Service
    Copyright (C) 2013  Tsysin Konstantin, Reischl Patrick

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */
package org.jvalue.ods.server.pegelonline;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jvalue.ods.db.DbAccessor;
import org.jvalue.ods.db.DbFactory;
import org.jvalue.ods.db.exception.DbException;
import org.jvalue.ods.logger.Logging;
import org.jvalue.ods.main.Router;
import org.jvalue.ods.server.RouterUtils;
import org.jvalue.ods.server.restlet.AccessObjectAttributeByIdRestlet;
import org.jvalue.ods.server.restlet.AccessObjectByIdRestlet;
import org.jvalue.ods.server.restlet.ClassObjectIdRestlet;
import org.jvalue.ods.server.restlet.ClassObjectRestlet;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class PegelOnlineRouter. defines routes that start with /pegelonline/
 * 
 */
public class PegelOnlineRouter implements Router<Restlet> {

	/** The routes. */
	private HashMap<String, Restlet> routes;

	/** The db accessor. */
	private DbAccessor<JsonNode> dbAccessor;

	/**
	 * Instantiates a new pegel online router.
	 * 
	 */
	public PegelOnlineRouter() {
		this.dbAccessor = DbFactory.createDbAccessor("pegelonline");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvalue.ods.adapter.RouterInterface#getRoutes()
	 */
	@Override
	public Map<String, Restlet> getRoutes() {
		routes = new HashMap<String, Restlet>();

		// gets data from all stations
		Restlet stationsRestlet = new Restlet() {

			@Override
			public void handle(Request request, Response response) {

				String message = "";

				// there is an attribute in url
				if (request.getResourceRef().getQueryAsForm().size() == 1) {

					message = new RouterUtils().getDocumentByAttribute(request, dbAccessor);

				} else {

					try {

						List<JsonNode> nodes = null;
						ObjectMapper mapper = new ObjectMapper();

						try {
							dbAccessor.connect();

							nodes = dbAccessor.getAllDocuments();

							message += mapper.writeValueAsString(nodes);
						} catch (RuntimeException e) {
							String errorMessage = "Could not retrieve data from db: "
									+ e;
							Logging.error(this.getClass(), errorMessage);
							System.err.println(errorMessage);
							message += mapper
									.writeValueAsString("Could not retrieve data. Try to update database via /pegelonline/update.");
						}

					} catch (IOException e) {
						String errorMessage = "Error during client request: "
								+ e;
						Logging.error(this.getClass(), errorMessage);
						System.err.println(errorMessage);
					}

				}

				response.setEntity(message, MediaType.APPLICATION_JSON);

			}

		};

		Restlet stationsFlatRestlet = new Restlet() {

			@Override
			public void handle(Request request, Response response) {
				// Print the requested URI path
				String message = "";
				try {

					List<JsonNode> nodes = null;
					ObjectMapper mapper = new ObjectMapper();

					try {
						dbAccessor.connect();

						nodes = dbAccessor.executeDocumentQuery(
								"_design/pegelonline", "getAllStationsFlat",
								null);

						message += mapper.writeValueAsString(nodes);
					} catch (RuntimeException e) {
						String errorMessage = "Could not retrieve data from db: "
								+ e;
						Logging.error(this.getClass(), errorMessage);
						System.err.println(errorMessage);
						message += mapper
								.writeValueAsString("Could not retrieve data. Try to update database via /pegelonline/update.");
					}

				} catch (IOException e) {
					String errorMessage = "Error during client request: " + e;
					Logging.error(this.getClass(), errorMessage);
					System.err.println(errorMessage);
				}

				response.setEntity(message, MediaType.APPLICATION_JSON);

			}

		};

		// gets the data of a single station
		Restlet singleStationRestlet = new Restlet() {
			@Override
			public void handle(Request request, Response response) {

				List<JsonNode> nodes = null;
				dbAccessor.connect();

				String name = (String) request.getAttributes().get("station");
				name = name.toUpperCase();

				nodes = dbAccessor.executeDocumentQuery("_design/pegelonline",
						"getSingleStation", name);

				if (!nodes.isEmpty()) {
					response.setEntity(nodes.get(0).toString(),
							MediaType.APPLICATION_JSON);
				} else {
					response.setEntity("Station not found.",
							MediaType.TEXT_PLAIN);
				}

			}
		};

		Restlet metadataRestlet = new Restlet() {
			@Override
			public void handle(Request request, Response response) {

				List<JsonNode> node = null;
				dbAccessor.connect();

				try {
					node = dbAccessor.executeDocumentQuery(
							"_design/pegelonline", "getMetadata", null);

					response.setEntity(node.get(0).toString(),
							MediaType.APPLICATION_JSON);
				} catch (DbException ex) {
					response.setEntity("Station not found.",
							MediaType.TEXT_PLAIN);
				}
			}
		};

		Restlet metadataIdRestlet = new Restlet() {
			@Override
			public void handle(Request request, Response response) {

				ObjectMapper mapper = new ObjectMapper();
				List<JsonNode> node = null;
				dbAccessor.connect();

				try {
					node = dbAccessor.executeDocumentQuery(
							"_design/pegelonline", "getMetadata", null);

					String id = "";
					if (node.get(0).isObject()) {

						HashMap<String, Object> metadata;

						try {
							metadata = mapper
									.readValue(
											node.get(0).toString(),
											new TypeReference<HashMap<String, Object>>() {
											});
							id = (String) metadata.get("_id");
						} catch (IOException e) {
							response.setEntity("Could not retrieve data.",
									MediaType.TEXT_PLAIN);
							return;
						}

					}

					response.setEntity(id, MediaType.APPLICATION_JSON);
				} catch (DbException ex) {
					response.setEntity("Metadata not found.",
							MediaType.TEXT_PLAIN);
				}
			}
		};

		Restlet idRestlet = new Restlet() {

			@Override
			public void handle(Request request, Response response) {

				String message = "";
				try {

					ObjectMapper mapper = new ObjectMapper();

					try {
						dbAccessor.connect();
						String name = (String) request.getAttributes().get(
								"station");
						name = name.toUpperCase();
						List<JsonNode> nodes = dbAccessor.executeDocumentQuery(
								"_design/pegelonline", "getStationId", name);
						if (nodes.isEmpty()) {
							throw new RuntimeException();
						} else {
							message += mapper.writeValueAsString(nodes.get(0));
						}

					} catch (RuntimeException e) {
						String errorMessage = "Could not retrieve data from db: "
								+ e;
						Logging.error(this.getClass(), errorMessage);
						System.err.println(errorMessage);
						message += mapper
								.writeValueAsString("Could not retrieve data.");
					}

				} catch (IOException e) {
					String errorMessage = "Error during client request: " + e;
					Logging.error(this.getClass(), errorMessage);
					System.err.println(errorMessage);
				}

				response.setEntity(message, MediaType.APPLICATION_JSON);

			}
		};
		
		routes.put("/ods/de/pegelonline/stations", stationsRestlet);
		routes.put("/ods/de/pegelonline/stationsFlat", stationsFlatRestlet);
		routes.put("/ods/de/pegelonline/stations/{station}",
				singleStationRestlet);
		routes.put("/ods/de/pegelonline/metadata", metadataRestlet);
		routes.put("/ods/de/pegelonline/metadata/$id", metadataIdRestlet);
		routes.put("/ods/de/pegelonline/stations/{station}/$id", idRestlet);
		routes.put("/ods/de/pegelonline/${id}", new AccessObjectByIdRestlet(
				dbAccessor));
		routes.put("/ods/de/pegelonline/stations/$class",
				new ClassObjectRestlet(dbAccessor, "_design/pegelonline",
						"getClassObject"));
		routes.put("/ods/de/pegelonline/stations/$class_id",
				new ClassObjectIdRestlet(dbAccessor, "_design/pegelonline",
						"getClassObjectId"));
		routes.put("/ods/de/pegelonline/${id}/{attribute}",
				new AccessObjectAttributeByIdRestlet(dbAccessor));
		return routes;
	}

	/**
	 * Gets the db accessor.
	 * 
	 * @return the db accessor
	 */
	public DbAccessor<JsonNode> getDbAccessor() {
		return dbAccessor;
	}

	/**
	 * Sets the db accessor.
	 * 
	 * @param dbAccessor
	 *            the new db accessor
	 */
	public void setDbAccessor(DbAccessor<JsonNode> dbAccessor) {
		this.dbAccessor = dbAccessor;
	}

}
