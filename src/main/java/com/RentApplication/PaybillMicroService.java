package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Paybills CRUD
 * 
 * @author mjepkoech
 *
 */

public class PaybillMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("managePaybills");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("Paybills Microservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");
				paybillCases(message, operation, data);

			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}
		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Paybills handler has reached all nodes");
			} else {
				System.out.println("Paybills handler failed!");
			}
		});
	}

	/**
	 * Method handling paybill use cases
	 * @param message
	 * @param operation
	 * @param data
	 */
	private void paybillCases(Message<JsonObject> message, String operation, JsonObject data) {
		JsonArray fields = new JsonArray();
		String funcSP = "";

		switch (operation) {
		case "add":
			funcSP = "sp_addPaybill";

			String account = data.getString("account").trim();
			String account_name = data.getString("account_name").trim();
			String provider_id = data.getString("provider_id").trim();
			String landlord_id = data.getString("landlord_id").trim();
			fields = new JsonArray().add(account).add(account_name).add(provider_id).add(landlord_id);

			break;
		case "update":
			funcSP = "sp_updatePaybill";

			String id = data.getString("id").trim();
			account = data.getString("account").trim();
			account_name = data.getString("account_name").trim();
			provider_id = data.getString("provider_id").trim();
			landlord_id = data.getString("landlord_id").trim();
			fields = new JsonArray().add(id).add(account).add(account_name).add(provider_id).add(landlord_id);

			break;

		case "retrieve":

			funcSP = "sp_getLandlordPaybillAccounts";
			landlord_id = data.getString("landlord_id").trim();
			fields = new JsonArray().add(landlord_id);

			break;

		case "retrieveByProvider":

			funcSP = "sp_getPaybillsByProvider";
			id = data.getString("provider_id").trim();
			fields = new JsonArray().add(id);

			break;

		case "retrieveAllPaybills":

			funcSP = "sp_getAllPaybillAccounts";
			fields = new JsonArray().add(operation);
			break;

		case "delete":
			
			funcSP = "sp_removePaybill";
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
	}
}
