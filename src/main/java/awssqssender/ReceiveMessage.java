package awssqssender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import awssqssender.object.ReceivedMessage;

//Convenience path for Run class
@Path("/receivemessages")
public class ReceiveMessage extends Endpoint {
    private final static Logger LOGGER = Logger.getLogger(ReceiveMessage.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveMessages() {

        Response rsp = checkEnvironmentVariablesAndSetSQSClient(true);
        if (rsp != null) {
            return rsp;
        }
        // Received messages.
        List<ReceivedMessage> receivedMessages = new ArrayList<ReceivedMessage>();
        try {
            /*
             * Create a new instance of the builder with all defaults (credentials and
             * region) set automatically. For more information, see Creating Service Clients
             * in the AWS SDK for Java Developer Guide.
             */
            LOGGER.info("===============================================");
            LOGGER.info("Receive messages from Amazon SQS Standard Queues");
            LOGGER.info("===============================================\n");

            final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
            final List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            for (final Message message : messages) {

                ReceivedMessage rcvMsg = new ReceivedMessage();
                rcvMsg.id = message.getMessageId();
                rcvMsg.body = message.getBody();
                receivedMessages.add(rcvMsg);
                String receiptHandle = message.getReceiptHandle();
                LOGGER.info("Message");
                LOGGER.info("  MessageId:     " + rcvMsg.id);
                LOGGER.info("  ReceiptHandle: " + receiptHandle);
                LOGGER.info("  MD5OfBody:     " + message.getMD5OfBody());
                LOGGER.info("  Body:          " + rcvMsg.body);
                for (final Entry<String, String> entry : message.getAttributes().entrySet()) {
                    LOGGER.info("Attribute");
                    LOGGER.info("  Name:  " + entry.getKey());
                    LOGGER.info("  Value: " + entry.getValue());
                }
                LOGGER.info("");

                // Delete the message.
                LOGGER.info("Deleting the message...");
                sqs.deleteMessage(new DeleteMessageRequest(queueUrl, receiptHandle));
                LOGGER.info("Message deleted.");
                LOGGER.info("");
            }

        } catch (final AmazonServiceException ase) {
            return getExceptionResponse(ase);
        } catch (final AmazonClientException ace) {
            return getExceptionResponse(ace);
        }

        return Response.status(Response.Status.OK).entity(receivedMessages).build();

    }
}