package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Buildings' charges CRUD
 * @author mjepkoech
 *
 */
public class BuildingChargesMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("buildingCharges");
		consumer.handler((Message<JsonObject> message) -> { 
			JsonObject reqdata = message.body();

			System.out.println("Building Charges received a message: " + reqdata);
			try {
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonArray fields = new JsonArray();
				String funcSP = "";

				if (operation.equals("add")) {
					addCharge(message, data);

				} else {

					switch (operation) {
					case "update":
						
						funcSP = "sp_updateBuildingCharge";
						String id = data.getString("id").trim();
						String building_id = data.getString("building_id").trim();
						String name = data.getString("name").trim();
						fields = new JsonArray().add(id).add(building_id).add(name);

						break;

					case "retrieve":

						funcSP = "sp_getBuildingCharges";
						id = data.getString("building_id").trim();
						fields = new JsonArray().add(id);

						break;


					case "delete":
						
						funcSP = "sp_removeBuildingCharge";
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
				System.out.println("Payment modes handler has reached all nodes");
			} else {
				System.out.println("Payment Modes handler failed!");
			}
		});
	}

	/**
	 * Method to add charges in form of a loop
	 * @param message
	 * @param data
	 */
	private void addCharge(Message<JsonObject> message, JsonObject data) {
		JsonArray fields;
		int building_id = 0;
		String funcSP;
		JsonArray details = data.getJsonArray("charge_details");

		JsonObject dbfields = new JsonObject();
		int details_size = details.size();
		System.out.println("DETAILS SIZE: " + details_size);

		for (int i = 0; i < details_size; i++) {
			dbfields = details.getJsonObject(i);

			building_id = data.getInteger("building_id");
			String name = dbfields.getString("name").trim();

			fields = new JsonArray().add(building_id).add(name);
			 funcSP = "sp_addBuildingCharges";

			JsonObject authData = new JsonObject();
			authData.put("storedprocedure", funcSP);
			authData.put("params", fields);

			EventBus esbBus = vertx.eventBus();
			DeliveryOptions options = new DeliveryOptions();
			int time = 5000;
			options.setSendTimeout(time);
			MessageProducer<JsonObject> producer = esbBus.publisher("DATABASEACCESS", options);
			producer.send(authData, (AsyncResult<Message<JsonObject>> artxn) -> {
				if (artxn.succeeded()) {
					JsonObject responseFields = artxn.result().body();
					System.out.println("Received reply from Session Mgr: " + responseFields);

					JsonObject feedback = new JsonObject();
					JsonArray params = responseFields.getJsonArray("params");
					feedback.put("data", params);

					message.reply(feedback);							
					} else {
					message.fail(0, artxn.cause().getMessage());

				}
			});
		}
	}

}