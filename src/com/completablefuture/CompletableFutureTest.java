package com.completablefuture;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * 
 * @Description 组合式异步编程
 *                Runnable类型的参数会忽略计算的结果，Consumer是纯消费计算结果，BiConsumer会组合另外一个CompletionStage纯消费，
 *                Function会对计算结果做转换，BiFunction会组合另外一个CompletionStage的计算结果做转换
 * @date  2018年5月31日下午4:19:30
 * @version V1.0  
 * @author 秦涛   (taoqin0203@outlook.com)
 * <p>Copyright (c) Department of Research and Development/Beijing.</p>
 */
public class CompletableFutureTest {

    private static Random rand = new Random();

    private static long t = System.currentTimeMillis();

    /**
        主动完成计算     futureTest();   completableFutureTest(); completableFutureTest2();
        创建CompletableFuture对象。 completedFutureExample(); runAsyncExample();completableFutureTest();
        计算结果完成时的处理   whenCompleteExample(); completeExceptionallyExample(); 
        转换  thenApplyExample(); thenApplyAsyncWithExecutorExample(); thenApplyTestExample();
        纯消费(执行Action)  thenAcceptExample();
        组合  thenComposeExample(); thenComposeExample2();  thenCombineExample();  thenCombineAsyncExample();  thenCombine功能更类似thenAcceptBoth
        Either 任意一个CompletableFuture计算完成的时候就会执行  acceptEitherExample(); applyToEitherExample2();
        辅助方法 allOf 和 anyOf  anyOfExample();
     */
    public static void main(String[] args) throws Exception {

    }

    /**
     * 
     * @Description Future实现异步
     * @throws Exception    
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月31日 上午9:51:33
     */
    static void futureTest() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(10);
        @SuppressWarnings("static-access")
        Future<Integer> f = es.submit(() -> {
            // 长时间异步计算
            Thread.currentThread().sleep(2000);
            return 100;
        });
        // 检测是否完成
        while (!f.isDone()) {
            Thread.currentThread().sleep(200);
            System.err.println("wait");
        }
        ;

        boolean cancel = f.cancel(true);
        // 阻塞线程获取结果
        System.err.println("isCancel" + cancel + ",result:" + f.get());
    }

    /**
     * 
     * @Description  主动完成计算
     * @throws Exception    
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月31日 上午10:01:52
     */
    static void completableFutureTest() throws Exception {
        CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // int i = 1 / 0;
            return 100;
        });
        // 检测是否完成
        // while (!f.isDone())
        // ;
        boolean cancel = false;
        // cancel = f.cancel(true);
        // f.join();
        Integer result = null;
        // result = f.get();
        // unchecked异常(CompletionException)
        // result = f.join();
        // 如果结果已经计算完则返回结果或者抛出异常，否则返回给定的valueIfAbsent值
        Thread.currentThread().sleep(2000);
        result = f.getNow(20);
        // 阻塞线程获取结果
        System.err.println("isCancel:" + cancel + ",result:" + result);
    }

    /**
     * 
     * @Description 主动完成计算
     * @throws IOException    
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月31日 上午10:15:01
     */
    static void completableFutureTest2() throws IOException {
        final CompletableFuture<Integer> f = new CompletableFuture<>();
        class Client extends Thread {
            CompletableFuture<Integer> f;

            Client(String threadName, CompletableFuture<Integer> f) {
                super(threadName);
                this.f = f;
            }

            @Override
            public void run() {
                try {
                    System.out.println(this.getName() + ": " + f.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        new Client("Client1", f).start();
        new Client("Client2", f).start();
        System.out.println("waiting");
        // f.complete(100);
        // f.obtrudeValue(20);
        f.completeExceptionally(new Exception("one"));
        f.obtrudeException(new Exception("two"));
        System.in.read();
    }

    /**
     * 
     * @Description 预定义结果并获取   CompletableFuture  
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 上午11:17:59
     */
    static void completedFutureExample() {
        // 用来返回一个已经计算好的CompletableFuture
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message");
        System.err.println(cf.isDone());
        System.err.println(cf.getNow(null) != null ? "message" : "is empty");
    }

    /**
     * 
     * @Description 异步执行无返回值
     * @throws Exception    
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 上午11:29:39
     */
    static void runAsyncExample() throws Exception {
        StringBuffer sb = new StringBuffer();
        @SuppressWarnings("static-access")
        CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
            System.err.println(Thread.currentThread().isDaemon());
            try {
                sb.append("hello!");
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.err.println(cf.isDone());
        Thread.sleep(1000);
        System.err.println(cf.isDone());
        System.err.println(sb.toString());
    }

    /**
     * 
     * @Description  当CompletableFuture的计算结果完成，或者抛出异常的时候，我们可以执行特定的Action
     * @throws Exception    
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月31日 上午10:58:03
     */
    static void whenCompleteExample() throws Exception {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(CompletableFutureTest::getMoreData);//.exceptionally(e -> 100);
        CompletableFuture<Integer> f = future.whenComplete((v, e) -> {
            System.out.println("v:" + v);
            // 异常
            System.out.println("e:" + e);
        });
        System.out.println(f.get());
        System.in.read();
    }

    static int getMoreData() {
        System.out.println("begin to start compute");
        try {
            Thread.sleep(1000);
            int i = 1/0;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // int i = 1 / 0;
        System.out.println("end to start compute. passed " + (System.currentTimeMillis() - t) / 1000 + " seconds");
        return rand.nextInt(100);
    }

    /**
     * 
     * @Description thenApply前一阶段执行完后要执行的函数action，函数执行会被阻塞当前线程
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 上午11:37:00
     */
    static void thenApplyExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApply(s -> {
            System.err.println(Thread.currentThread().isDaemon());
            return s.toUpperCase();
        });
        String now = cf.getNow(null);
        System.err.println(now != null && "MESSAGE".equals(now));
    }

    /**
     * 
     * @Description 异步转换
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 上午11:48:58
     */
    static void thenApplyAsyncExample() {
        @SuppressWarnings("static-access")
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
            System.err.println(Thread.currentThread().isDaemon());
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return s.toUpperCase();
        });
        String now = cf.getNow(null);
        String join = cf.join();
        System.err.println("now:" + now + ",join:" + join + "," + (join != null && "MESSAGE".equals(join)));
    }

    static ExecutorService executor = Executors.newFixedThreadPool(3, new ThreadFactory() {
        int count = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "custom-executor-" + count++);
        }
    });

    /**
     * 
     * @Description 使用线程池异步执行
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月31日 上午11:55:48
     */
    static void thenApplyAsyncWithExecutorExample() {
        @SuppressWarnings("static-access")
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
            System.err.println(Thread.currentThread().getName().startsWith("custom-executor-"));
            System.err.println(Thread.currentThread().isDaemon());
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return s.toUpperCase();
        }, executor);
        String now = cf.getNow(null);
        String join = cf.join();
        System.err.println("now:" + now + ",join:" + join + (join != null && "MESSAGE".equals(join)));
    }

    static void thenApplyTestExample() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            return 100;
        });
        CompletableFuture<String> f = future.thenApplyAsync(i -> i * 10).thenApply(i -> i.toString());
        System.out.println(f.get());
    }

    /**
     * 
     * @Description 不需要返回值消费前一段阶段结果
     * @return void     
     * @version V1.0
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月31日 上午11:58:51
     */
    static void thenAcceptExample() throws Exception {
        StringBuilder result = new StringBuilder();
        CompletableFuture<Void> thenAccept = CompletableFuture.completedFuture("message").thenAccept(s -> result.append(s));
        System.err.println("---" + thenAccept.get() + "---" + thenAccept.join());
        System.err.println(result.toString());
    }

    /**
     * 
     * @Description 异步使用前一阶段结果
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月31日 下午12:03:13
     */
    static void thenAcceptAsyncExample() {
        StringBuilder result = new StringBuilder();
        CompletableFuture<Void> cf = CompletableFuture.completedFuture("message").thenAcceptAsync(s -> result.append(s));
        cf.join();
        System.err.println(result.toString());
    }

    /**
     * 
     * @Description 捕获异常
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 下午12:18:06
     */
    static void completeExceptionallyExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(e -> {
            int i = 1 / 0;
            return e.toUpperCase();
        });
        // handle方法兼有whenComplete和转换的两个功能
        CompletableFuture<String> exceptionHandler = cf.handle((s, th) -> {
            return (th != null) ? "message upon cancel" : "";
        });
        cf.completeExceptionally(new RuntimeException("completed exceptionally"));
        System.err.println("Was not completed exceptionally:" + cf.isCompletedExceptionally());
        try {
            cf.join();
            System.err.println("Should have thrown an exception");
        } catch (CompletionException ex) {
            System.err.println("completed exceptionally:" + ex.getCause().getMessage());
        }
        System.err.println("message upon cancel:" + exceptionHandler.join());
    }

    /**
     * 
     * @Description 取消计算     
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月31日 下午12:28:09
     */
    static void cancelExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(String::toUpperCase, executor);
        CompletableFuture<String> cf2 = cf.exceptionally(throwable -> "canceled message");
        System.err.println("Was not canceled:" + cf.cancel(true));
        System.err.println("Was not completed exceptionally:" + cf.isCompletedExceptionally());
        System.err.println("canceled message:" + cf2.join());

    }

    /**
     * 
     * @Description 两个阶段一个是应用大写转换在原始的字符串上， 另一个阶段是应用小写转换
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 上午9:00:44
     */
    static void applyToEitherExample() {
        String original = "Message";
        CompletableFuture<String> cf1 = CompletableFuture.completedFuture(original).thenApplyAsync(s -> delayedUpperCase(s));
        CompletableFuture<String> cf2 = cf1.applyToEither(CompletableFuture.completedFuture(original).thenApplyAsync(s -> delayedLowerCase(s)), s -> s + " from applyToEither");
        System.err.println(cf2.join().endsWith(" from applyToEither") ? cf2.join() : "Result was empty");
    }

    /**
     * 
     * @Description 调用消费者接口
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 上午10:51:48
     */
    static void acceptEitherExample() {
        String original = "Message";
        StringBuilder result = new StringBuilder();
        CompletableFuture<Void> cf = CompletableFuture.completedFuture(original).thenApplyAsync(s -> delayedUpperCase(s)).acceptEither(CompletableFuture.completedFuture(original).thenApplyAsync(s -> delayedLowerCase(s)),
                s -> result.append(s).append("acceptEither"));
        cf.join();
        System.err.println(result.toString().endsWith("acceptEither") ? result.toString() : "Result was empty");
    }

    static void applyToEitherExample2() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            int i = rand.nextInt(1000);
            try {
                Thread.sleep(100 + i);
                System.err.println("one:" + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return i;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            int i = rand.nextInt(1000);
            try {
                Thread.sleep(100 + i);
                System.err.println("two:" + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return i;
        });
        CompletableFuture<String> f = future.applyToEither(future2, i -> i.toString());
        System.err.println(f.get());
    }

    /**
     * 
     * @Description 依赖的CompletableFuture如果等待两个阶段完成后执行了一个Runnable。 所有的阶段都是同步执行的，第一个阶段执行大写转换，第二个阶段执行小写转换     
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月31日 下午12:31:58
     */
    static void runAfterBothExample() {
        String original = "Message";
        StringBuilder result = new StringBuilder();
        CompletableFuture.completedFuture(original).thenApply(String::toUpperCase).runAfterBoth(CompletableFuture.completedFuture(original).thenApply(String::toLowerCase), () -> result.append("done"));
        System.err.println("Result was empty:" + (result.length() > 0) + result.toString());
    }

    /**
     * 
     * @Description      BiConsumer实现合并
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 上午9:47:53
     */
    static void thenAcceptBothExample() {
        String original = "Message";
        StringBuilder result = new StringBuilder();
        CompletableFuture.completedFuture(original).thenApply(String::toUpperCase).thenAcceptBoth(CompletableFuture.completedFuture(original).thenApply(String::toLowerCase), (s1, s2) -> result.append(s1 + s2));
        System.err.println("MESSAGEmessage".equals(result.toString()));
    }

    static void thenRunExample() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            return 100;
        });
        CompletableFuture<Void> f = future.thenRun(() -> System.out.println("finished"));
        System.out.println(f.get());
    }

    /**
     * 
     * @Description      使用两个阶段的结果再返回一个结果   两个CompletionStage是并行执行的，它们之间并没有先后依赖顺序，other并不会等待先前的CompletableFuture执行完毕后再执行
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 上午10:30:53
     */
    static void thenCombineExample() throws Exception {
        String original = "Message";
        CompletableFuture<String> cf = CompletableFuture.completedFuture(original).thenApply(s -> delayedUpperCase(s)).thenCombine(CompletableFuture.completedFuture(original).thenApply(s -> delayedLowerCase(s)),
                (s1, s2) -> s1.toString() + s2.toString());
        System.err.println("MESSAGEmessage".equals(cf.get()));
    }

    /**
     * 
     * @Description      依赖的前两个阶段异步地执行，所以thenCombine()也异步地执行，即时它没有Async后缀。
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 上午11:10:53
     */
    static void thenCombineAsyncExample() {
        String original = "Message";
        CompletableFuture<String> cf = CompletableFuture.completedFuture(original).thenApplyAsync(s -> delayedUpperCase(s)).thenCombine(CompletableFuture.completedFuture(original).thenApplyAsync(s -> delayedLowerCase(s)),
                (s1, s2) -> s1 + s2);
        System.err.println("MESSAGEmessage".equals(cf.join()));
    }

    /**
     * 
     * @Description 组合第一个阶段以及第二阶段结果        第一个阶段的完成(大写转换)， 它的结果传给一个指定的返回CompletableFuture函数，它的结果就是返回的CompletableFuture的结果
     * 函数需要一个大写字符串做参数，然后返回一个CompletableFuture, 这个CompletableFuture会转换字符串变成小写然后连接在大写字符串的后面
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 下午8:10:57
     */
    static void thenComposeExample() {
        String original = "Message";
        CompletableFuture<String> cf = CompletableFuture.completedFuture(original).thenApply(s -> delayedUpperCase(s))
                .thenCompose(upper -> CompletableFuture.completedFuture(original).thenApply(s -> delayedLowerCase(s)).thenApply(s -> upper + s));
        System.err.println("MESSAGEmessage".equals(cf.join()));
    }

    static void thenComposeExample2() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            return 100;
        });
        CompletableFuture<String> f = future.thenCompose(i -> {
            return CompletableFuture.supplyAsync(() -> {
                return (i * 10) + "";
            });
        });
        System.out.println(f.get());
    }

    /**
     * 
     * @Description 每个阶段都是转换一个字符串为大写。因为本例中这些阶段都是同步地执行(thenApply), 从anyOf中创建的CompletableFuture会立即完成，
     * 这样所有的阶段都已完成，我们使用whenComplete(BiConsumer<? super Object, ? super Throwable> action)处理完成的结果。
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 下午8:25:57
     */
    static void anyOfExample2() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        List<CompletableFuture<String>> futures = messages.stream().map(msg -> CompletableFuture.completedFuture(msg).thenApply(s -> {
            return delayedUpperCase(s);
        })).collect(Collectors.toList());
        CompletableFuture.anyOf(futures.toArray(new CompletableFuture[futures.size()])).whenComplete((res, th) -> {
            System.err.println("th:" + res);
            if (th == null) {
                result.append(res);
            }
        });
        System.err.println(result.toString());
    }

    static void anyOfExample() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            int i = rand.nextInt(1000);
            try {
                Thread.sleep(100 + i);
                System.err.println("one:" + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return i;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            int i = rand.nextInt(1000);
            try {
                Thread.sleep(100 + i);
                System.err.println("two:" + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return i;
        });
        CompletableFuture<Object> f = CompletableFuture.anyOf(future, future2);
        System.out.println(f.get());
    }

    /**
     * 
     * @Description 所有执行完成后执行
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 下午9:20:37
     */
    static void allOfExample() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        List<CompletableFuture<String>> futures = messages.stream().map(msg -> CompletableFuture.completedFuture(msg).thenApply(s -> delayedUpperCase(s))).collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).whenComplete((v, th) -> {
            futures.forEach(cf -> System.err.println(cf.getNow(null)));
            result.append("done");
        });
        System.err.println(result.toString());
    }

    /**
     * 
     * @Description 异步执行
     * @return void     
     * @version V1.0
     * @auth    秦涛   (taoqin0203@outlook.com)
     * @date 2018年5月30日 下午9:20:50
     */
    static void allOfAsyncExample() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        List<CompletableFuture<String>> futures = messages.stream().map(msg -> CompletableFuture.completedFuture(msg).thenApplyAsync(s -> delayedUpperCase(s))).collect(Collectors.toList());
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).whenComplete((v, th) -> {
            futures.forEach(cf -> System.err.println(cf.getNow(null)));
            result.append("done");
        });
        allOf.join();
        System.err.println(result.length() > 0 ? result.toString() : "Result was empty");
    }

    private static String delayedLowerCase(String s) {
        return s.toLowerCase();
    }

    private static String delayedUpperCase(String s) {
        return s.toUpperCase();
    }

}
