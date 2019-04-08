package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * this is the database operation class
 *
 */

public class DatabaseService extends AbstractVerticle {

	public void callDatabase(String funcSP, JsonArray fields, String operation, Message<JsonObject> message,
			EventBus esbBus) {

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

				if (operation.equals("retrieveInvoice")) {
					message.reply(responseFields);

				}
				if ((operation.equals("retrieve")) || (operation.equals("retrieveAllNotices"))
						|| (operation.equals("retrieveAllBranches")) || (operation.equals("retrieveAllEstates"))
						|| (operation.equals("retrieveAllPaybills")) || (operation.equals("retrieveByProvider"))
						|| (operation.equals("retrieveAllLandlords")) || (operation.equals("retrieveAll"))
						|| (operation.equals("retrieveAllTowns"))) {

					feedback.put("data", params);
				} else {
					feedback.put("data", params.getJsonObject(0));
				}
				System.out.println("my fields=" + feedback);

				message.reply(feedback);

			} else {
				System.out.println("my fields=" + artxn.cause().getMessage());

				message.fail(0, artxn.cause().getMessage());

			}
		});
	}
}
