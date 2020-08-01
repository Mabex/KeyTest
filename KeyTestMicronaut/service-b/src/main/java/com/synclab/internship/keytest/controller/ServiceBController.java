package com.synclab.internship.keytest.controller;

import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.service.ServiceCaller;
import com.synclab.internship.keytest.util.AsyCripto;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBMapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

@Controller
public class ServiceBController {

    private final ServiceCaller serviceCaller;
    private InfluxDB influxDB;
    private InfluxDBMapper mapper;

    public ServiceBController(ServiceCaller serviceCaller) {

        this.serviceCaller = serviceCaller;

        this.influxDB = InfluxDBFactory.connect("http://localhost:8086");
        Pong pong = this.influxDB.ping();
        if (pong.getVersion().equalsIgnoreCase("unknown")) {
            System.out.println("Error pinging server.");
        } else {
            influxDB.query(new Query("CREATE DATABASE fromB", ""));
            influxDB.query(new Query("CREATE RETENTION POLICY \"defaultPolicy\" ON \"fromB\" DURATION 30d REPLICATION 1 DEFAULT"));
            influxDB.enableBatch(100, 200, TimeUnit.MILLISECONDS);
            influxDB.setRetentionPolicy("defaultPolicy");
            influxDB.setDatabase("fromB");
        }

        mapper = new InfluxDBMapper(influxDB);
    }

    @Post("/message")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Message> postMessage(Message message)
            throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException,
            InvalidKeyException, IllegalBlockSizeException {
        AsyCripto asyCripto = new AsyCripto("RSA");
        PublicKey key = serviceCaller.getKey(message.getFrom());
        if (key == null) {
            return HttpResponse.notFound(
                    new Message("B","Your key could not be retrieved.",
                            asyCripto.encryptText(serviceCaller.getKeyPair().getPrivate(),"B")
                    )
            );
        }

        //System.out.println("FROM:" + message.getFrom());
        //System.out.println("SIG:" + asyCripto.decryptText(key, message.getSignature()) );
        if (!message.getFrom().equals(asyCripto.decryptText(key, message.getSignature()))) {
            return HttpResponse.badRequest(
                    new Message("B","Signature mismatch.",
                            asyCripto.encryptText(serviceCaller.getKeyPair().getPrivate(),"B")
                    )
            );
        }

        String secret;
        try {
            secret = asyCripto.decryptText(serviceCaller.getKeyPair().getPrivate(), message.getBody());
        } catch (Exception exception) {
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        influxDB.write(Point.measurement("message")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", secret)
                .build());

        QueryResult queryResult = influxDB.query(new Query("SELECT COUNT(\"value\") FROM \"fromB\".\"defaultPolicy\".\"message\""));
        //mapper.toPOJO(queryResult, MessageCount.class).forEach(System.out::println);

        Message response = new Message("B",
                asyCripto.encryptText(key, "Your secret is: "+secret),
                asyCripto.encryptText(serviceCaller.getKeyPair().getPrivate(),"B")
        );

        return HttpResponse.ok(response);
    }
}
