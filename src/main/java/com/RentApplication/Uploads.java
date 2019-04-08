package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Banks CRUD
 *
 * @author mjepkoech
 *
 */

public class Uploads extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("uploads");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("Uploads Microservice received a message: " + reqdata);
			try {
				String operation = "add";
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				JsonArray fields = new JsonArray();

				String funcSP = "saveImage";
				String path = "D:\\Millicent/";

				String base64 = data.getString("file");
				convertAndSave(base64, (path + data.getString("fileName")));
				System.out.println("Saved");

				String file_path = path + data.getString("fileName");

				fields = new JsonArray().add(file_path);
				EventBus esbBus = vertx.eventBus();

				new DatabaseService().callDatabase(funcSP, fields, operation, message, esbBus);

			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Uploads handler has reached all nodes");
			} else {
				System.out.println("Uploads handler failed!");
			}
		});
	}

	private static void convertAndSave(String base64, String filePath) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(filePath);
		fos.write(Base64.decodeBase64(base64));
//		return fos;
		fos.close();

	}
}
