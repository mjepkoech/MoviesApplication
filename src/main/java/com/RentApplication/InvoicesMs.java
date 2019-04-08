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
 * Invoices CRUD
 * 
 * @author mjepkoech
 *
 */

public class InvoicesMs extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("manageInvoices");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("Invoices Ms received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				JsonArray fields = new JsonArray();

				String funcSP = "";
				String invoice_number = "INV-";
				EventBus esbBus = vertx.eventBus();

				switch (operation) {
				case "add":
					funcSP = "sp_addInvoice";

					String user_id = data.getString("user_id").trim();
					String unit_id = data.getString("unit_id").trim();
					String currency = data.getString("currency").trim();
					String amount = data.getString("amount").trim();
					String channel = reqdata.getJsonObject("data").getJsonObject("channel_details").getString("channel")
							.trim();

					fields = new JsonArray().add(amount).add(currency).add(invoice_number).add(channel).add(user_id)
							.add(unit_id);
					break;

				case "update":
					funcSP = "sp_updateInvoice";

					String id = data.getString("id").trim();
					user_id = data.getString("user_id").trim();
					unit_id = data.getString("unit_id").trim();
					currency = data.getString("currency").trim();
					amount = data.getString("amount").trim();
					channel = reqdata.getJsonObject("data").getJsonObject("channel_details").getString("channel")
							.trim();
					fields = new JsonArray().add(id).add(amount).add(currency).add(channel).add(user_id)
							.add(unit_id);

					break;

				case "retrieveInvoice":

					funcSP = "sp_getInvoice";
					id = data.getString("invoice_id").trim();
					fields = new JsonArray().add(id);

					break;

				case "retrieve":

					funcSP = "sp_getUserInvoices";
					user_id = data.getString("user_id").trim();
					unit_id = data.getString("unit_id").trim();
					fields = new JsonArray().add(user_id).add(unit_id);

					break;

				case "delete":

					funcSP = "sp_removeInvoice";
					id = data.getString("id").trim();
					user_id = data.getString("user_id").trim();
					fields = new JsonArray().add(id).add(user_id);

					break;

				default:
					System.out.println("No such operation: " + operation);
					break;
				}

				callDatabase(message, reqdata, operation, fields, funcSP, esbBus);
			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Invoices handler has reached all nodes");
			} else {
				System.out.println("Invoices handler failed!");
			}
		});
	}

	/**
	 * This method adds invoice details as well as invoice charges.
	 * @param message
	 * @param reqdata
	 * @param operation
	 * @param fields
	 * @param funcSP
	 * @param esbBus
	 */
	private void callDatabase(Message<JsonObject> message, JsonObject reqdata, String operation, JsonArray fields,
		String funcSP, EventBus esbBus) {
		JsonObject authData = new JsonObject();
		authData.put("storedprocedure", funcSP);
		authData.put("params", fields);
		System.out.println("my fields=" + authData);

		DeliveryOptions options = new DeliveryOptions();
		int time = 5000;
		options.setSendTimeout(time);
		System.out.println("my fields=" + authData);

		MessageProducer<JsonObject> producer = esbBus.publisher("DATABASEACCESS", options);
		producer.send(authData, (AsyncResult<Message<JsonObject>> artxn) -> {
			if (artxn.succeeded()) {
				JsonObject responseFields = artxn.result().body();
				System.out.println("Received reply from Session Mgr: " + responseFields);

				JsonObject feedback = new JsonObject();
				JsonArray params = responseFields.getJsonArray("params");
				JsonObject dbfields = new JsonObject();
				String invoice_id = "";
				int param_size = params.size();
				for (int i = 0; i < param_size; i++) {
					dbfields = params.getJsonObject(i);
					if (dbfields.containsKey("id")) {
						invoice_id = dbfields.getInteger("id").toString();
						break;

					}
				}
				System.out.println("The id is " + invoice_id);
				if (operation.equals("add")) {
					reqdata.getJsonObject("data").getJsonObject("transaction_details").put("invoice_id", invoice_id);
					System.out.println("Received reply from Units MS: " + reqdata);

					saveCharges(message, reqdata, esbBus, options, responseFields, feedback, params);

				}

				else if (operation.equals("retrieveInvoice")) {
					reqdata.getJsonObject("data").getJsonObject("transaction_details").put("invoice_id", invoice_id);
					System.out.println("Received reply from Units MS: " + reqdata);

					retrieveCharges(message, reqdata, esbBus, options, feedback, params);
				}
				else if ((operation.equals("retrieveAll")) || (operation.equals("retrieve"))) {

					feedback.put("data", params);
					message.reply(feedback);

				} else {
					feedback.put("data", params.getJsonObject(0));
					message.reply(feedback);

				}
			} else {
				message.fail(0, artxn.cause().getMessage());

			}
		});
	}

	/**
	 * method to send charges for saving purposes
	 * @param message
	 * @param reqdata
	 * @param esbBus
	 * @param options
	 * @param responseFields
	 * @param feedback
	 * @param params
	 */
	private void saveCharges(Message<JsonObject> message, JsonObject reqdata, EventBus esbBus, DeliveryOptions options,
			JsonObject responseFields, JsonObject feedback, JsonArray params) {
		MessageProducer<JsonObject> otpsend = esbBus.publisher("invoiceCharges", options);
		otpsend.send(reqdata, (AsyncResult<Message<JsonObject>> otpartxn) -> {
			if (otpartxn.succeeded()) {
				JsonObject res = otpartxn.result().body();
				System.out.println("Received reply from invoice charges Mgr: " + res);
				message.reply(responseFields);

			} else {
				message.fail(0, otpartxn.cause().getMessage());
				System.out.println("Received reply from unit charges: " + otpartxn.cause().getMessage());
			}
		});
		feedback.put("data", params.getJsonObject(0));
		message.reply(feedback);
	}

	/**
	 * method to retrieve charges
	 * @param message
	 * @param reqdata
	 * @param esbBus
	 * @param options
	 * @param feedback
	 * @param params
	 */
	private void retrieveCharges(Message<JsonObject> message, JsonObject reqdata, EventBus esbBus, DeliveryOptions options,
			JsonObject feedback, JsonArray params) {
		MessageProducer<JsonObject> otpsend = esbBus.publisher("invoiceCharges", options);
		otpsend.send(reqdata, (AsyncResult<Message<JsonObject>> otpartxn) -> {
			if (otpartxn.succeeded()) {
				JsonObject res = otpartxn.result().body();
				System.out.println("Received reply from invoice charges Mgr: " + res);
				JsonArray params1 = res.getJsonArray("params");

				params.getJsonObject(0).put("charges", params1);
				feedback.put("data", params.getJsonObject(0));
				message.reply(feedback);

			} else {
				message.fail(0, otpartxn.cause().getMessage());
			}
		});
	}
}

