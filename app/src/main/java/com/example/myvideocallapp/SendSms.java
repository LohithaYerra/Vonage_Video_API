//package com.example.myvideocallapp;
//
//import com.vonage.client.VonageClient;
//import com.vonage.client.messages.sms.SmsTextRequest;
//
//public class SendSms {
//    public static void main(String[] args) throws Exception{
//        VonageClient client = VonageClient.builder().apiKey("555734c0")
//                .apiSecret(System.getenv("VONAGE_API_SECRET")).build();
//        var response = client.getMessagesClient().sendMessage(
//                SmsTextRequest.builder()
//                        .from("Vonage APIs").
//                        to("918688947429").
//                        text("This is an SMS text message sent using the Vonage Messages API.")
//                        .build());
//        System.out.println("Message Sent Successfully. ID: " + response.getMessageUuid());
//    }
//}
