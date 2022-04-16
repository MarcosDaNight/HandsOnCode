package com.redhat.camel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.support.builder.Namespaces;

public class CamelRouter extends RouteBuilder {

    
	@Override
	public void configure() throws Exception {
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Namespaces ns = new Namespaces("ns2", "http://server.produtos.redhat.com/");

        restConfiguration().bindingMode(RestBindingMode.auto)
        	.component("platform-http")
			.dataFormatProperty("prettyPrint", "true")
			.contextPath("/").port(8080)
			.apiContextPath("/openapi")
			.apiProperty("api.title", "Camel Quarkus Demo API")
			.apiProperty("api.version", "1.0.0-SNAPSHOT")
            .apiProperty("cors", "true");

		rest().tag("API de serviços Demo utilizando Camel e Quarkus").produces("application/json")				
		
			.get("/clientes")				
				.description("Listar todos os clientes")
				.route().routeId("restclienteall") .to("direct:clienteall").choice()
				.when(body().isNull()).setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404)).endChoice().otherwise()
				.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
				.endRest()

                .get("/clientes/{cpf}")
				.description("Consulta cliente por CPF")
				.route().routeId("restquerybycpf").to("direct:querybycpf").choice()
				.when(body().isNull()).setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404)).endChoice().otherwise()
				.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
				.endRest()

			.post("/clientes/{cpf}/{nome}/{email}/{endereco}")
				.description("Salvar novo cliente")
				.route().routeId("restclienteinsert") .to("direct:clienteinsert")
				.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
				.endRest()


			.put("/clientes/{cpf}/{nome}/{email}/{endereco}")
				.description("Alterar cliente")
				.responseMessage().code(200).message("cliente alterado com sucesso").endResponseMessage()
				.responseMessage().code(500).message("Falha ao alterar cliente").endResponseMessage()
				.route().routeId("restclienteupdate").to("direct:clienteupdate")
				.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
				.endRest()

			.delete("/clientes/{cpf}")
				.description("Exclui um cliente")
				.route().routeId("restclientedelete").to("direct:clientedelete")
				.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
				.endRest()

			.get("/produtos/{codigo}")
				.description("consultar produto por código")
				.route().routeId("restproduto")
				.to("direct:produto")				
				.setHeader(Exchange.CONTENT_TYPE,constant("application/json"))
				.setHeader(Exchange.HTTP_RESPONSE_CODE,constant(200))
				.endRest()
                
            ;

        from("direct:clienteall").routeId("clienteall").to("sql:classpath:sql/queryall.sql");
		from("direct:querybycpf").routeId("querybycpf").to("sql:classpath:sql/querybycpf.sql?outputType=SelectOne&outputClass=com.redhat.camel.model.Cliente");
		from("direct:clienteinsert").routeId("clienteinsert").to("sql:classpath:sql/insert.sql");
		from("direct:clienteupdate").routeId("clienteupdate").to("sql:classpath:sql/update.sql");
		from("direct:clientedelete").routeId("clientedelete").to("sql:classpath:sql/delete.sql");

                
    }
}