package com.redhat;

//import com.redhat.dto.Customer;
//import com.redhat.dto.CustomerSuccess;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class Routes extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    restConfiguration()
      .component("netty-http")
      .port("8080")
      .bindingMode(RestBindingMode.auto);
    
    rest()
      .path("/").consumes("application/json").produces("application/json")
        .put("/get_pc")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:put-customer")
        .post("/get_pc")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:post-customer")
        .get("/get_pc")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:get-customer");
    
    from("direct:post-customer")
      .setHeader("HTTP_METHOD", constant("POST"))
      .to("direct:request");
    from("direct:put-customer")
      .setHeader("HTTP_METHOD", constant("PUT"))
      .to("direct:request");
    from("direct:get-customer")
      .setHeader("HTTP_METHOD", constant("GET"))
      .to("direct:request");

    from("direct:request")
      .setHeader("backend", simple("{{redhat.backend}}"))
      .setHeader("Access-Control-Allow-Origin", simple("*"))
      .to("log:DEBUG?showBody=true&showHeaders=true")
      .toD("${header.backend}?bridgeEndpoint=true&throwExceptionOnFailure=false")
      .to("log:DEBUG?showBody=true&showHeaders=true");
      
//      .choice()
//        .when(simple("${header.CamelHttpResponseCode} != 201 && ${header.CamelHttpResponseCode} != 202"))
//          .log("err")
//          .transform(constant("Error"))
//        .otherwise()
//          .log("ok")
//      .endChoice();
  }
}