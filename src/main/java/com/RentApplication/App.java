package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * this is the main class
 *
 */
public class App extends AbstractVerticle {
	public void start() throws Exception {

//public class App extends AbstractVerticle{
//	public static void main(String args[]) {
		Vertx vertx = Vertx.vertx();
		int instances = 1;

		JsonObject myconfig = new JsonObject();

		DeploymentOptions options = new DeploymentOptions().setConfig(myconfig).setInstances(instances).setHa(true);

		vertx.deployVerticle(APIGateway.class.getName(), options);
		vertx.deployVerticle(ChannelAuthenticator.class.getName(), options);
		vertx.deployVerticle(DatabaseAdapter.class.getName(), options);
		vertx.deployVerticle(TenantMicroService.class.getName(), options);
		vertx.deployVerticle(LoginMicroService.class.getName(), options);
		vertx.deployVerticle(LandlordMicroService.class.getName(), options);
		vertx.deployVerticle(NotificationsMicroservice.class.getName(), options);
		vertx.deployVerticle(OTPMicroservice.class.getName(), options);
		vertx.deployVerticle(PasswordsMicroservice.class.getName(), options);
		vertx.deployVerticle(NewPasswordsMicroService.class.getName(), options);
		vertx.deployVerticle(BuildingsMicroService.class.getName(), options);
		vertx.deployVerticle(EstatesMicroService.class.getName(), options);
		vertx.deployVerticle(UnitsMicroService.class.getName(), options);
		vertx.deployVerticle(CountiesMicroService.class.getName(), options);
		vertx.deployVerticle(TownsMicroService.class.getName(), options);
		vertx.deployVerticle(BanksMicroService.class.getName(), options);
		vertx.deployVerticle(BankBranchesMicroService.class.getName(), options);
		vertx.deployVerticle(PaymentModesMicroService.class.getName(), options);
		vertx.deployVerticle(VacationNoticeMicroService.class.getName(), options);
		vertx.deployVerticle(LandlordBankAccountsMicroService.class.getName(), options);
		vertx.deployVerticle(BuildingChargesMicroService.class.getName(), options);
		vertx.deployVerticle(UnitChargesMicroService.class.getName(), options);
		vertx.deployVerticle(DatabaseService.class.getName(), options);
		vertx.deployVerticle(PaybillMicroService.class.getName(), options);
		vertx.deployVerticle(PaybillProvidersMs.class.getName(), options);
		vertx.deployVerticle(MpesaCheckoutMS.class.getName(), options);
		vertx.deployVerticle(MpesaCheckoutCallbackMS.class.getName(), options);
		vertx.deployVerticle(LandlordAccounts.class.getName(), options);
		vertx.deployVerticle(InvoicesMs.class.getName(), options);
		vertx.deployVerticle(InvoiceChargesMs.class.getName(), options);
		vertx.deployVerticle(Uploads.class.getName(), options);

	}
}