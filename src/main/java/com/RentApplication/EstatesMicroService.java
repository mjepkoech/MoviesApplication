package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Estates CRUD
 * 
 * @author mjepkoech
 *
 */
public class EstatesMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("manageEstates");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("EstatesMicroservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				estateCases(message, operation, data);

			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Estates handler has reached all nodes");
			} else {
				System.out.println("Estates failed!");
			}
		});
	}

	/**
	 * Method to handle estates operations
	 * @param message
	 * @param operation
	 * @param data
	 */
	private void estateCases(Message<JsonObject> message, String operation, JsonObject data) {
		JsonArray fields = new JsonArray();
		String funcSP = "";

		switch (operation) {
		case "add":
			
			funcSP = "sp_addEstate";
			String name = data.getString("name").trim();
			String street = data.getString("street").trim();
			String town = data.getString("town").trim();
			String landlord = data.getString("landlord").trim();
			fields = new JsonArray().add(name).add(street).add(town).add(landlord);

			break;
		case "update":
			
			funcSP = "sp_updateEstate";
			String id = data.getString("id").trim();
			name = data.getString("name").trim();
			street = data.getString("street").trim();
			town = data.getString("town").trim();
			landlord = data.getString("landlord").trim();
			fields = new JsonArray().add(id).add(name).add(street).add(town).add(landlord);

			break;

		case "retrieve":
			
			funcSP = "sp_getEstate";
			landlord = data.getString("landlord").trim();
			fields = new JsonArray().add(landlord);

			break;

		case "retrieveEstate":
			
			funcSP = "sp_getEstateById";
			id = data.getString("id").trim();
			fields = new JsonArray().add(id);

			break;

		case "retrieveAllEstates":

			funcSP = "sp_getAllEstates";
			fields = new JsonArray().add(operation);

			break;

		case "delete":
			
			funcSP = "sp_removeEstate";
			String estate = data.getString("id").trim();
			landlord = data.getString("landlord").trim();
			fields = new JsonArray().add(estate).add(landlord);

			break;

		default:
			System.out.println("No such operation: " + operation);
			break;
		}
		EventBus esbBus = vertx.eventBus();
		new DatabaseService().callDatabase(funcSP, fields, operation, message, esbBus);
	}

}