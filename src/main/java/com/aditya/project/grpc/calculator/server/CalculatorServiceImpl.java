package com.aditya.project.grpc.calculator.server;

import com.proto.calculator.CalculatorServiceGrpc;
import com.proto.calculator.ComputeAverageRequest;
import com.proto.calculator.ComputeAverageResponse;
import com.proto.calculator.PrimeNoDecompositionRequest;
import com.proto.calculator.PrimeNoDecompositionResponse;
import com.proto.calculator.SumRequest;
import com.proto.calculator.SumResponse;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        SumResponse response = SumResponse.newBuilder()
                .setSumResult(request.getFirstNumber() + request.getSecondNumber())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void primeNoDecomposition(PrimeNoDecompositionRequest request, StreamObserver<PrimeNoDecompositionResponse> responseObserver) {
        int number = request.getNumber();
        int divisor = 2;
        while(number>1) {
            if(number % divisor == 0) {
                number = number/divisor;
                responseObserver.onNext(PrimeNoDecompositionResponse.newBuilder()
                        .setPrimeFactor(divisor)
                        .build());
            } else {
                divisor += 1;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        return new StreamObserver<ComputeAverageRequest>() {
            int sum = 0;
            int count = 0;
            @Override
            public void onNext(ComputeAverageRequest value) {
                sum += value.getNumber();
                count += 1;
            }
            @Override
            public void onError(Throwable t) {
                //do nothing for now
            }
            @Override
            public void onCompleted() {
                double average = (double) sum/count;
                responseObserver.onNext(ComputeAverageResponse.newBuilder()
                        .setAverage(average)
                        .build());
                responseObserver.onCompleted();
            }
        };
    }
}
