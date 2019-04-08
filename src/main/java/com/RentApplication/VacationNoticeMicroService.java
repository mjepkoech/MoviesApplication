package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * House vacation notices CRUD
 * 
 * @author mjepkoech
 *
 */
public class VacationNoticeMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("vacationNotification");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("vacation Microservice received a message: " + reqdata);
			try {
				String operation = reqdata.getJsonObject("data").getJsonObject("transaction_details")
						.getString("action").trim();
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");
				JsonArray fields = new JsonArray();
				String funcSP = "";

				switch (operation) {
				case "add":
					funcSP = "sp_addVacationNotice";

					String sender_id = data.getString("sender_id").trim();
					String unit_id = data.getString("unit_id").trim();
					String notice = data.getString("message").trim();
					String date = data.getString("starting_date").trim();

					fields = new JsonArray().add(sender_id).add(unit_id).add(notice).add(date);

					break;
				case "update":
					
					funcSP = "sp_updateVacationNotice";
					String id = data.getString("id").trim();
					sender_id = data.getString("sender_id").trim();
					unit_id = data.getString("unit_id").trim();
					notice = data.getString("message").trim();
					date = data.getString("starting_date").trim();
					fields = new JsonArray().add(id).add(sender_id).add(unit_id).add(notice).add(date);

					break;

				case "retrieveAllNotices":

					funcSP = "sp_getAllNotices";
					id = data.getString("unit_id").trim();
					sender_id = data.getString("user_id").trim();
					fields = new JsonArray().add(id).add(sender_id);

					break;

				case "delete":
					
					funcSP = "sp_removeVacationNotice";
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

			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Vacation Notices handler has reached all nodes");
			} else {
				System.out.println("Vacation Notices handler failed!");
			}
		});
	}
}