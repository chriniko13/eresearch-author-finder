package com.eresearch.author.finder.it;


import com.eresearch.author.finder.EresearchAuthorFinderApplication;
import com.eresearch.author.finder.application.configuration.JmsConfiguration;
import com.eresearch.author.finder.core.FileSupport;
import com.eresearch.author.finder.dto.AuthorFinderDto;
import com.eresearch.author.finder.dto.AuthorFinderQueueResultDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.jms.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = EresearchAuthorFinderApplication.class,
        properties = {"application.properties"}
)
@RunWith(SpringRunner.class)
public class SpecificationIT {

    @Value("${spring.activemq.broker-url}")
    private String activeMqBrokerUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private RestTemplate restTemplate;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(1234);

    @Before
    public void setUp() {
        restTemplate = new RestTemplate();
    }

    @Test
    public void author_finder_consumption_works_as_expected() throws Exception {

        // given
        String authorFinderDtoAsString = FileSupport.getResource("test/first_case_input.json");

        AuthorFinderDto payload
                = objectMapper.readValue(authorFinderDtoAsString, AuthorFinderDto.class);


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        HttpEntity<AuthorFinderDto> httpEntity = new HttpEntity<>(payload, httpHeaders);

        mockAuthorElsevier();


        // when
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/author-finder/find",
                HttpMethod.POST,
                httpEntity,
                String.class);

        // then
        Assert.assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());
        Assert.assertNotNull(responseEntity.getBody());


        String expectedAsString = FileSupport.getResource("test/first_case_output.json");

        JSONAssert.assertEquals(
                expectedAsString, responseEntity.getBody(),
                new CustomComparator(JSONCompareMode.STRICT, new Customization("process-finished-date", (o1, o2) -> true))
        );

    }


    @Test
    public void author_finder_consumption_works_as_expected_async_queue() throws Exception {

        // given
        String authorFinderDtoAsString = FileSupport.getResource("test/first_case_input.json");

        AuthorFinderDto payload
                = objectMapper.readValue(authorFinderDtoAsString, AuthorFinderDto.class);


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Transaction-Id", UUID.randomUUID().toString());

        HttpEntity<AuthorFinderDto> httpEntity = new HttpEntity<>(payload, httpHeaders);

        mockAuthorElsevier();


        // when
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/author-finder/find-q",
                HttpMethod.POST,
                httpEntity,
                String.class);


        // then
        Assert.assertNotNull(responseEntity.getBody());

        String body = responseEntity.getBody();
        JSONAssert.assertEquals("{\"message\":\"Response will be written in queue.\"}", body, true);


        // Getting JMS connection from the server
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMqBrokerUrl);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // Creating session for seding messages
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

        // Getting the queue
        Queue destination = session.createQueue(JmsConfiguration.QUEUES.AUTHOR_RESULTS_QUEUE);

        // MessageConsumer is used for receiving (consuming) messages
        MessageConsumer consumer = session.createConsumer(destination);

        final String expected = FileSupport.getResource("test/first_case_output_queue.json");

        Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> {

                    Message message = consumer.receive();
                    if (message instanceof TextMessage) {

                        TextMessage textMessage = (TextMessage) message;
                        String text = textMessage.getText();

                        AuthorFinderQueueResultDto authorFinderQueueResultDto = objectMapper.readValue(text, AuthorFinderQueueResultDto.class);
                        String result = objectMapper.writeValueAsString(authorFinderQueueResultDto);
                        System.out.println(">>> RESULT: " + result);

                        try {

                            JSONAssert.assertEquals(expected, text,
                                    new CustomComparator(JSONCompareMode.STRICT,
                                            new Customization("authorFinderResultsDto.process-finished-date", (o1, o2) -> true),
                                            new Customization("transactionId", (o1, o2) -> true))
                            );

                            return true;
                        } catch (AssertionError error) {
                            error.printStackTrace();
                            return false;
                        } finally {
                            message.acknowledge();
                        }
                    }

                    return false;
                });

    }

    private void mockAuthorElsevier() {
        stubFor(get(urlEqualTo("/content/search/author?apikey=f560b7d8fb2ee94533209bc0fdf5087f&query=authlast(Christidis)%20and%20authfirst(Nikolaos%20)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(FileSupport.getResource("test/elsevier_author_capture/getElsevierAuthor__authlast(Christidis)%20and%20authfirst(Nikolaos%20).json"))));
    }
}
