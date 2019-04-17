package com.eresearch.author.finder.service;

import lombok.extern.log4j.Log4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j

@Component
@Aspect
public class CaptureElsevierAuthorResponseService {

    @Value("${capture.elsevier-author-response}")
    private boolean captureElsevierAuthor;

    @Value("${capture-service.path-to-store-files}")
    private String pathToStoreFiles;

    @Pointcut("execution(String com.eresearch.author.finder.connector.communicator.BasicCommunicator.communicateWithElsevier(java.net.URI))")
    private void interceptElsevierAuthorCommunication() {
    }

    @Around("interceptElsevierAuthorCommunication()")
    public Object intercept(ProceedingJoinPoint pjp) throws Throwable {

        Object result = pjp.proceed();

        if (captureElsevierAuthor) {
            //Example: https://api.elsevier.com/content/search/author?apikey=f560b7d8fb2ee94533209bc0fdf5087f&query=authlast(Christidis)%20and%20authfirst(Nikolaos%20)
            URI uri = (URI) pjp.getArgs()[0];

            String queryParamsConcatenated = uri.toString().split("\\?")[1];

            String[] queryParams = queryParamsConcatenated.split("&");

            Map<String, String> queryParamsBindings = Arrays
                    .stream(queryParams)
                    .collect(Collectors.toMap(
                            qP -> qP.split("=")[0],
                            qP -> qP.split("=")[1])
                    );

            //System.out.println("    >>>" + uri);
            log("getElsevierAuthor__" + queryParamsBindings.get("query"), (String) result, "json");
        }

        return result;
    }


    private void log(String filename, String contents, String fileType) {
        try {
            Path path = Paths.get(pathToStoreFiles, filename + "." + fileType);

            Files.deleteIfExists(path);
            Files.createFile(path);

            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
                bufferedWriter.write(contents);
                bufferedWriter.newLine();
            }
        } catch (Exception error) {
            log.error("error occurred: " + error.getMessage(), error);
        }
    }

}
