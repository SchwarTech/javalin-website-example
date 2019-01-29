package app.aws;


import app.Main;
import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class StreamLambdaHandler implements RequestStreamHandler {

    private static SparkLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {

            handler = SparkLambdaContainerHandler.getAwsProxyHandler();

            Main.initialize();

        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            log.error("Could not initialize spark container", e);
            throw new RuntimeException("Could not initialize Spark container", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);

        // just in case it wasn't closed by the mapper
        outputStream.close();
    }
}
