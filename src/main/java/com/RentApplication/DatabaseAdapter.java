package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * Connects to the database
 * 
 * @author mjepkoech
 *
 */
public class DatabaseAdapter extends AbstractVerticle {

	private JDBCClient client = null;

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();
		System.out.println("deploymentId=" + vertx.getOrCreateContext().deploymentID());
		MessageConsumer<JsonObject> consumer = eb.consumer("DATABASEACCESS");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject data = message.body();
			System.out.println("I have received a message: " + data);

			try {
				String spname = data.getString("storedprocedure");

				JsonArray datafields = data.getJsonArray("params");

				String paramindices = "";
//            StringBuilder sb = new StringBuilder();
				int datafields_size = datafields.size();
				for (int i = 0; i < datafields_size; i++) {
					paramindices += "?,";
//                sb.append(paramindices).append("?,");
				}

				paramindices = paramindices.substring(0, paramindices.length() - 1);

				System.out.println("Param fields=" + datafields);
				String funcSP = "{ call " + spname + "(" + paramindices + ") }";
				JsonObject jsonresponse = new JsonObject();

				getJDBCClientLocal().getConnection(res -> {
					SQLConnection connection = res.result();
					if (res.succeeded()) {
						connection.callWithParams(funcSP, datafields, new JsonArray(), conres -> {
							if (conres.succeeded()) {
								ResultSet rs = conres.result();
								if (conres.succeeded()) {
									if (rs.getNumColumns() > 0) {
										jsonresponse.put("params", rs.getRows());
										System.out.println(jsonresponse);
										message.reply(jsonresponse);
									} else {
										message.fail(0, "No data");
									}
								} else {
									jsonresponse.put("message", conres.cause().getMessage());
									jsonresponse.put("statuscode", "58");
									message.fail(0, jsonresponse + conres.cause().getMessage());
									System.out.println(jsonresponse + conres.cause().getMessage());
								}
							} else {
								jsonresponse.put("message", conres.cause().getMessage());
								jsonresponse.put("statuscode", "59");
								message.fail(0, conres.cause().getMessage());
								System.out.println(jsonresponse + conres.cause().getMessage());
							}
						});
						connection.close();
					} else {
						jsonresponse.put("message", "Database connection failed");
						jsonresponse.put("statuscode", "60");
						// msg.reply(jsonresponse);
						message.fail(0, jsonresponse + res.cause().getMessage());
						System.out.println(jsonresponse + res.cause().getMessage());
					}
				});

			} catch (Exception ex) {
				message.reply(data + ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("The DB handler registration has reached all nodes. DID="
						+ vertx.getOrCreateContext().deploymentID());
			} else {
				System.out.println("DB Registration failed!");
			}
		});
	}

	public JDBCClient getJDBCClientLocal() {
		try {

			JsonObject json = new JsonObject()
					.put("url", "jdbc:mysql://10.20.2.4:3306/EasyRentDB?allowPublicKeyRetrieval=true&useSSL=false")
					.put("driver_class", "com.mysql.cj.jdbc.Driver").put("max_pool_size", 60).put("user", "easyrent")
					.put("password", "Pass@123").put("row_stream_fetch_size", 256).put("min_pool_size", 10)
					.put("max_statements", 10).put("max_statements_per_connection", 3).put("max_idle_time", 15)
					.put("initial_pool_size", 10);

//            JsonObject json = new JsonObject()
//                    .put("url", "jdbc:mysql://localhost:3306/rentals?useSSL=false")
//                    .put("driver_class", "com.mysql.cj.jdbc.Driver")
//                    .put("max_pool_size", 60)
//                    .put("user", "root")
//                    .put("password", "Mine12345")
//                    .put("row_stream_fetch_size", 256)
//                    .put("min_pool_size", 10)
//                    .put("max_statements", 10)
//                    .put("max_statements_per_connection", 3)
//                    .put("max_idle_time", 15)
//                    .put("initial_pool_size", 10);

			client = JDBCClient.createShared(vertx, json);
			// System.out.println("Connection to DB Successful");

		} catch (Exception ex) {
			System.out.println("ERROR: " + ex.getMessage());
		}
		return client;
	}
}