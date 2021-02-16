package com.aditya.project.grpc.client;

import com.proto.greet.GreetManyTimesRequest;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.Greeting;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {

    public static void main(String[] args) {

        System.out.println("Hello! I am a GRPC Client.");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        System.out.println("Creating stub!");
        // DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        // DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);

        // Created a Greet Service Client
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // Created a Greeting
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Aditya")
                .setLastName("Kshettri")
                .build();

//        // Created a GreetRequest
//        GreetRequest request = GreetRequest.newBuilder()
//                .setGreeting(greeting)
//                .build();
//
//        // Call the RPC and get the GreetResponse
//        GreetResponse response = greetClient.greet(request);
//        System.out.println(response.getResult());

        // Server Streaming
        // We prepare the request
        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // We stream the response
        greetClient.greetManyTimes(request)
                .forEachRemaining(response -> {
                    System.out.println(response.getResult());
                });

        System.out.println("Shutting down channel!");
        channel.shutdown();
    }
}
