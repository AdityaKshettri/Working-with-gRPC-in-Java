package com.aditya.project.grpc.calculator.server;

import com.proto.calculator.CalculatorServiceGrpc;
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
}
