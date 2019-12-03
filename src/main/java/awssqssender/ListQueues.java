package awssqssender;

import awssqssender.object.QueueInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

@Path("/listqueues")
public class ListQueues extends Endpoint {

    private final static Logger LOGGER = Logger.getLogger(ListQueues.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listQueues() {

        Response rsp = checkEnvironmentVariablesAndSetSQSClient(false);
        if (rsp != null)
        {
            return rsp;
        }

        /*
         * Create a new instance of the builder with all defaults (credentials and
         * region) set automatically. For more information, see Creating Service Clients
         * in the AWS SDK for Java Developer Guide.
         */
        LOGGER.info("===============================================");
        LOGGER.info("List Amazon SQS Standard Queues");
        LOGGER.info("===============================================\n");
        List<QueueInfo> queues = new ArrayList<QueueInfo>();
        try {
            // List all queues.
            LOGGER.info("Listing all queues in your account.\n");
            for (final String queueUrl : sqs.listQueues().getQueueUrls()) {
                
                QueueInfo q = new QueueInfo();
                q.url = queueUrl;
                queues.add(q);
                LOGGER.info("  QueueUrl: " + queueUrl);
            }
            LOGGER.info("");

        } catch (final AmazonServiceException ase) {
            return getExceptionResponse(ase);
        } catch (final AmazonClientException ace) {
            return getExceptionResponse(ace);
        }

        return Response.status(Response.Status.OK).entity(queues).build();        
    }
}