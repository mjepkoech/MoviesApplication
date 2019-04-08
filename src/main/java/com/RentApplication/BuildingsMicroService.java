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
 * Buildings CRUD
 * 
 * @author mjepkoech
 *
 */
public class BuildingsMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("manageBuildings");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("BuildingsMicroservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				JsonArray fields = new JsonArray();

				String funcSP = "";

				switch (operation) {
				case "add":

					funcSP = "sp_addBuilding";

					String name = data.getString("name").trim();
					String floors = data.getString("floors").trim();
					String rent_due_date = data.getString("rent_due_date").trim();
					String street = data.getString("street").trim();
					String description = data.getString("description").trim();
					String pets_allowed = data.getString("pets_allowed").trim();
					String parking = data.getString("parking").trim();
					String perimeter_fence = data.getString("perimeter_fence").trim();
					String playground = data.getString("playground").trim();
					String swimmingpool = data.getString("swimmingpool").trim();
					String estate = data.getString("estate").trim();
					String vacate_notice_period = data.getString("vacate_notice_period").trim();
					String reservation_period = data.getString("reservation_period").trim();
					String type = data.getString("type").trim();
					String landlord = data.getString("landlord").trim();

					fields = new JsonArray().add(name).add(floors).add(rent_due_date).add(street).add(description)
							.add(pets_allowed).add(parking).add(perimeter_fence).add(playground).add(swimmingpool)
							.add(vacate_notice_period).add(reservation_period).add(estate).add(type).add(landlord);
					break;
				case "update":

					funcSP = "sp_updateBuilding";

					String id = data.getString("id").trim();
					name = data.getString("name").trim();
					floors = data.getString("floors").trim();
					rent_due_date = data.getString("rent_due_date").trim();
					street = data.getString("street").trim();
					description = data.getString("description").trim();
					pets_allowed = data.getString("pets_allowed").trim();
					parking = data.getString("parking").trim();
					perimeter_fence = data.getString("perimeter_fence").trim();
					playground = data.getString("playground").trim();
					swimmingpool = data.getString("swimmingpool").trim();
					estate = data.getString("estate").trim();
					vacate_notice_period = data.getString("vacate_notice_period").trim();
					reservation_period = data.getString("reservation_period").trim();
					type = data.getString("type").trim();
					landlord = data.getString("landlord").trim();

					fields = new JsonArray().add(id).add(name).add(floors).add(rent_due_date).add(street).add(description)
							.add(pets_allowed).add(parking).add(perimeter_fence).add(playground).add(swimmingpool)
							.add(vacate_notice_period).add(reservation_period).add(estate).add(type).add(landlord);

					break;

				case "retrieve":
					funcSP = "sp_getBuildings";
					landlord = data.getString("landlord").trim();
					estate = data.getString("estate").trim();

					fields = new JsonArray().add(landlord).add(estate);

					break;

				case "landlordBuildings":
					funcSP = "sp_getLandlordBuildings";
					landlord = data.getString("landlord").trim();

					fields = new JsonArray().add(landlord);

					break;

				case "retrieveBuilding":
					funcSP = "sp_getBuildingById";

					id = data.getString("id").trim();

					fields = new JsonArray().add(id);

					break;

				case "retrieveAllBuildings":

					funcSP = "sp_getAllBuildings";

					fields = new JsonArray().add(operation);

					break;

				case "delete":
					funcSP = "sp_removeBuilding";

					String building = data.getString("id").trim();
					landlord = data.getString("landlord").trim();

					fields = new JsonArray().add(building).add(landlord);

					break;

				default:
					System.out.println("No such operation: " + operation);
					break;
				}

				callDatabase(message, reqdata, operation, fields, funcSP);
			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Buildings handler has reached all nodes");
			} else {
				System.out.println("Buildings handler failed!");
			}
		});
	}

	/**
	 * Sends building details to database adapter
	 * 
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
				JsonObject dbfields = new JsonObject();
				int building_Id = 0;
				int param_size = params.size();
				for (int i = 0; i < param_size; i++) {
					dbfields = params.getJsonObject(i);
					if (dbfields.containsKey("id")) {
						building_Id = dbfields.getInteger("id");
						break;
					}
				}

				if (operation.equals("add")) {
					savePaymodes(message, reqdata, esbBus, options, feedback, params, building_Id);

				}

				if ((operation.equals("retrieve")) || (operation.equals("retrieveAllBuildings"))
						|| (operation.equals("landlordBuildings"))) {
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

	/**
	 * Saves building payment modes and building charges
	 * @param message
	 * @param reqdata
	 * @param esbBus
	 * @param options
	 * @param feedback
	 * @param params
	 * @param building_Id
	 */
	private void savePaymodes(Message<JsonObject> message, JsonObject reqdata, EventBus esbBus, DeliveryOptions options,
			JsonObject feedback, JsonArray params, int building_Id) {
		JsonArray pay_acc = reqdata.getJsonObject("data").getJsonObject("transaction_details").getJsonArray("pay_acc");
		String act = "add";
		JsonObject transaction_details = new JsonObject().put("pay_acc", pay_acc).put("building_id", building_Id)
				.put("action", act);
		JsonObject transaction_data = new JsonObject().put("transaction_details", transaction_details);
		JsonObject sms = new JsonObject().put("data", transaction_data);
		MessageProducer<JsonObject> otpsend = esbBus.publisher("paymentModes", options);
		otpsend.send(sms, (AsyncResult<Message<JsonObject>> otpartxn) -> {
			if (otpartxn.succeeded()) {
				JsonObject res = otpartxn.result().body();
				System.out.println("Received reply from Payments Mgr: " + res);
				JsonArray pay_accounts = res.getJsonArray("params");
				params.add(pay_accounts);

				message.reply(pay_accounts);

			} else {
//									 message.fail(0, otpartxn.cause().getMessage());
				System.out.println("Received reply from Payments Mgr: " + otpartxn.cause().getMessage());

			}

		});

		JsonArray charge_details = reqdata.getJsonObject("data").getJsonObject("transaction_details")
				.getJsonArray("charges");
		transaction_details = new JsonObject().put("charge_details", charge_details).put("building_id", building_Id)
				.put("action", act);
		System.out.println("charges: " + transaction_details);

		transaction_data = new JsonObject().put("transaction_details", transaction_details);
		sms = new JsonObject().put("data", transaction_data);
		MessageProducer<JsonObject> chargesend = esbBus.publisher("buildingCharges", options);
		chargesend.send(sms, (AsyncResult<Message<JsonObject>> otpartxn) -> {
			if (otpartxn.succeeded()) {
				JsonObject res = otpartxn.result().body();
				System.out.println("Received reply from Building charges Mgr: " + res);
				JsonArray charges = res.getJsonArray("params");
				params.add(charges);

				message.reply(charges);

			} else {
				message.fail(0, otpartxn.cause().getMessage());
				System.out.println("Received reply from Building charges Mgr: " + otpartxn.cause().getMessage());

			}

		});
		feedback.put("data", params.getJsonObject(0));
	}

}