package com.reactor.app;

import com.reactor.app.model.Comments;
import com.reactor.app.model.User;
import com.reactor.app.model.UserComments;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        ejemploContraPresion();
    }
    public void ejemploContraPresion(){
        Flux.range(1,10)
                .log()
                .limitRate(2)
                .subscribe(/*new Subscriber<Integer>() {
                    private Subscription subscription;
                    private Integer limite = 2;
                    private Integer consumido = 0;
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(limite);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info(integer.toString());
                        consumido++;
                        if(consumido==limite){
                            consumido= 0;
                            subscription.request(limite);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }*/);
    }
    public void ejemploIntervalDesdeCreate() {
        Flux.create(emiter->{
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                private Integer contador = 0;
                @Override
                public void run() {
                    emiter.next(++contador);
                    if (contador==10){
                        timer.cancel();
                        emiter.complete();
                    }
                    if(contador == 5){
                        timer.cancel();
                        emiter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
                    }
                }
            }, 1000, 1000);
        })
                .subscribe(next->log.info(next.toString()),
                        error->log.error(error.getMessage()),
                        ()->log.info("Hemos terminado"));
    }
    public void ejemploIntervalInfinito() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(()->latch.countDown())
                .flatMap(i->{
                    if(i>=5){
                        return Flux.error(new InterruptedException("Solo hasta 5!"));
                    }
                    return Flux.just(i);
                })
                .map(i->"Hola "+i)
                .retry(2)
                .subscribe(log::info, e->log.error(e.toString()));
        latch.await();
    }
    public void ejemploDelayElements() {
        Flux<Integer> range = Flux.range(1,12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i->log.info(i.toString()));
        range.subscribe();
    }
    public void ejemploInterval() {
        Flux<Integer> range = Flux.range(1,12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
        range.zipWith(retraso,(ra, re)->ra)
                .doOnNext(i->log.info(i.toString()))
                .blockLast();
//                .subscribe();
        //blockLast bloquea
    }
    public void ejemploZipWithRange() {
        Flux<Integer> range = Flux.range(0,4);
        Flux.just(1, 2, 3, 4)
                .map(i->i*4)
                .zipWith(range,(uno,dos)->String.format("Primer Flux: %d, Segundo Flux: %d",uno,dos))
                .subscribe(data->log.info(data));
    }
    public void ejemploUserCommentsZipWithForma2() {
        Mono<User> userMono = Mono.fromCallable(()->new User("Luis","Chichanda"));
        Mono<Comments> commentsMono = Mono.fromCallable(()->{
            Comments comments = new Comments();
            comments.addComments("hola1");
            comments.addComments("hola2");
            comments.addComments("hola3");
            return comments;
        });
        userMono.zipWith(commentsMono)
                .map(tuple -> new UserComments(tuple.getT1(),tuple.getT2()))
                .subscribe(uc->log.info(uc.toString()));
    }
    public void ejemploUserCommentsZipWith() {
        Mono<User> userMono = Mono.fromCallable(()->new User("Luis","Chichanda"));
        Mono<Comments> commentsMono = Mono.fromCallable(()->{
            Comments comments = new Comments();
            comments.addComments("hola1");
            comments.addComments("hola2");
            comments.addComments("hola3");
            return comments;
        });
        userMono.zipWith(commentsMono,(u,c)->new UserComments(u,c))
                .subscribe(uc->log.info(uc.toString()));
    }
    public void ejemploUserCommentsFlatMap() {
        Mono<User> userMono = Mono.fromCallable(()->new User("Luis","Chichanda"));
        Mono<Comments> commentsMono = Mono.fromCallable(()->{
            Comments comments = new Comments();
            comments.addComments("hola1");
            comments.addComments("hola2");
            comments.addComments("hola3");
            return comments;
        });
        userMono.flatMap(u->commentsMono.map(c->new UserComments(u,c)))
                .subscribe(uc->log.info(uc.toString()));
    }
    public void ejemploCollectList() {
        List<User> userList = new ArrayList<>();
        userList.add(new User("Luis","Chichanda"));
        userList.add(new User("Jeniffer","Katherine"));
        userList.add(new User("Maria","Fulana"));
        userList.add(new User("Diego","Sultano"));
        userList.add(new User("Juan","Mengano"));
        userList.add(new User("Bruce","Lee"));
        userList.add(new User("Bruce","Willis"));
        Flux.fromIterable(userList)
                .collectList()
                .subscribe(list ->{
                    list.forEach(item ->log.info(item.toString()));
                });
    }
    public void ejemploToString() {
        List<User> userList = new ArrayList<>();
        userList.add(new User("Luis","Chichanda"));
        userList.add(new User("Jeniffer","Katherine"));
        userList.add(new User("Maria","Fulana"));
        userList.add(new User("Diego","Sultano"));
        userList.add(new User("Juan","Mengano"));
        userList.add(new User("Bruce","Lee"));
        userList.add(new User("Bruce","Willis"));
        Flux.fromIterable(userList)
                .map(element -> element.getName().toUpperCase().concat(" ").concat(element.getLastName().toUpperCase()))
                .flatMap(element -> {
                    if(element.contains("bruce".toUpperCase())){
                        return Mono.just(element);
                    }
                    return Mono.empty();
                })
                .map(String::toLowerCase)
                .subscribe(log::info);
    }
    public void ejemploFlatMap() {
        List<String> userList = new ArrayList<>();
        userList.add("Luis Chichanda");
        userList.add("Jeniffer Katherine");
        userList.add("Maria Fulana");
        userList.add("Diego Sultano");
        userList.add("Juan Mengano");
        userList.add("Bruce Lee");
        userList.add("Bruce Willis");
        Flux.fromIterable(userList)
                .map(element -> new User(element.split(" ")[0].toUpperCase(), element.split(" ")[1].toUpperCase()))
                .flatMap(element -> {
                    if(element.getName().equalsIgnoreCase("bruce")){
                        return Mono.just(element);
                    }
                    return Mono.empty();
                })
                .map(element -> {
                    element.setName(element.getName().toLowerCase());
                    return element;
                })
                .subscribe(e -> log.info(e.toString()));
    }
    public void ejemploIterable() {
        List<String> userList = new ArrayList<>();
        userList.add("Luis Chichanda");
        userList.add("Jeniffer Katherine");
        userList.add("Maria Fulana");
        userList.add("Diego Sultano");
        userList.add("Juan Mengano");
        userList.add("Bruce Lee");
        userList.add("Bruce Willis");
//        Flux<String> names = Flux.just("Luis Chichanda", "Jeniffer Katherine", "Maria Fulana", "Diego Sultano", "Juan Mengano", "Bruce Lee", "Bruce Willis");
        Flux<String> names = Flux.fromIterable(userList);
        Flux<User> userFlux = names.map(element -> new User(element.split(" ")[0].toUpperCase(), element.split(" ")[1].toUpperCase()))
                .filter(element -> element.getName().toLowerCase().equals("bruce"))
                .doOnNext(element -> {
                    if (element == null) {
                        throw new RuntimeException("Nombres estan vacio");
                    }
                    System.out.printf(element.getName().concat(" ").concat(element.getLastName().concat("\n")));
                })
                .map(element -> {
                    element.setName(element.getName().toLowerCase());
                    return element;
                });
        userFlux.subscribe(e -> log.info(e.toString()),
                error -> log.error(error.getMessage()),
                () -> log.info("Ha finalizado"));
    }
}
