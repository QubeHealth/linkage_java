package com.linkage.controller;

import java.util.HashMap;
import java.util.Map;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.ImsNwService;

import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/webhook")
@Produces(MediaType.APPLICATION_JSON)
public class WebhookController extends BaseController {

    private ImsNwService imsNwService;
    public WebhookController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
    }
    // Helper method to create a Response object with status and data
    private Response response(Response.Status status, Object data) {
        return Response.status(status).entity(data).build();
    }

    @POST
    @Path("/eRupee")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eRupeeData(Object body) {

        logger.info("E-RUPEE DATA {}\n", body);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body;

        Map<String, Object> sendData = new HashMap<>();
        sendData.put("terminalId", data.get("terminalId") );
        sendData.put("BankRRN", data.get("BankRRN") );
        sendData.put("merchantTranId", data.get("merchantTranId") );
        sendData.put("PayerName", data.get("PayerName") );
        sendData.put("PayerMobile", data.get("PayerMobile") );
        sendData.put("PayerName", data.get("PayerName") );
        sendData.put("PayerVA", data.get("PayerVA") );
        sendData.put("PayerAmount", data.get("PayerAmount") );
        sendData.put("TxnStatus", data.get("TxnStatus") );
        sendData.put("TxnInitDate", data.get("TxnInitDate") );
        sendData.put("TxnCompletionDate", data.get("TxnCompletionDate") );

        ApiResponse<Object> imsRes = this.imsNwService.redeemVoucher(sendData);
        if (!imsRes.getStatus()) {
            imsRes.setMessage("Failed to process data");
            return response(Response.Status.FORBIDDEN, imsRes);
        } 

        return Response.status(Response.Status.OK).entity(new ApiResponse<>(true, "success", null)).build();
    }

}
