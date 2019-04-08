package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Unit charges CRUD
 * 
 * @author mjepkoech
 *
 */
public class UnitChargesMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("unitCharges");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("Unit Charges received a message: " + reqdata);
			try {

				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();

				JsonArray fields = new JsonArray();

				String funcSP = "";

				String id = "";
				JsonObject dbfields = new JsonObject();

				if (operation.equals("add")) {
					addCharge(message, data, operation, id);

				} else {

					switch (operation) {

					case "update":

						JsonArray details = data.getJsonArray("charges");

						int details_size = details.size();

						for (int i = 0; i < details_size; i++) {
							dbfields = details.getJsonObject(i);

							id = dbfields.getString("id").trim();
							String unit_id = data.getString("unit_id").trim();
							String description = dbfields.getString("description").trim();
							String buildingcharge_id = dbfields.getString("buildingcharge_id").trim();
							String amount = dbfields.getString("amount").trim();
							String mandatory = dbfields.getString("mandatory").trim();
							String recurring = dbfields.getString("recurring").trim();
							String recur_mode = dbfields.getString("recur_mode").trim();
							String account = dbfields.getString("account").trim();
							fields = new JsonArray().add(id).add(unit_id).add(description).add(buildingcharge_id)
									.add(amount).add(account).add(mandatory).add(recurring).add(recur_mode);
							funcSP = "sp_updateUnitCharge";

							EventBus esbBus = vertx.eventBus();
							new DatabaseService().callDatabase(funcSP, fields, operation, message, esbBus);
						}
						break;

					case "retrieve":

						funcSP = "sp_getUnitCharges";

						id = data.getString("unit_id").trim();

						fields = new JsonArray().add(id);

						break;

					case "delete":
						
						funcSP = "sp_removeUnitCharge";
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
			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Unit charges handler has reached all nodes");
			} else {
				System.out.println("Unit charges handler failed!");
			}
		});
	}

	/**
	 * Method to handle charge addition inform of a loop
	 * @param message
	 * @param data
	 * @param operation
	 * @param id
	 */
	private void addCharge(Message<JsonObject> message, JsonObject data, String operation, String id) {
		JsonArray fields;
		JsonObject dbfields;
		JsonArray details = data.getJsonArray("charges");

		int details_size = details.size();

		for (int i = 0; i < details_size; i++) {
			dbfields = details.getJsonObject(i);

			String unit_id = data.getString("unit_id");
			String description = dbfields.getString("description").trim();
			String buildingcharge_id = dbfields.getString("buildingcharge_id").trim();
			String amount = dbfields.getString("amount").trim();
			String mandatory = dbfields.getString("mandatory").trim();
			String recurring = dbfields.getString("recurring").trim();
			String recur_mode = dbfields.getString("recur_mode").trim();
			String account = dbfields.getString("account").trim();
			fields = new JsonArray().add(id).add(unit_id).add(description).add(buildingcharge_id)
					.add(amount).add(account).add(mandatory).add(recurring).add(recur_mode);
			String funcSP = "sp_addUnitCharges";

			EventBus esbBus = vertx.eventBus();
			new DatabaseService().callDatabase(funcSP, fields, operation, message, esbBus);

		}
	}

}