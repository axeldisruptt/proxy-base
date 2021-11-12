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
        .put("/get_cities")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:put-customer")
        .post("/get_cities")
//          .type(Customer.class).outType(CustomerSuccess.class)
          .to("direct:post-customer");
        .get("/get_cities")
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
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          String url = "https://apisgratis.com/api/codigospostales/v2/ciudades/";
          System.out.println("URL:"+URL);
          Message inMessage = exchange.getIn();
          String query = inMessage.getHeader(Exchange.HTTP_QUERY, String.class);
          System.out.println("Query:"+query);
          if(query != null){
              query = query + "&bridgeEndpoint=true&throwExceptionOnFailure=false";
              System.out.println("Query is not null:"+query);
          }else{
              query = "bridgeEndpoint=true&throwExceptionOnFailure=false";
              System.out.println("Query is null:"+query);
          }
          url = url + "?" + query; 
          exchange.getMessage().setHeader("CamelHttpRawQuery", query);
          System.out.println("Query:"+query);
          exchange.getMessage().setHeader(Exchange.HTTP_URI, url);
          System.out.println("URI:" + url);
        }
      })
      .setHeader("backend", simple("{{redhat.backend}}"))
      .setHeader("", simple("*"))
      .to("log:DEBUG?showBody=true&showHeaders=true")
      .to("https://netsuite")
      .streamCaching()
      .log(LoggingLevel.INFO, "${in.headers.CamelFileName}")
      .to("log:DEBUG?showBody=true&showHeaders=true")
      /*
      .to("log:DEBUG?showBody=true&showHeaders=true")
      .toD("http://${header.backend}?bridgeEndpoint=true&throwExceptionOnFailure=false")
      .to("log:DEBUG?showBody=true&showHeaders=true");
      */
      
//      .choice()
//        .when(simple("${header.CamelHttpResponseCode} != 201 && ${header.CamelHttpResponseCode} != 202"))
//          .log("err")
//          .transform(constant("Error"))
//        .otherwise()
//          .log("ok")
//      .endChoice();
  }
}