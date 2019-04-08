package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Paybills Providers CRUD
 * 
 * @author mjepkoech
 *
 */

public class PaybillProvidersMs extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("paybillProviders");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("Paybillprovider Microservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");
				JsonArray fields = new JsonArray();
				String funcSP = "";

				switch (operation) {
				case "add":
					
					funcSP = "sp_addPaybillProvider";
					String name = data.getString("name").trim();
					fields = new JsonArray().add(name);

					break;
				case "update":
					
					funcSP = "sp_updatePaybillProvider";
					String id = data.getString("id").trim();
					name = data.getString("name").trim();
					fields = new JsonArray().add(id).add(name);

					break;

				case "retrieveAll":

					funcSP = "sp_getAllPaybillProviders";
					fields = new JsonArray().add(operation);

					break;

				case "delete":
					
					funcSP = "sp_removePaybillProvider";
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
				System.out.println("Paybillprovider handler has reached all nodes");
			} else {
				System.out.println("Paybillprovider handler failed!");
			}
		});
	}
}
