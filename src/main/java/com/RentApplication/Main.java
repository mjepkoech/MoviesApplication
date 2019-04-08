package com.RentApplication;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * this is the main class
 *
 */

public class Main {
	public static void main(String args[]) {
		Vertx vertx = Vertx.vertx();
		int instances = 1;

		JsonObject myconfig = new JsonObject();

		DeploymentOptions options = new DeploymentOptions().setConfig(myconfig).setInstances(instances).setHa(true);

		vertx.deployVerticle(App.class.getName(), options);
	}
}
