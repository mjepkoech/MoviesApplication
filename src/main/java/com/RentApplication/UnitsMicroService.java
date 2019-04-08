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
 * Units CRUD
 * 
 * @author mjepkoech
 *
 */
public class UnitsMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("manageUnits");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("UnitsMicroservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				unitCases(message, reqdata, operation, data);
				
			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Units handler has reached all nodes");
			} else {
				System.out.println("Units failed!");
			}
		});
	}

	/**
	 * Method to handle the units cases
	 * @param message
	 * @param reqdata
	 * @param operation
	 * @param data
	 */
	private void unitCases(Message<JsonObject> message, JsonObject reqdata, String operation, JsonObject data) {
		JsonArray fields = new JsonArray();
		String funcSP = "";

		switch (operation) {
		case "add":
			
			funcSP = "sp_addUnit";
			String name = data.getString("name").trim();
			String rent_amount = data.getString("rent_amount").trim();
			String deposit_amount = data.getString("deposit_amount").trim();
			String water_bill_number = data.getString("water_bill_number").trim();
			String electricity_bill_number = data.getString("electricity_bill_number").trim();
			String bedrooms_ensuite = data.getString("bedrooms_ensuite").trim();
			String bedrooms = data.getString("bedrooms").trim();
			String bathrooms = data.getString("bathrooms").trim();
			String floor = data.getString("floor").trim();
			String balcony = data.getString("balcony").trim();
			String currency = data.getString("currency").trim();
			String building = data.getString("building").trim();
			fields = new JsonArray().add(name).add(rent_amount).add(deposit_amount).add(water_bill_number).add(electricity_bill_number)
					.add(bedrooms_ensuite).add(bedrooms).add(bathrooms).add(floor).add(balcony).add(currency).add(building);

			break;
		case "update":
			
			funcSP = "sp_updateUnit";
			String id = data.getString("id").trim();
			 name = data.getString("name").trim();
			 rent_amount = data.getString("rent_amount").trim();
			 deposit_amount = data.getString("deposit_amount").trim();
			 water_bill_number = data.getString("water_bill_number").trim();
			 electricity_bill_number = data.getString("electricity_bill_number").trim();
			 bedrooms_ensuite = data.getString("bedrooms_ensuite").trim();
			 bedrooms = data.getString("bedrooms").trim();
			 bathrooms = data.getString("bathrooms").trim();
			 floor = data.getString("floor").trim();
			 balcony = data.getString("balcony").trim();
			 currency = data.getString("currency").trim();
			 building = data.getString("building").trim();
			fields = new JsonArray().add(id).add(name).add(rent_amount).add(deposit_amount).add(water_bill_number).add(electricity_bill_number)
					.add(bedrooms_ensuite).add(bedrooms).add(bathrooms).add(floor).add(balcony).add(currency).add(building);

			break;

		case "retrieve":
			
			funcSP = "sp_getUnits";
			building = data.getString("building").trim();
			fields = new JsonArray().add(building);

			break;

		case "retrieveVacant":
			
			funcSP = "sp_getVacantUnits";
			fields = new JsonArray().add(operation);

			break;

		case "tenantUnits":
			
			funcSP = "sp_getTenantUnits";
			id = data.getString("tenant_id").trim();
			fields = new JsonArray().add(id);

			break;

		case "reserve":
			
			funcSP = "sp_bookUnit";
			String unit = data.getString("unit_id").trim();
			String tenant = data.getString("tenant_id").trim();
			fields = new JsonArray().add(unit).add(tenant);

			break;

		case "link":
			
			funcSP = "sp_linkTenantToUnit";
			String mobilenumber = data.getString("mobilenumber").trim();
			unit = data.getString("unit").trim();
			String landlord = data.getString("landlord").trim();
			fields = new JsonArray().add(mobilenumber).add(unit).add(landlord);

			break;

		case "delete":
			
			funcSP = "sp_removeUnit";
			unit = data.getString("id").trim();
			fields = new JsonArray().add(unit);

			break;

		default:
			System.out.println("No such operation: " + operation);
			break;
		}
		System.out.println("No such operation: " + fields);
		callDatabase(message, reqdata, operation, fields, funcSP);
	}

	/**
	 * Method calls the database adapter
	 * @param message
	 * @param reqdata
	 * @param operation
	 * @param fields
	 * @param funcSP
	 */
	private void callDatabase(Message<JsonObject> message, JsonObject reqdata, String operation, JsonArray fields,
			String funcSP) {
		JsonObject authData = new JsonObject();
		authData.put("storedprocedure", funcSP);
		authData.put("params", fields);
		System.out.println("Received reply from units Session Mgr: " + authData);


		EventBus esbBus = vertx.eventBus();
		DeliveryOptions options = new DeliveryOptions();
		int time = 5000;
		options.setSendTimeout(time);
		MessageProducer<JsonObject> producer = esbBus.publisher("DATABASEACCESS", options);
		producer.send(authData, (AsyncResult<Message<JsonObject>> artxn) -> {
			if (artxn.succeeded()) {
				JsonObject responseFields = artxn.result().body();
				System.out.println("Received reply from units Session Mgr: " + responseFields);

				JsonObject feedback = new JsonObject();
				JsonArray params = responseFields.getJsonArray("params");
				JsonObject dbfields = new JsonObject();
				String unit_Id = "";
				int param_size = params.size();
				for (int i = 0; i < param_size; i++) {
					dbfields = params.getJsonObject(i);
					if (dbfields.containsKey("id")) {
						unit_Id = dbfields.getInteger("id").toString();
						break;
					}
				}
				
				if (operation.equals("update")) {
					System.out.println("Received reply from Units MS: " + reqdata);
				}

				if (operation.equals("add")) {
					reqdata.getJsonObject("data").getJsonObject("transaction_details").put("unit_id", unit_Id);
					System.out.println("Received reply from Units MS: " + reqdata);

					MessageProducer<JsonObject> otpsend = esbBus.publisher("unitCharges", options);
					otpsend.send(reqdata, (AsyncResult<Message<JsonObject>> otpartxn) -> {
						if (otpartxn.succeeded()) {
							JsonObject res = otpartxn.result().body();
							System.out.println("Received reply from unit charges Mgr: " + res);

							message.reply(responseFields);

						} else {
							message.fail(0, otpartxn.cause().getMessage());
							System.out.println(
									"Received reply from unit charges: " + otpartxn.cause().getMessage());
						}
					});
					feedback.put("data", params.getJsonObject(0));
				}

				if ((operation.equals("retrieve")) || (operation.equals("retrieveVacant"))
						|| (operation.equals("tenantUnits"))) {
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