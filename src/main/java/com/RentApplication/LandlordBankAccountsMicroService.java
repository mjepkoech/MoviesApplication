package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Landlord's bank accounts CRUD
 * 
 * @author mjepkoech
 *
 */
public class LandlordBankAccountsMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("manageBankAccounts");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("Landlord Bank Accounts Microservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				JsonArray fields = new JsonArray();
				String funcSP = "";

				switch (operation) {
				case "add":
					funcSP = "sp_addBankAccount";

					String landlord_id = data.getString("landlord_id");
					String account_name = data.getString("account_name");
					String account_number = data.getString("account_number");
					String bank = data.getString("bank_id");
					String branch = data.getString("branch_id");

					fields = new JsonArray().add(landlord_id).add(account_name).add(account_number).add(bank)
							.add(branch);

					break;
				case "update":
					funcSP = "sp_updateBankAccount";

					String id = data.getString("id").trim();
					landlord_id = data.getString("landlord_id");
					account_name = data.getString("account_name");
					account_number = data.getString("account_number");
					bank = data.getString("bank_id");
					branch = data.getString("branch_id");

					fields = new JsonArray().add(id).add(landlord_id).add(account_name).add(account_number).add(bank)
							.add(branch);

					break;

				case "retrieve":

					funcSP = "sp_getBankaccounts";
					id = data.getString("id").trim();
					fields = new JsonArray().add(id);

					break;

				case "retrieveLandlordAccounts":

					funcSP = "sp_getBankaccounts";
					id = data.getString("id").trim();
					fields = new JsonArray().add(id);

					break;

				case "delete":

					funcSP = "sp_removeBankAccount";
					id = data.getString("id").trim();
					String user_id = data.getString("user_id").trim();
					fields = new JsonArray().add(id).add(user_id);

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
				System.out.println("Landlord Bank Accounts modes handler has reached all nodes");
			} else {
				System.out.println("Landlord Bank Accounts Modes handler failed!");
			}
		});
	}

}