package com.linkage.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.storage.Storage.AnywhereCaches.List;
import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.GetVpaByMobileSchema;
import com.linkage.core.validations.RefereeInviteMsgSchema;
import com.linkage.utility.Helper;

import jakarta.ws.rs.core.MultivaluedHashMap;

public class WatiService extends BaseServiceClient {

    public WatiService(LinkageConfiguration configuration) {
        super(configuration);
    }

    public ApiResponse<Object> referreeInviteMessage(RefereeInviteMsgSchema body) {

        MultivaluedHashMap<String, Object> header = new MultivaluedHashMap<>();

        header.putSingle("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJkYTU3MGE2My00NTMxLTQ3NTItOTUyYi01Y2U0YzY2ZWFiZjkiLCJ1bmlxdWVfbmFtZSI6Imt1bmFsLmthbWJsZUBxdWJlaGVhbHRoLmNvbSIsIm5hbWVpZCI6Imt1bmFsLmthbWJsZUBxdWJlaGVhbHRoLmNvbSIsImVtYWlsIjoia3VuYWwua2FtYmxlQHF1YmVoZWFsdGguY29tIiwiYXV0aF90aW1lIjoiMDMvMjAvMjAyNCAwNzoyMToxMCIsImRiX25hbWUiOiJtdC1wcm9kLVRlbmFudHMiLCJ0ZW5hbnRfaWQiOiIxMTM3OTgiLCJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL3dzLzIwMDgvMDYvaWRlbnRpdHkvY2xhaW1zL3JvbGUiOlsiREVWRUxPUEVSIiwiQVVUT01BVElPTl9NQU5BR0VSIl0sImV4cCI6MjUzNDAyMzAwODAwLCJpc3MiOiJDbGFyZV9BSSIsImF1ZCI6IkNsYXJlX0FJIn0.YJa1N5fV3-nHLofACDrHOlIpHtKBZ1TBHkge0Rj5Df0");
        String url = "https://live-mt-server.wati.io/113798/api/v1/sendTemplateMessage?whatsappNumber=91" + body.getMobile();
        
        //logger.info("mobile upi supreme response {}", Helper.toJsonString(response));

        //Arraylist of key value pairs then added to hashmap
        Map<String, String> parameter = new HashMap<>();
        parameter.put("name", "cashback_amount");
        parameter.put("value", "100");
        ArrayList<Map<String, String>> parameters = new ArrayList<>();
        parameters.add(parameter);
        Map<String, Object> mainMap = new HashMap<>();
        mainMap.put("template_name", "qp_cashback_referal_invite24may2024");
        mainMap.put("broadcast_name", "test referal");
        mainMap.put("parameters", parameters);

       return this.networkCallExternalService(url, "POST", mainMap, header);


    }

}