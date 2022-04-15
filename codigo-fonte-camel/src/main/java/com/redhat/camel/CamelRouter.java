package com.redhat.camel;

import org.apache.camel.builder.RouteBuilder;

public class CamelRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:codigofonte?period=2s").log("Olá usuário!!");
    }
}