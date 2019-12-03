package awssqssender;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import awssqssender.object.Message;
import awssqssender.object.Status;

/*
Call to send message to Amazon SQS

Sample curl
curl -v --request POST --url http://localhost:8085/sendmessage --header 'Content-Type: application/json' --data '{"msg":"Hello World 5!"}'
*/

//Path is /run because OpenWhisk requires so.
@Path("/run")
public class Run extends Endpoint {

    private final static Logger LOGGER = Logger.getLogger(Run.class.getName());

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage(Message message) {

        Response rsp = checkEnvironmentVariablesAndSetSQSClient(true);
        if (rsp != null)
        {
            return rsp;
        }

        LOGGER.info("===============================================");
        LOGGER.info("Send message to Amazon SQS Standard Queue");
        LOGGER.info("===============================================\n");
        try {
            // Send message to Queue URL specified in environment variable
            sqs.sendMessage(new SendMessageRequest(queueUrl, message.msg));

        } catch (final AmazonServiceException ase) {
            return getExceptionResponse(ase);
        } catch (final AmazonClientException ace) {
            return getExceptionResponse(ace);
        }
        String msg = "Sent: " + message.msg;
        LOGGER.info(msg);
        Status status = new Status();
        status.status = "Message sent.";
        return Response.status(Response.Status.OK).entity(status).build();
    }
}