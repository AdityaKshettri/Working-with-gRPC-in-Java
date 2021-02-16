package com.aditya.project.grpc.greeting.client;

import com.proto.greet.GreetEveryoneRequest;
import com.proto.greet.GreetEveryoneResponse;
import com.proto.greet.GreetManyTimesRequest;
import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.Greeting;
import com.proto.greet.LongGreetRequest;
import com.proto.greet.LongGreetResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) {
        System.out.println("Hello! I am a GRPC Client.");

        GreetingClient main = new GreetingClient();
        main.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        // doUnaryCall(channel);
        // doServerStreamingCall(channel);
        // doClientStreamingCall(channel);
        doBiDiStreamingCall(channel);

        System.out.println("Shutting down channel!");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {
        // Created a Greet Service Client
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // Created a Greeting
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Aditya")
                .setLastName("Kshettri")
                .build();

        // Created a GreetRequest
        GreetRequest request = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // Call the RPC and get the GreetResponse
        GreetResponse response = greetClient.greet(request);
        System.out.println(response.getResult());
    }

    private void doServerStreamingCall(ManagedChannel channel) {
        // Created a Greet Service Client
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // Created a Greeting
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Aditya")
                .setLastName("Kshettri")
                .build();

        // Server Streaming
        // We prepare the request
        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // We stream the response
        greetClient.greetManyTimes(request)
                .forEachRemaining(response -> System.out.println(response.getResult()));
    }

    private void doClientStreamingCall(ManagedChannel channel) {
        // Create a Client (stub)
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetResponse> responseObserver = new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // We get a response from server
                System.out.println("Received a Response from the Server");
                System.out.println(value.getResult());
                // OnNext will be called only once
            }

            @Override
            public void onError(Throwable t) {
                // We get an error from server
            }

            @Override
            public void onCompleted() {
                // Server is done sending us data
                // OnCompleted will be called right after onNext
                System.out.println("Server has completed sending us something");
                latch.countDown();
            }
        };

        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(responseObserver);

        // Streaming message #1
        System.out.println("Sending message 1");
        requestObserver.onNext(LongGreetRequest.newBuilder()
            .setGreeting(Greeting.newBuilder()
                .setFirstName("Aditya")
                .build())
            .build());

        // Streaming message #2
        System.out.println("Sending message 2");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Kshettri")
                        .build())
                .build());

        // Streaming message #3
        System.out.println("Sending message 3");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Adi")
                        .build())
                .build());

        // We inform the server that the client is done sending
        requestObserver.onCompleted();

        // delay to get the response from the server
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        // Create a Client (stub)
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneResponse> responseObserver = new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response from server : " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        };

        StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(responseObserver);

        Arrays.asList("Aditya", "Kshettri", "Adi")
                .forEach(name -> {
                    System.out.println("Sending : " + name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                            .setFirstName(name)
                            .build())
                        .build());
                    // Checking the asynchronous behaviour below
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
