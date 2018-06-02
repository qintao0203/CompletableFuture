package com.completablefuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * 
 * @Description CompletableFuture实现例子
 * @date  2018年6月2日上午10:54:22
 * @version V1.0  
 * @author 秦涛   (taoqin0203@outlook.com)
 * <p>Copyright (c) Department of Research and Development/Beijing.</p>
 */
public class CompletableFutureExample {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        List<Car> join = cars().thenCompose(cars -> {
            List<CompletionStage<Car>> updatedCars = cars.stream().map(car -> rating(car.manufacturerId).thenApply(r -> {
                car.setRating(r);
                return car;
            })).collect(Collectors.toList());

            CompletableFuture<Void> done = CompletableFuture.allOf(updatedCars.toArray(new CompletableFuture[updatedCars.size()]));
            return done.thenApply(v -> updatedCars.stream().map(CompletionStage::toCompletableFuture).map(CompletableFuture::join).collect(Collectors.toList()));
        }).whenComplete((cars, th) -> {
            if (th == null) {
                cars.forEach(System.out::println);
            } else {
                throw new RuntimeException(th);
            }
        }).toCompletableFuture().join();

        long end = System.currentTimeMillis();

        System.out.println("Took " + (end - start) + " ms.");
    }

    static CompletionStage<Float> rating(int manufacturer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                simulateDelay();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            // int i = 1 / 0;
            switch (manufacturer) {
            case 2:
                return 4f;
            case 3:
                return 4.1f;
            case 7:
                return 4.2f;
            default:
                return 5f;
            }
        }).exceptionally(th -> -1f);
    }

    static CompletionStage<List<Car>> cars() {
        List<Car> carList = new ArrayList<>();
        carList.add(new Car(1, 3, "Fiesta", 2017));
        carList.add(new Car(2, 7, "Camry", 2014));
        carList.add(new Car(3, 2, "M2", 2008));
        return CompletableFuture.supplyAsync(() -> carList);
    }

    private static void simulateDelay() throws InterruptedException {
        Random r = new Random();
        // int nextInt = r.nextInt(5000);
        // System.err.println("time:" + nextInt);
        Thread.sleep(5000);
    }
}
