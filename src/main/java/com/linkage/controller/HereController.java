// package com.linkage.controller;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.Set;

// import com.google.rpc.Help;
// import com.linkage.LinkageConfiguration;
// import com.linkage.api.ApiResponse;
// import com.linkage.client.HereService;
// import com.linkage.core.validations.HereSearchSchema;
// import com.linkage.utility.Helper;

// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.validation.ConstraintViolation;
// import jakarta.validation.Validator;
// import jakarta.ws.rs.Consumes;
// import jakarta.ws.rs.POST;
// import jakarta.ws.rs.Path;
// import jakarta.ws.rs.Produces;
// import jakarta.ws.rs.core.Context;
// import jakarta.ws.rs.core.MediaType;
// import jakarta.ws.rs.core.Response;

// @Path("/api/heremaps")
// @Produces(MediaType.APPLICATION_JSON)
// public class HereController extends BaseController {

//     public HereService hereService;

//     public HereController(LinkageConfiguration configuration, Validator validator) {
//         super(configuration, validator);

//         this.hereService = new HereService(configuration);
//     }

//     private Response response(Response.Status status, Object data) {
//         return Response.status(status).entity(data).build();
//     }

//     @POST
//     @Path("/hereSearch")
//     @Produces(MediaType.APPLICATION_JSON)
//     @Consumes(MediaType.APPLICATION_JSON)
//     public Object hereSearch(@Context HttpServletRequest request, HereSearchSchema reqBody) {

//         Set<ConstraintViolation<HereSearchSchema>> violations = validator.validate(reqBody);
//         if (!violations.isEmpty()) {
//             String errorMessage = violations.stream()
//                     .map(ConstraintViolation::getMessage)
//                     .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
//             return "";
//         }

//         ApiResponse<Object> hereResponse = this.hereService.hereSearch(reqBody); 
//         System.out.println(hereResponse);
//         if(!hereResponse.getStatus()) {
//             return "";
//         }

//         HashMap<Object, Object> res = new HashMap<>();
//         res.put("res", hereResponse.getData());

//         System.out.println(hereResponse.getData().getClass().getSimpleName());
//         return Response.status(Response.Status.OK).entity( new ApiResponse<>(false, hereResponse.getMessage(),Helper.convertJsonToString(hereResponse.getData())) ).build();
//     };
//     }

