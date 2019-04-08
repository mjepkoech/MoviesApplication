package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Bank branches CRUD
 * 
 * @author mjepkoech
 *
 */
public class BankBranchesMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("manageBranches");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("BankBranchesMicroservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				JsonArray fields = new JsonArray();

				String funcSP = "";

				switch (operation) {
				case "add":
					funcSP = "sp_addBranch";

					String branch = data.getString("name").trim();
					String bank = data.getString("bank").trim();
					String code = data.getString("code").trim();

					fields = new JsonArray().add(branch).add(bank).add(code);

					break;
				case "update":
					funcSP = "sp_updateBranch";

					String Id = data.getString("id").trim();
					branch = data.getString("name").trim();
					code = data.getString("code").trim();
					bank = data.getString("bank").trim();

					fields = new JsonArray().add(Id).add(branch).add(code).add(bank);

					break;

				case "retrieve":

					funcSP = "sp_getBranches";

					Id = data.getString("bank").trim();

					fields = new JsonArray().add(Id);

					break;

				case "retrieveAllBranches":

					funcSP = "sp_getAllBranches";

					String host = reqdata.getJsonObject("data").getJsonObject("channel_details").getString("host")
							.trim();

					fields = new JsonArray().add(host);

					break;

				case "delete":
					funcSP = "sp_removeBranch";

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
				System.out.println("Branches handler has reached all nodes");
			} else {
				System.out.println("Branches handler failed!");
			}
		});
	}

}