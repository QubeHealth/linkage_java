package com.linkage.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.LoansService;
import com.linkage.client.MailReaderService;
import com.linkage.client.MasterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/mail")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MailController extends BaseController {

    private MailReaderService mailReaderService;
    private MasterService masterService;
    private LoansService loansService;

    public MailController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.mailReaderService = new MailReaderService(null, null, null, null, configuration);
        this.masterService = new MasterService(configuration);
        this.loansService = new LoansService(configuration);
    }

    @POST
    @Path("/emailReader")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> emailReader(@Context HttpServletRequest request) throws MessagingException, IOException {
        return this.mailReaderService.fetchAndProcessEmail();

        //Pre-Auth flow db calls
        Map<String, Object> preFundedReqMap = new HashMap<>();
        preFundedReqMap.put("user_id","123");
        preFundedReqMap.put("hsp_id","123");
        preFundedReqMap.put("partnered_user_id",policy_no);
        preFundedReqMap.put("tpa_desk_id",KH_id);
        preFundedReqMap.put("status","PENDING");
        preFundedReqMap.put("type","TPA");
        preFundedReqMap.put("processed_at",null);
        preFundedReqMap.put("requested_amount",null);
        preFundedReqMap.put("disbursement_amount",null);
        preFundedReqMap.put("is_active",true);
        preFundedReqMap.put("claim_no",null);

        Long preFundedRequest = (Long)this.loansService.preFundedrequestStore(preFundedReqMap);
       
        Map<String,Object> preFundedEmailerMap = new HashMap<>();
        preFundedEmailerMap.put("type","PRE AUTH");
        preFundedEmailerMap.put("subject",subject);
        preFundedEmailerMap.put("is_active",true);
        preFundedEmailerMap.put("claim_no",null);
        preFundedEmailerMap.put("pf_req_id", preFundedRequest.toString());

        Long prefundedEmail = (Long)this.masterService.prefundedEmail(preFundedEmailerMap);

        Map<String, Object> emailerItems = new HashMap<>();
        emailerItems.put("tpa_desk_id",KH_id);
        emaileritems.put("claim_no", null);
        emailerItems.put("initial_amt_req",null);
        emailerItems.put("initial_amt_approved",null);
        emaileritems.put("final_adj_amt_req", null);
        emailerItems.put("final_adj_amt_approved",null);
        emailerItems.put("metadata",metadata);
        emailerItems.put("patient_name",patient_name);

        Long emailItems = (Long)this.masterService.emailInsert(emailerItems);

        Map<String, Object> adjudicationDataMap = new HashMap<>();
        adjudicationDataMap.put("")

    }

    // @POST
    // @Path("/emailDataStore")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public Response emailDataStore(@Context HttpServletRequest request, Map<String, String> requestBody) {
    //     try {
    //         //ApiResponse<Object> dApiResponse = masterService.mailDataStore(requestBody);
    //         return Response.status(Response.Status.OK)
    //                 .entity(new ApiResponse<>(true, "Data stored successfully", null))
    //                 .build();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    //                 .entity(new ApiResponse<>(false, "Error storing data", null))
    //                 .build();
    //     }
    // }
}