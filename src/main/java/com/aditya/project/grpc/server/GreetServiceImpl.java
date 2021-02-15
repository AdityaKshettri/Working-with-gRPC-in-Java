package com.aditya.project.grpc.server;

import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.Greeting;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {

        // Extract the fields from Request
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();

        // Create the Response
        String result = "Hello " + firstName;
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        // Send the Response
        responseObserver.onNext(response);

        // Complete the RPC Call
        responseObserver.onCompleted();
    }
}
