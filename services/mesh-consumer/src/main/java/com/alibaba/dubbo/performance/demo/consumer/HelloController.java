package com.alibaba.dubbo.performance.demo.consumer;

import org.apache.commons.lang3.RandomStringUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Random;

@RestController
public class HelloController {

    private AsyncHttpClient asyncHttpClient = org.asynchttpclient.Dsl.asyncHttpClient();

    private ResponseEntity ok = new ResponseEntity("OK", HttpStatus.OK);
    private ResponseEntity error = new ResponseEntity("ERROR", HttpStatus.INTERNAL_SERVER_ERROR);

    Random r = new Random(1);

    @RequestMapping(value = "/invoke")
    public DeferredResult<ResponseEntity> invoke() {

        String str = RandomStringUtils.random(r.nextInt(1024), true, true);

        String url = "http://127.0.0.1:20000";


        DeferredResult<ResponseEntity> result = new DeferredResult<>();

        org.asynchttpclient.Request request = org.asynchttpclient.Dsl.post(url)
                .addFormParam("interface", "com.alibaba.dubbo.performance.demo.provider.IHelloService")
                .addFormParam("method", "hash")
                .addFormParam("parameterTypesString", "Ljava/lang/String;")
                .addFormParam("parameter", str)
                .build();

        ListenableFuture<org.asynchttpclient.Response> responseFuture = asyncHttpClient.executeRequest(request);

        Runnable callback = () -> {
            try {
                // 检查返回值是否正确,如果不正确返回500。有以下原因可能导致返回值不对:
                // 1. agent解析dubbo返回数据不对
                // 2. agent没有把request和dubbo的response对应起来
                String value = responseFuture.get().getResponseBody();
                if (String.valueOf(str.hashCode()).equals(value)){
                    result.setResult(ok);
                } else {
                    result.setResult(error);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        responseFuture.addListener(callback, null);

        return result;
    }
}
