package com.aditya.project.grpc.calculator.client;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.ComputeAverageRequest;
import com.proto.calculator.ComputeAverageResponse;
import com.proto.calculator.FindMaximumRequest;
import com.proto.calculator.FindMaximumResponse;
import com.proto.calculator.PrimeNoDecompositionRequest;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) {
        CalculatorClient main = new CalculatorClient();
        main.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();
        //doUnaryCall(channel);
        //doStreamingServerCall(channel);
        //doStreamingClientCall(channel);
        doBiDiStreamingCall(channel);
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient = CalculatorServiceGrpc.newBlockingStub(channel);
        SumRequest request = SumRequest.newBuilder()
                .setFirstNumber(10)
                .setSecondNumber(25)
                .build();
        SumResponse response = syncClient.sum(request);
        System.out.println(request.getFirstNumber() + " + " + request.getSecondNumber() + " = " + response.getSumResult());
    }

    private void doStreamingServerCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncClient = CalculatorServiceGrpc.newBlockingStub(channel);
        int number = 20;
        syncClient.primeNoDecomposition(PrimeNoDecompositionRequest.newBuilder()
                .setNumber(number)
                .build())
                .forEachRemaining(response -> System.out.println(response.getPrimeFactor()));
    }

    private void doStreamingClientCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ComputeAverageResponse> responseObserver = new StreamObserver<ComputeAverageResponse>() {
            @Override
            public void onNext(ComputeAverageResponse value) {
                System.out.println("Received Response from server.");
                System.out.println("Average : " + value.getAverage());
            }
            @Override
            public void onError(Throwable t) {
                // do nothing for now
            }
            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending us data.");
                latch.countDown();
            }
        };
        StreamObserver<ComputeAverageRequest> requestObserver = asyncClient.computeAverage(responseObserver);
        Arrays.asList(1, 2, 3, 4)
                .forEach(number -> requestObserver.onNext(ComputeAverageRequest.newBuilder()
                    .setNumber(number)
                    .build()));
        requestObserver.onCompleted();
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<FindMaximumResponse> responseObserver = new StreamObserver<FindMaximumResponse>() {
            @Override
            public void onNext(FindMaximumResponse value) {
                System.out.println("Got new max from server : " + value.getMaximum());
            }
            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }
            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data.");
            }
        };
        StreamObserver<FindMaximumRequest> requestObserver = asyncClient.findMaximum(responseObserver);
        Arrays.asList(100, 20, 345, 40)
                .forEach(number -> {
                    System.out.println("Sending number : " + number);
                    requestObserver.onNext(FindMaximumRequest.newBuilder()
                            .setNumber(number)
                            .build());
                    try {
                        Thread.sleep(100);
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
