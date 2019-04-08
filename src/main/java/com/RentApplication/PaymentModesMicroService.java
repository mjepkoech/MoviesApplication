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
 * Buildings' payment modes CRUD
 * 
 * @author mjepkoech
 *
 */
public class PaymentModesMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("paymentModes");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("Payment Modes Microservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				JsonArray fields = new JsonArray();

				String funcSP = "";

				if (operation.equals("add")) {
					addPaymentAccounts(message, data);

				} else {
					switch (operation) {
					case "update":
						funcSP = "sp_updateBuildingPaymentMode";

						String id = data.getString("id").trim();
						String building_Id = data.getString("building_id").trim();
						String type = data.getString("type").trim();
						String mode_id = data.getString("mode_id").trim();
						String account_id = data.getString("account_id").trim();

						fields = new JsonArray().add(id).add(building_Id).add(type).add(mode_id).add(account_id);

						break;

					case "retrieve":

						funcSP = "sp_getBuildingPaymentModes";
						id = data.getString("building_id").trim();
						fields = new JsonArray().add(id);

						break;

					case "delete":
						
						funcSP = "sp_removeBuildingPaymentMode";
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
	 * Adds building payment accounts in form of a loop
	 * @param message
	 * @param data
	 */
	private void addPaymentAccounts(Message<JsonObject> message, JsonObject data) {
		JsonArray fields;
		String funcSP;
		int building_id;
		JsonArray details = data.getJsonArray("pay_acc");

		JsonObject dbfields = new JsonObject();
		int details_size = details.size();
		System.out.println("DETAILS SIZE: " + details_size);

		for (int i = 0; i < details_size; i++) {
			dbfields = details.getJsonObject(i);

			building_id = data.getInteger("building_id");
			String type = dbfields.getString("type").trim();
			String mode_id = dbfields.getString("mode_id").trim();
			String account_id = dbfields.getString("account_id").trim();

			fields = new JsonArray().add(building_id).add(type).add(mode_id).add(account_id);
			funcSP = "sp_addBuildingPaymentMode";

			JsonObject authData = new JsonObject();
			authData.put ("storedprocedure", funcSP);
			authData.put("params", fields);

			EventBus esbBus = vertx.eventBus();
			DeliveryOptions options = new DeliveryOptions();
			options.setSendTimeout(5000);
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