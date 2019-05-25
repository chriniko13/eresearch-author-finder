package com.eresearch.author.finder.application.actuator.health;

import com.eresearch.author.finder.dto.AuthorFinderDto;
import com.eresearch.author.finder.dto.AuthorFinderResultsDto;
import com.eresearch.author.finder.dto.AuthorNameDto;
import com.eresearch.author.finder.exception.BusinessProcessingException;
import com.eresearch.author.finder.service.AuthorFinderService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Log4j
@Component
public class EresearchAuthorFinderHealthCheck extends AbstractHealthIndicator {

    @Autowired
    private AuthorFinderService authorFinderService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${do.specific.author.finder.api.health.check}")
    private String doSpecificAuthorFinderApiHealthCheck;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        this.performBasicHealthChecks();

        Optional<Exception> ex = this.specificHealthCheck();

        if (ex.isPresent()) {
            builder.down(ex.get());
        } else {
            builder.up();
        }
    }

    private void performBasicHealthChecks() {
        //check disk...
        DiskSpaceHealthIndicatorProperties diskSpaceHealthIndicatorProperties
                = new DiskSpaceHealthIndicatorProperties();
        diskSpaceHealthIndicatorProperties.setThreshold(10737418240L); /*10 GB*/
        new DiskSpaceHealthIndicator(diskSpaceHealthIndicatorProperties);

        //check jms (active mq) is up...
        new JmsHealthIndicator(jmsTemplate.getConnectionFactory());
    }

    private Optional<Exception> specificHealthCheck() {

        if (Boolean.valueOf(doSpecificAuthorFinderApiHealthCheck)) {
            //check if we can get a response from elsevier-api...
            Optional<Exception> ex2 = specificElsevierApiHealthCheck();
            if (ex2.isPresent()) {
                return ex2;
            }
        }

        return Optional.empty();
    }

    private Optional<Exception> specificElsevierApiHealthCheck() {

        try {

            /*
            NOTE: remove 'or' query strategy for this health-check if you want to have fast response.
             */
            AuthorFinderResultsDto results
                    = authorFinderService.authorFinderOperation(
                    AuthorFinderDto.builder()
                            .authorName(AuthorNameDto.builder()
                                    .firstName("Athanasios")
                                    .surname("Voulodimos")
                                    .build())
                            .build());

            if (Objects.isNull(results)) {
                log.error("EresearchAuthorFinderHealthCheck#specificElsevierApiHealthCheck --- result from elsevier-api is null.");
                return Optional.of(new NullPointerException("result from elsevier-api is null."));
            }

        } catch (BusinessProcessingException ex) {

            log.error("EresearchAuthorFinderHealthCheck#specificElsevierApiHealthCheck --- communication issue.", ex);
            return Optional.of(ex);

        }

        return Optional.empty();
    }

}
