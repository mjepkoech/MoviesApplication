package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Banks CRUD
 * 
 * @author mjepkoech
 *
 */

public class BanksMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("manageBanks");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("BanksMicroservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				JsonArray fields = new JsonArray();

				String funcSP = "";

				switch (operation) {
				case "add":
					funcSP = "sp_addBank";

					String bank = data.getString("name").trim();
					String code = data.getString("code").trim();

					fields = new JsonArray().add(bank).add(code);

					break;
				case "update":
					funcSP = "sp_updateBank";

					String Id = data.getString("id").trim();
					bank = data.getString("name").trim();
					code = data.getString("code").trim();

					fields = new JsonArray().add(Id).add(bank).add(code);

					break;

				case "retrieve":

					funcSP = "sp_getBanks";

					fields = new JsonArray().add(operation);

					break;

				case "delete":
					funcSP = "sp_removeBank";

					Id = data.getString("id").trim();
					String admin = data.getString("admin_id").trim();

					fields = new JsonArray().add(Id).add(admin);

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
				System.out.println("Banks handler has reached all nodes");
			} else {
				System.out.println("Banks handler failed!");
			}
		});
	}

}
