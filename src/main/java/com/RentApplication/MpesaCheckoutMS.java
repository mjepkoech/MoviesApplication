//package com.RentApplication;
//
//import io.vertx.core.AbstractVerticle;
//import io.vertx.core.AsyncResult;
//import io.vertx.core.eventbus.DeliveryOptions;
//import io.vertx.core.eventbus.EventBus;
//import io.vertx.core.eventbus.Message;
//import io.vertx.core.eventbus.MessageConsumer;
//import io.vertx.core.eventbus.MessageProducer;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.web.client.WebClient;
//import io.vertx.ext.web.client.HttpResponse;
//
//public class MpesaCheckoutMS extends AbstractVerticle {
//
//	@Override
//	public void start() throws Exception {
//		EventBus eb = vertx.eventBus();
//
//		MessageConsumer<JsonObject> consumer = eb.consumer("paymentCheckout");
//		consumer.handler((Message<JsonObject> message) -> {
//
//			JsonObject reqdata = message.body();
//			JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");
//			String transaction_type = reqdata.getJsonObject("data").getJsonObject("transaction_details")
//					.getString("transaction_type").trim();
//
//			try {
//				JsonArray fields = new JsonArray();
//				String funcSP = "";
////				switch (transaction_type) {
////				case "MPESACHECKOUT":
//					funcSP = "sp_saveTransaction";
//
//					String currency = data.getString("currency").trim();
//					String amount = data.getString("amount").trim();
//					String date_paid = data.getString("date_paid").trim();
//					String reference_number = data.getString("ref").trim();
////					String reference_number = UUID.randomUUID().toString();
//					String payment_type = data.getString("payment_type").trim();
//					String mobile_number = data.getString("mobile_number").trim();
//					String paid_by = data.getString("userId").trim();
//					String password = data.getString("pass").trim();
//					String provider_id = data.getString("provider_id").trim();
//					String channel = reqdata.getJsonObject("data").getJsonObject("channel_details").getString("channel")
//							.trim();
//					String invoice_id = data.getString("invoice_id").trim();
//
//					fields = new JsonArray().add(currency).add(amount).add(date_paid).add(reference_number)
//							.add(payment_type).add(mobile_number).add(paid_by).add(password).add(provider_id)
//							.add(channel).add(invoice_id);
////					break;
//
////				case "CARDCHECKOUT":
////					funcSP = "sp_saveTransaction";
////
////					currency = data.getString("currency").trim();
////					amount = data.getString("amount").trim();
////					date_paid = data.getString("date_paid").trim();
////					reference_number = data.getString("ref").trim();
////					payment_type = data.getString("payment_type").trim();
////					mobile_number = data.getString("mobile_number").trim();
////					paid_by = data.getString("userId").trim();
////					password = data.getString("pass").trim();
////					provider_id = data.getString("provider_id").trim();
////					channel = reqdata.getJsonObject("data").getJsonObject("channel_details").getString("channel")
////							.trim();
////					invoice_id = data.getString("invoice_id").trim();
////
////					fields = new JsonArray().add(currency).add(amount).add(date_paid).add(reference_number)
////							.add(payment_type).add(mobile_number).add(paid_by).add(password).add(provider_id).add(channel)
////							.add(invoice_id);
////					break;
////
////				default:
////					System.out.println("No such TRANSACTION TYPE: " + transaction_type);
////					break;
////				}
//
//				JsonObject authData = new JsonObject();
//				authData.put("storedprocedure", funcSP);
//				authData.put("params", fields);
//				System.out.println("my fields=" + authData);
//
//				DeliveryOptions options = new DeliveryOptions();
//				int time = 5000;
//				options.setSendTimeout(time);
//				System.out.println("my fields=" + authData);
//
//				EventBus esbBus = vertx.eventBus();
//
//				MessageProducer<JsonObject> producer = esbBus.publisher("DATABASEACCESS", options);
//				producer.send(authData, (AsyncResult<Message<JsonObject>> artxn) -> {
//					if (artxn.succeeded()) {
//						JsonObject responseFields = artxn.result().body();
//						System.out.println("Received reply from Session Mgr: " + responseFields);
//
//						JsonObject feedback = new JsonObject();
//						JsonArray params = responseFields.getJsonArray("params");
//
//						String conf = "{\r\n" + "    \"pg_c2b_callback_port\": 7453,\r\n"
//								+ "    \"pg_c2b_callback_host\": \"10.20.2.4\",\r\n" + "    \"clientid\": \"5064\",\r\n"
//								+ "    \"short_code\": \"174379\",\r\n"
//								+ "    \"posting_url\": \"https://testgateway.ekenya.co.ke:8443/ServiceLayer/request/postRequest\",\r\n"
//								+ "    \"presentment_url\": \"https://testgateway.ekenya.co.ke:8443/ServiceLayer/presentment/ePay\",\r\n"
//								+ "    \"mpesacheckout_url\": \"https://testgateway.ekenya.co.ke:8443/ServiceLayer/onlinecheckout/request\",\r\n"
//								+ "    \"mpesacheckout_results_query_url\": \"https://testgateway.ekenya.co.ke:8443/ServiceLayer/onlinecheckout/query\",\r\n"
//								+ "    \"pg_username\": \"easy.rent\",\r\n"
//								+ "    \"pg_password\": \"e794e56686bb0a92c1049191d2ddacd581f5ffe43ad0000a3e40429bf8c5fee35ba4389a553e3d884ce8cc3072d8d125f9774eb2a14b2bc50470ea3011c29724\",\r\n"
//								+ "    \"pg_rsa_key\": \"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDTSkMfs9dF/UgA1hdpF71D/wc6J30Mw40ZOCTHx5YWds0j6MFfRr9RHJf9I/MuB6e6zXtgA1z9FarAUYo5rmHV6GGGvTy1+RA6b1/KY9iFW4Gw8JRrtDmEaNpYW+K4RYCGxU30acgFIPZrj8vsTWtRsBOqHUhGR4Ios+uSqwNjOQIDAQAB\",\r\n"
//								+ "    \"service_id\": {\r\n" + "      \"mpesa_checkout\": \"5067\",\r\n"
//								+ "      \"card_checkout\": \"3038\",\r\n"
//								+ "      \"BILLPRESENTMENT_KPLCPREPAID\": \"1\",\r\n"
//								+ "      \"BILL_KPLCPREPAID\": \"1\",\r\n"
//								+ "      \"BILLPRESENTMENT_KPLCPOSTPAID\": \"4043\",\r\n"
//								+ "      \"BILL_KPLCPOSTPAID\": \"4043\",\r\n"
//								+ "      \"AIRTIMETOPUP_SAFARICOM\": \"2\",\r\n"
//								+ "      \"AIRTIMETOPUP_AIRTEL\": \"3\",\r\n"
//								+ "      \"AIRTIMETOPUP_TELKOM\": \"5059\",\r\n"
//								+ "      \"BILLPRESENTMENT_DSTV_PRESENTMENT\": \"4044\",\r\n"
//								+ "      \"BILL_ZUKUTV\": \"4049\",\r\n" + "      \"BILL_ZUKUINTERNET\": \"4056\",\r\n"
//								+ "      \"BILL_JAMIITELKOM\": \"5058\",\r\n"
//								+ "      \"BILL_STARTIMES\": \"5064\",\r\n"
//								+ "      \"BILLPRESENTMENT_NHIF\": \"1019\",\r\n" + "      \"BILL_NHIF\": \"1019\",\r\n"
//								+ "      \"BILLPRESENTMENT_DSTV\": \"4044\",\r\n" + "      \"BILL_DSTV\": \"4044\",\r\n"
//								+ "      \"BILL_ACCESSKENYA\": \"4054\",\r\n"
//								+ "      \"BILLPRESENTMENT_GOTV\": \"4048\",\r\n" + "      \"BILL_GOTV\": \"4048\"\r\n"
//								+ "    }\r\n" + "  }";
//						JsonObject pgConfig = new JsonObject(conf);
//						JsonObject paymentgatewayJsonOBJ = new JsonObject();
//						paymentgatewayJsonOBJ.put("amount", data.getString("amount").trim())
//								.put("username", pgConfig.getString("pg_username"))
//								.put("password", pgConfig.getString("pg_password"))
//								.put("clientid", pgConfig.getString("clientid"))
//								.put("accountno", data.getString("mobile_number").trim())
//								.put("msisdn", data.getString("mobile_number").trim())
//								.put("accountreference", data.getString("invoice_id").trim())
//								.put("narration", data.getString("invoice_id").trim())
//								.put("transactionid", params.getJsonObject(0).getInteger("id").toString())
//								.put("serviceid", pgConfig.getJsonObject("service_id").getString("mpesa_checkout"))
//								.put("shortcode", pgConfig.getString("short_code"));
//
//						JsonObject paymentgatewayJsonOBJ1 = new JsonObject();
//						paymentgatewayJsonOBJ1.put("amount", data.getString("amount"))
//								.put("username", pgConfig.getString("pg_username"))
//								.put("password", pgConfig.getString("pg_password"))
//								.put("clientid", pgConfig.getString("clientid"))
//								.put("accountno", data.getString("mobile_number").trim())
//								.put("msisdn", data.getString("mobile_number").trim())
//								.put("accountreference", data.getString("invoice_id").trim())
//								.put("narration", data.getString("invoice_id").trim())
//								.put("transactionid", params.getJsonObject(0).getInteger("id").toString())
//								.put("serviceid", pgConfig.getJsonObject("service_id").getString("card_checkout"))
//								.put("currencycode", data.getString("currency"))
//								.put("payload", data.getString("pg_payload"));
//
//						// DO THE ACTUAL SENDING
//
//						WebClient client = WebClient.create(vertx);
//						if (transaction_type.equals("MPESACHECKOUT")) {
//							client.post(8443, "testgateway.ekenya.co.ke", "/ServiceLayer/onlinecheckout/request")
//									.ssl(true).timeout(60000).sendJsonObject(paymentgatewayJsonOBJ, inqRes -> {
//										if (inqRes.succeeded()) {
//											HttpResponse<io.vertx.core.buffer.Buffer> response = inqRes.result();
//											JsonObject pgrawresponse = response.bodyAsJsonObject();
//											System.out.println("PG RESPONSE:" + pgrawresponse);
//											String PGSTATUS = (pgrawresponse.containsKey("STATUS")
//													? pgrawresponse.getString("STATUS")
//													: pgrawresponse.getString("status"));
//											String PGMESSAGE = "";
//											JsonObject responseOBJ = new JsonObject();
//											if (PGSTATUS.equals("00")) {
//												PGMESSAGE = pgrawresponse.getString("STATUSDESCRIPTION")
//														.replaceAll("[^a-zA-Z0-9 ]", " ");
//												responseOBJ.put("response_code", "00");
//												responseOBJ.put("response_message", PGMESSAGE);
//												responseOBJ.put("biller_raw_response", PGMESSAGE);
//												data.put("response", responseOBJ);
//
//												System.out.println("Payment details =" + responseOBJ);
//
//												feedback.put("data", params.getJsonObject(0));
//												message.reply(feedback);
//											}
//
//										}
//										message.fail(0, inqRes.cause().getMessage());
//
//									});
//						} else if (transaction_type.equals("CARDCHECKOUT")) {
//							String card_security_code = data.getString("card_security_code").trim();
//							String card_expiry_date = data.getString("card_expiry_date").trim();
//							String card_id = data.getString("card_id").trim();
//							String debit_account = data.getString("debit_account").trim();
//							client.post(8443, "testgateway.ekenya.co.ke", "/ServiceLayer/onlinecheckout/request")
//									.ssl(true).timeout(60000).sendJsonObject(paymentgatewayJsonOBJ1, inqRes -> {
//										if (inqRes.succeeded()) {
//											HttpResponse<io.vertx.core.buffer.Buffer> response = inqRes.result();
//											JsonObject pgrawresponse = response.bodyAsJsonObject();
//											System.out.println("PG RESPONSE:" + pgrawresponse);
//											String PGSTATUS = (pgrawresponse.containsKey("STATUS")
//													? pgrawresponse.getString("STATUS")
//													: pgrawresponse.getString("status"));
//											String PGMESSAGE = "";
//											JsonObject responseOBJ = new JsonObject();
//											if (PGSTATUS.equals("00")) {
//												PGMESSAGE = pgrawresponse.getString("statusDescription")
//														.replaceAll("[^a-zA-Z0-9 ]", " ");
//												responseOBJ.put("response_code", "00");
//												responseOBJ.put("response_message", PGMESSAGE);
//												responseOBJ.put("biller_raw_response", PGMESSAGE);
//												data.put("response", responseOBJ);
//
//												feedback.put("data", params.getJsonObject(0));
//												message.reply(feedback);
//											}
//										}
//										message.fail(0, inqRes.cause().getMessage());
//
//									});
//						}
//					} else {
//						message.fail(0, artxn.cause().getMessage());
//
//					}
//
//				});
//			} catch (Exception ex) {
//				message.fail(0, ex.getMessage());
//			}
//
//		});
//
//		consumer.completionHandler(res -> {
//			if (res.succeeded()) {
//				System.out.println("MpesaCheckout handler has reached all nodes");
//			} else {
//				System.out.println("MpesaCheckout handler failed!");
//			}
//		});
//	}
//
//}

package com.RentApplication;

import java.util.UUID;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.HttpResponse;

public class MpesaCheckoutMS extends AbstractVerticle {
	

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("paymentCheckout");
		consumer.handler((Message<JsonObject> message) -> {

			JsonObject reqdata = message.body();

			JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");
			String transaction_type = reqdata.getJsonObject("data").getJsonObject("transaction_details")
					.getString("transaction_type").trim();

			String conf = "{\r\n" + 
					"    \"pg_c2b_callback_port\": 7453,\r\n" + 
					"    \"pg_c2b_callback_host\": \"10.20.2.4\",\r\n" + 
					"    \"clientid\": \"5064\",\r\n" + 
					"    \"short_code\": \"174379\",\r\n" + 
					"    \"posting_url\": \"https://testgateway.ekenya.co.ke:8443/ServiceLayer/request/postRequest\",\r\n" + 
					"    \"presentment_url\": \"https://testgateway.ekenya.co.ke:8443/ServiceLayer/presentment/ePay\",\r\n" + 
					"    \"mpesacheckout_url\": \"https://testgateway.ekenya.co.ke:8443/ServiceLayer/onlinecheckout/request\",\r\n" + 
					"    \"mpesacheckout_results_query_url\": \"https://testgateway.ekenya.co.ke:8443/ServiceLayer/onlinecheckout/query\",\r\n" + 
					"    \"pg_username\": \"easy.rent\",\r\n" + 
					"    \"pg_password\": \"e794e56686bb0a92c1049191d2ddacd581f5ffe43ad0000a3e40429bf8c5fee35ba4389a553e3d884ce8cc3072d8d125f9774eb2a14b2bc50470ea3011c29724\",\r\n" + 
					"    \"pg_rsa_key\": \"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDTSkMfs9dF/UgA1hdpF71D/wc6J30Mw40ZOCTHx5YWds0j6MFfRr9RHJf9I/MuB6e6zXtgA1z9FarAUYo5rmHV6GGGvTy1+RA6b1/KY9iFW4Gw8JRrtDmEaNpYW+K4RYCGxU30acgFIPZrj8vsTWtRsBOqHUhGR4Ios+uSqwNjOQIDAQAB\",\r\n" + 
					"    \"service_id\": {\r\n" + 
					"      \"mpesa_checkout\": \"5067\",\r\n" + 
					"      \"card_checkout\": \"3038\",\r\n" + 
					"      \"BILLPRESENTMENT_KPLCPREPAID\": \"1\",\r\n" + 
					"      \"BILL_KPLCPREPAID\": \"1\",\r\n" + 
					"      \"BILLPRESENTMENT_KPLCPOSTPAID\": \"4043\",\r\n" + 
					"      \"BILL_KPLCPOSTPAID\": \"4043\",\r\n" + 
					"      \"AIRTIMETOPUP_SAFARICOM\": \"2\",\r\n" + 
					"      \"AIRTIMETOPUP_AIRTEL\": \"3\",\r\n" + 
					"      \"AIRTIMETOPUP_TELKOM\": \"5059\",\r\n" + 
					"      \"BILLPRESENTMENT_DSTV_PRESENTMENT\": \"4044\",\r\n" + 
					"      \"BILL_ZUKUTV\": \"4049\",\r\n" + 
					"      \"BILL_ZUKUINTERNET\": \"4056\",\r\n" + 
					"      \"BILL_JAMIITELKOM\": \"5058\",\r\n" + 
					"      \"BILL_STARTIMES\": \"5064\",\r\n" + 
					"      \"BILLPRESENTMENT_NHIF\": \"1019\",\r\n" + 
					"      \"BILL_NHIF\": \"1019\",\r\n" + 
					"      \"BILLPRESENTMENT_DSTV\": \"4044\",\r\n" + 
					"      \"BILL_DSTV\": \"4044\",\r\n" + 
					"      \"BILL_ACCESSKENYA\": \"4054\",\r\n" + 
					"      \"BILLPRESENTMENT_GOTV\": \"4048\",\r\n" + 
					"      \"BILL_GOTV\": \"4048\"\r\n" + 
					"    }\r\n" + 
					"  }";
			
			try {
			
				if (transaction_type.equals("MPESACHECKOUT")) {

				JsonArray fields = new JsonArray();
				String funcSP = "sp_saveTransaction";

				String currency = data.getString("currency").trim();
				String amount = data.getString("amount").trim();
//				String date_paid = data.getString("date_paid").trim();
				String reference_number = data.getString("ref").trim();
//				String payment_type = data.getString("payment_type").trim();
				String mobile_number = data.getString("mobile_number").trim();
				String paid_by = data.getString("user_id").trim();
				String password = data.getString("pass").trim();
				String provider_id = data.getString("provider_id").trim();
				String channel = reqdata.getJsonObject("data").getJsonObject("channel_details").getString("channel")
						.trim();
				String invoice_id = data.getString("invoice_id").trim();

				fields = new JsonArray().add(currency).add(amount).add(reference_number)
				.add(mobile_number).add(paid_by).add(password).add(provider_id).add(channel).add(invoice_id);

				JsonObject authData = new JsonObject();
				authData.put("storedprocedure", funcSP);
				authData.put("params", fields);
				System.out.println("my fields=" + authData);

				DeliveryOptions options = new DeliveryOptions();
				int time = 5000;
				options.setSendTimeout(time);
				System.out.println("my fields=" + authData);

				EventBus esbBus = vertx.eventBus();

				MessageProducer<JsonObject> producer = esbBus.publisher("DATABASEACCESS", options);
				producer.send(authData, (AsyncResult<Message<JsonObject>> artxn) -> {
					if (artxn.succeeded()) {
						JsonObject responseFields = artxn.result().body();
						System.out.println("Received reply from Session Mgr: " + responseFields);

						JsonObject feedback = new JsonObject();
						JsonArray params = responseFields.getJsonArray("params");


						JsonObject pgConfig = new JsonObject(conf);
						JsonObject paymentgatewayJsonOBJ = new JsonObject();
						paymentgatewayJsonOBJ
								.put("amount", data.getString("amount").trim())
								.put("username", pgConfig.getString("pg_username"))
								.put("password", pgConfig.getString("pg_password"))
								.put("clientid", pgConfig.getString("clientid"))
								.put("accountno", data.getString("mobile_number").trim())
								.put("msisdn", data.getString("mobile_number").trim())
								.put("accountreference", data.getString("invoice_id").trim())
								.put("narration", data.getString("invoice_id").trim())
								.put("transactionid", params.getJsonObject(0).getInteger("id").toString())
								.put("serviceid", pgConfig.getJsonObject("service_id").getString("mpesa_checkout"))
								.put("shortcode", pgConfig.getString("short_code"));
					
						callPG(message, data, transaction_type, artxn, feedback, params, paymentgatewayJsonOBJ);

					}
					else {
						message.fail(0, artxn.cause().getMessage());
					}
				});
				}
				
				else if (transaction_type.equals("CARDCHECKOUT")) {


				JsonArray fields = new JsonArray();
				String funcSP = "sp_addCardTransaction";

				String currency = data.getString("currency").trim();
				String amount = data.getString("amount").trim();
				String reference_number = UUID.randomUUID().toString();
				String payment_type = data.getString("payment_type").trim();
				String paid_by = data.getString("paid_by").trim();
				String channel = reqdata.getJsonObject("data").getJsonObject("channel_details").getString("channel")
						.trim();
				String invoice_id = data.getString("invoice_id").trim();

				fields = new JsonArray().add(currency).add(amount).add(reference_number).add(paid_by).add(channel).add(invoice_id);

				System.out.println("payment has a message: " + fields);

				JsonObject authData = new JsonObject();
				authData.put("storedprocedure", funcSP);
				authData.put("params", fields);
				System.out.println("my fields=" + authData);

				DeliveryOptions options = new DeliveryOptions();
				int time = 5000;
				options.setSendTimeout(time);
				System.out.println("my fields=" + authData);

				EventBus esbBus = vertx.eventBus();

				MessageProducer<JsonObject> producer = esbBus.publisher("DATABASEACCESS", options);
				producer.send(authData, (AsyncResult<Message<JsonObject>> artxn) -> {
					if (artxn.succeeded()) {
						JsonObject responseFields = artxn.result().body();
						System.out.println("Received reply from Session Mgr: " + responseFields);

						JsonObject feedback = new JsonObject();
						JsonArray params = responseFields.getJsonArray("params");

						JsonObject pgConfig = new JsonObject(conf);
						JsonObject paymentgatewayJsonOBJ = new JsonObject();						
                        paymentgatewayJsonOBJ
						.put("amount", data.getString("amount").trim())
						.put("username", pgConfig.getString("pg_username"))
						.put("password", pgConfig.getString("pg_password"))
						.put("clientid", pgConfig.getString("clientid"))
                        .put("cardnumber", data.getString("card_number"))
                        .put("cardname", data.getString("card_name"))
                        .put("cardexpirydate", data.getString("card_expiry_date"))
                        .put("cardsecuritycode", data.getString("card_security_code"))
						.put("accountreference", data.getString("invoice_id").trim())
						.put("narration", data.getString("invoice_id").trim())
						.put("transactionid", params.getJsonObject(0).getInteger("id").toString())
						.put("serviceid", pgConfig.getJsonObject("service_id").getString("card_checkout"))
						.put("shortcode", pgConfig.getString("short_code"));                        
					
						// DO THE ACTUAL SENDING

						callPG(message, data, transaction_type, artxn, feedback, params, paymentgatewayJsonOBJ);

					} else {
						message.fail(0, artxn.cause().getMessage());
					}
				});
				}
			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("MpesaCheckout handler has reached all nodes");
			} else {
				System.out.println("MpesaCheckout handler failed!");
			}
		});
	}

	private void callPG(Message<JsonObject> message, JsonObject data, String transaction_type,
			AsyncResult<Message<JsonObject>> artxn, JsonObject feedback, JsonArray params,
			JsonObject paymentgatewayJsonOBJ) {
		WebClient client = WebClient.create(vertx);
		client.post(8443, "testgateway.ekenya.co.ke", "/ServiceLayer/onlinecheckout/request").ssl(true)
				.timeout(60000).sendJsonObject(paymentgatewayJsonOBJ, inqRes -> {
					if (inqRes.succeeded()) {
						HttpResponse<io.vertx.core.buffer.Buffer> response = inqRes.result();
						JsonObject pgrawresponse = response.bodyAsJsonObject();
						System.out.println("PG RESPONSE:" + pgrawresponse);
						String PGSTATUS = (pgrawresponse.containsKey("STATUS")
								? pgrawresponse.getString("STATUS")
								: pgrawresponse.getString("status"));
			
						String PGMESSAGE = "";
						JsonObject responseOBJ = new JsonObject();
						if (PGSTATUS.equals("00")) {
							if (transaction_type.equals("MPESACHECKOUT")) {
								PGMESSAGE = pgrawresponse.getString("STATUSDESCRIPTION")
										.replaceAll("[^a-zA-Z0-9 ]", " ");
								responseOBJ.put("response_code", "00");
								responseOBJ.put("response_message", PGMESSAGE);
								responseOBJ.put("biller_raw_response", PGMESSAGE);
								data.put("response", responseOBJ);
								
								System.out.println("Payment details =" + responseOBJ);

								feedback.put("data", params.getJsonObject(0));
								message.reply(feedback);
							} else if (transaction_type.equals("CARDCHECKOUT")) {
								PGMESSAGE = pgrawresponse.getString("statusDescription")
										.replaceAll("[^a-zA-Z0-9 ]", " ");
								responseOBJ.put("response_code", "00");
								responseOBJ.put("response_message", PGMESSAGE);
								responseOBJ.put("biller_raw_response", PGMESSAGE);
								data.put("response", responseOBJ);
								
								feedback.put("data", params.getJsonObject(0));
								message.reply(feedback);											
								}
						}
						else {
							feedback.put("message", pgrawresponse.getString("STATUSDESCRIPTION")
									.replaceAll("[^a-zA-Z0-9 ]", " "));
							message.reply(feedback);
						}
					}
					else {
					message.fail(0, inqRes.cause().getMessage());
					}
				});
	}

}