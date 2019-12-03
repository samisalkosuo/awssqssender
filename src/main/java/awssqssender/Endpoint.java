package awssqssender;

import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;



/*
    Super class for REST endpoints
*/
public class Endpoint
{

    private final static Logger LOGGER = Logger.getLogger(Endpoint.class.getName());
    protected AmazonSQS sqs = null;
    protected String queueUrl = null;

    public Response checkEnvironmentVariablesAndSetSQSClient(boolean checkQueueUrl)
    {
        String awsSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String awsAccessKey = System.getenv("AWS_ACCESS_KEY_ID");
        //regions https://docs.aws.amazon.com/general/latest/gr/rande.html
        //for example: eu-central-1
        String awsRegion = System.getenv("AWS_REGION");
        
        String msg = "";
        if (awsAccessKey == null || awsSecretKey == null || awsRegion == null) {
            msg = "AWS_ACCESS_KEY_ID and/or AWS_SECRET_ACCESS_KEY and/or AWS_REGION environment variable not found.";
            LOGGER.severe(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }

        if (checkQueueUrl == true)
        {
            queueUrl = System.getenv("AWS_SQS_QUEUE_URL");
            if (queueUrl == null)
            {
                msg = "AWS_SQS_QUEUE_URL environment variable not set.";
                LOGGER.severe(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();    
            }

        }

        sqs = AmazonSQSClientBuilder.defaultClient();

        return null;
    }

    public Response getExceptionResponse(AmazonServiceException ase)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Caught an AmazonServiceException, which means your request made it to Amazon SQS, but was rejected with an error response for some reason.");
        sb.append("\n");
        sb.append("Error Message:    "+ase.getMessage());
        sb.append("\n");
        sb.append("HTTP Status Code: "+ase.getStatusCode());
        sb.append("\n");
        sb.append("AWS Error Code:   "+ase.getErrorCode());
        sb.append("\n");
        sb.append("Error Type:       "+ase.getErrorType());
        sb.append("\n");
        sb.append("Request ID:       "+ase.getRequestId());
        sb.append("\n");
        String msg = sb.toString();
        LOGGER.severe(msg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();

    }

    public Response getExceptionResponse(AmazonClientException ace)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Caught an AmazonClientException, which means "
        + "the client encountered a serious internal problem while "
        + "trying to communicate with Amazon SQS, such as not " + "being able to access the network.");
        sb.append("\n");
        sb.append("Error Message: " + ace.getMessage());
        String msg = sb.toString();
        LOGGER.severe(msg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();

    }
    

}