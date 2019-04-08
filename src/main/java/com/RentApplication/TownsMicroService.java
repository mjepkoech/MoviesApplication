package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Towns CRUD
 * 
 * @author mjepkoech
 *
 */
public class TownsMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("manageTowns");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("TownsMicroservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");
				JsonArray fields = new JsonArray();
				String funcSP = "";

				switch (operation) {
				case "add":
					
					funcSP = "sp_addTown";
					String town = data.getString("name").trim();
					String county = data.getString("county").trim();
					fields = new JsonArray().add(town).add(county);

					break;
				case "update":
					
					funcSP = "sp_updateTown";
					String id = data.getString("id").trim();
					town = data.getString("name").trim();
					county = data.getString("county").trim();
					fields = new JsonArray().add(id).add(town).add(county);

					break;

				case "retrieve":

					funcSP = "sp_getTowns";
					county = data.getString("county").trim();
					fields = new JsonArray().add(county);

					break;

				case "retrieveAllTowns":

					funcSP = "sp_getAllTowns";
					fields = new JsonArray().add(operation);

					break;

				case "delete":
					
					funcSP = "sp_removeTown";
					id = data.getString("id").trim();
					String admin_id = data.getString("admin_id").trim();
					fields = new JsonArray().add(id).add(admin_id);

					break;

				default:
					System.out.println("No such operation: " + operation);
					break;
				}

				EventBus esbBus = vertx.eventBus();
				new DatabaseService().callDatabase(funcSP, fields, operation, message, esbBus);

			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Towns handler has reached all nodes");
			} else {
				System.out.println("Towns failed!");
			}
		});
	}
}