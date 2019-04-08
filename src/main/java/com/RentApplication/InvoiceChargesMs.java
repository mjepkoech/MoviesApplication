package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Invoice charges CRUD
 * @author mjepkoech
 *
 */
public class InvoiceChargesMs extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("invoiceCharges");
		consumer.handler((Message<JsonObject> message) -> { 
			JsonObject reqdata = message.body();

			try {

				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();

				JsonArray fields = new JsonArray();
				String funcSP = "";
				if (operation.equals("add")) {
					JsonArray details = data.getJsonArray("charges");

					addCharge(message, reqdata, data, operation, details);

				} else {

					switch (operation) {
					
					case "update":
						System.out.println("Invoice Char: " + data);

						funcSP = "sp_updateInvoiceCharge";
						String id = data.getString("id").trim();
						String invoice_id = data.getString("invoice_id").trim();
						String charge_name_id = data.getString("charge_name_id").trim();
						String charge_id = data.getString("charge_id").trim();
						String channel = reqdata.getJsonObject("data").getJsonObject("channel_details")
								.getString("channel").trim();
						System.out.println("Invoice Char: " + data);

						fields = new JsonArray().add(id).add(invoice_id).add(charge_name_id).add(charge_id).add(channel);
						System.out.println("Invoice Char: " + fields);

						break;

					case "retrieveInvoice":

						funcSP = "sp_getInvoiceCharges";
					    id = data.getString("invoice_id").trim();
						fields = new JsonArray().add(id);

						break;

					case "delete":
						
						funcSP = "sp_removeInvoiceCharge";
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
				System.out.println("Invoice charges handler has reached all nodes");
			} else {
				System.out.println("Invoice charges handler failed!");
			}
		});
	}

	/**
	 * Method to add charges in form of a loop
	 * @param message
	 * @param reqdata
	 * @param data
	 * @param operation
	 * @param details
	 */
	private void addCharge(Message<JsonObject> message, JsonObject reqdata, JsonObject data, String operation,
			JsonArray details) {
		JsonArray fields;
		String funcSP;
		JsonObject dbfields;
		int details_size = details.size();

		for (int i = 0; i < details_size; i++) {
			dbfields = details.getJsonObject(i);

			funcSP = "sp_addInvoiceCharges";
			String id = data.getString("invoice_id").trim();
			String charge_name_id = dbfields.getString("charge_name_id").trim();
			String charge_id = dbfields.getString("charge_id").trim();
			String channel = reqdata.getJsonObject("data").getJsonObject("channel_details")
					.getString("channel").trim();
			fields = new JsonArray().add(id).add(charge_name_id).add(charge_id).add(channel);

			EventBus esbBus = vertx.eventBus();
		    new DatabaseService().callDatabase(funcSP, fields, operation, message, esbBus);
		}
	}
}