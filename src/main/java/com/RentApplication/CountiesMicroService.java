package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Counties CRUD
 * 
 * @author mjepkoech
 *
 */
public class CountiesMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("manageCounties");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("CountiesMicroservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				JsonArray fields = new JsonArray();

				String funcSP = "";

				switch (operation) {
				case "add":
					funcSP = "sp_addCounty";

					String county = data.getString("name").trim();
					String code = data.getString("code").trim();

					fields = new JsonArray().add(county).add(code);

					break;
				case "update":
					funcSP = "sp_updateCounty";

					String id = data.getString("id").trim();
					county = data.getString("name").trim();
					code = data.getString("code").trim();

					fields = new JsonArray().add(id).add(county).add(code);

					break;

				case "retrieve":

					funcSP = "sp_getCounties";

					fields = new JsonArray().add(operation);

					break;

				case "delete":
					funcSP = "sp_removeCounty";

					id = data.getString("id").trim();
					String admin = data.getString("admin_id").trim();

					fields = new JsonArray().add(id).add(admin);

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
				System.out.println("Counties handler has reached all nodes");
			} else {
				System.out.println("Counties failed!");
			}
		});
	}

}