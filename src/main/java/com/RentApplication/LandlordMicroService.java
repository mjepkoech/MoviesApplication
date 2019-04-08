package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Landlords CRUD
 * 
 * @author mjepkoech
 *
 */
public class LandlordMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("landlord");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("LandlordMicroservice has received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				JsonArray fields = new JsonArray();

				String funcSP = "";

				switch (operation) {
				case "add":
					
					funcSP = "sp_addLandlords";
					String firstname = data.getString("firstname").trim();
					String lastname = data.getString("lastname").trim();
					String mobilenumber = data.getString("mobilenumber").trim();
					String idnumber = data.getString("idnumber").trim();
					String email = data.getString("email").trim();
					fields = new JsonArray().add(firstname).add(lastname).add(mobilenumber).add(idnumber).add(email);

					break;
				case "update":
					funcSP = "sp_updateLandlord";

					String id = data.getString("id").trim();
					firstname = data.getString("firstname").trim();
					lastname = data.getString("lastname").trim();
					mobilenumber = data.getString("mobilenumber").trim();
					idnumber = data.getString("idnumber").trim();
					email = data.getString("email").trim();
					fields = new JsonArray().add(id).add(firstname).add(lastname).add(mobilenumber).add(idnumber).add(email);

					break;

				case "retrieve":

					funcSP = "sp_getLandlords";
					fields = new JsonArray().add(operation);

					break;

				case "delete":

					funcSP = "sp_removeLandlord";
					id = data.getString("id").trim();
					String admin_id = data.getString("admin_id").trim();
					fields = new JsonArray().add(id).add(admin_id);

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
				System.out.println("Landlord handler registration has reached all nodes");
			} else {
				System.out.println("Landlord Registration failed!");
			}
		});
	}

}