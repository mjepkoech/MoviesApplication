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
 * Tenants CRUD
 * 
 * @author mjepkoech
 *
 */
public class TenantMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("tenant");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("I have received a message: " + reqdata);
			String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details").getString("action")
					.trim();
			JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");
			try {

				JsonArray fields = new JsonArray();
				String funcSP = "";
				
				switch (operation) {
				case "add":
					
					funcSP = "sp_addTenant";
					String firstname = data.getString("firstname").trim();
					String lastname = data.getString("lastname").trim();
					String mobilenumber = data.getString("mobilenumber").trim();
					String password = data.getString("password").trim();
					fields = new JsonArray().add(firstname).add(lastname).add(mobilenumber).add(password);

					break;

				case "create":
					
					funcSP = "sp_createTenant";
					firstname = data.getString("firstname").trim();
					lastname = data.getString("lastname").trim();
					mobilenumber = data.getString("mobilenumber").trim();
					String landlord = data.getString("landlord").trim();
					fields = new JsonArray().add(firstname).add(lastname).add(mobilenumber).add(landlord);

					break;
				case "update":
					
					funcSP = "sp_updateTenant";
					String Id = data.getString("id").trim();
					firstname = data.getString("firstname").trim();
					lastname = data.getString("lastname").trim();
					mobilenumber = data.getString("mobilenumber").trim();
					password = data.getString("password").trim();
					fields = new JsonArray().add(Id).add(firstname).add(lastname).add(mobilenumber).add(password);

					break;

				case "retrieve":
					
					funcSP = "sp_getTenants";
					fields = new JsonArray().add(operation);

					break;

				case "retrieveByPhone":
					
					funcSP = "sp_getTenant";
					mobilenumber = data.getString("mobilenumber").trim();
					Id = data.getString("id").trim();
					fields = new JsonArray().add(Id).add(mobilenumber);

					break;

				case "delete":
					funcSP = "sp_removeTenant";

					Id = data.getString("id").trim();
					String user_Id = data.getString("user_id").trim();
					fields = new JsonArray().add(Id).add(user_Id);

					break;

				default:
					System.out.println("No such operation: " + operation);
					break;
				}

				callDatabase(message, operation, data, fields, funcSP);
			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}
		});
		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Tenant handler has reached all nodes");
			} else {
				System.out.println("Tenant Registration failed!");
			}
		});
	}

	/**
	 * Method calls database adapter
	 * @param message
	 * @param operation
	 * @param data
	 * @param fields
	 * @param funcSP
	 */
	private void callDatabase(Message<JsonObject> message, String operation, JsonObject data, JsonArray fields,
			String funcSP) {
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
				System.out.println("Received reply from database ms: " + responseFields);
				String to = data.getString("mobilenumber");
				JsonArray params = responseFields.getJsonArray("params");

				JsonObject dbfields = new JsonObject();
				String otp = "";
				int param_size = params.size();
				for (int i = 0; i < param_size; i++) {
					dbfields = params.getJsonObject(i);
					if (dbfields.containsKey("otp")) {
						otp = dbfields.getString("otp");
						break;
					}
				}
				if (operation.equals("add")) {
					System.out.println("Operation: " + operation);

					String sms = "Dear " + data.getString("firstname")
							+ ", you have successfully registered for EasyRent App. Your OTP token is " + otp;
					JsonObject smsdata = new JsonObject().put("smsmessage", sms).put("mobilenumber", to);

					MessageProducer<JsonObject> otpsend = esbBus.publisher("notification", options);
					otpsend.send(smsdata, (AsyncResult<Message<JsonObject>> otpartxn) -> {
						if (otpartxn.succeeded()) {
							JsonObject res = otpartxn.result().body();
							System.out.println("Received reply from SMS Mgr: " + res);
							message.reply(responseFields);

						} else {
							message.fail(0, otpartxn.cause().getMessage());
							System.out.println("Received reply from SMS Mgr: " + otpartxn.cause().getMessage());
						}
					});
				}
				JsonObject feedback = new JsonObject();

				if (operation.equals("retrieve")) {
					feedback.put("data", params);
				} else {
					feedback.put("data", params.getJsonObject(0));
				}
				message.reply(feedback);
			} else {
				message.fail(0, artxn.cause().getMessage());
			}
		});
	}
}