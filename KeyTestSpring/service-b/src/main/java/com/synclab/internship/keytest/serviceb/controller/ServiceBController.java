package com.synclab.internship.keytest.serviceb.controller;

import com.synclab.internship.keytest.model.Message;
import com.synclab.internship.keytest.model.MessageCount;
import com.synclab.internship.keytest.serviceb.service.ServiceCaller;
import com.synclab.internship.keytest.serviceb.util.AsyCripto;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeUnit;

@RestController
public class ServiceBController implements CommandLineRunner {

    private final ServiceCaller serviceCaller;
    private final KeyPair keyPair;
    private InfluxDB influxDB;
    private InfluxDBMapper mapper;

    @Autowired
    public ServiceBController(ServiceCaller serviceCaller, KeyPair keyPair) {
        this.serviceCaller = serviceCaller;
        this.keyPair = keyPair;

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

    @PostMapping("/message")
    public ResponseEntity<Message> postMessage(@RequestBody Message message)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            BadPaddingException, InvalidKeyException, IllegalBlockSizeException {

        AsyCripto asyCripto = new AsyCripto("RSA");
        PublicKey key = serviceCaller.getKey(message.getFrom());
        if (key == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new Message("B","Your key could not be retrieved.",
                            asyCripto.encryptText(keyPair.getPrivate(),"B")
                    )
            );
        }

        //System.out.println("FROM:" + message.getFrom());
        //System.out.println("SIG:" + asyCripto.decryptText(key, message.getSignature()) );
        if (!message.getFrom().equals(asyCripto.decryptText(key, message.getSignature()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Message("B","Signature mismatch.",
                            asyCripto.encryptText(keyPair.getPrivate(),"B")
                    )
            );
        }

        String secret;
        try {
            secret = asyCripto.decryptText(keyPair.getPrivate(), message.getBody());
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        influxDB.write(Point.measurement("message")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("value", secret)
                .build());

        QueryResult queryResult = influxDB.query(new Query("SELECT COUNT(\"value\") FROM \"fromB\".\"defaultPolicy\".\"message\""));
        //mapper.toPOJO(queryResult, MessageCount.class).forEach(System.out::println);

        Message response = new Message("B",
                asyCripto.encryptText(key, "Your secret is: "+secret),
                asyCripto.encryptText(keyPair.getPrivate(),"B")
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public void run(String... args) throws Exception {
        serviceCaller.register("B");
    }
}
