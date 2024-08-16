package com.linkage.controller;

import java.util.Set;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.client.ErupeeService;
import com.linkage.core.validations.ErupeeSchema.VoucherRequest;
import com.linkage.core.validations.ErupeeSchema.VoucherStatus;
import com.linkage.utility.Helper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/api/erupee")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ErupeeController extends BaseController {

    private ErupeeService erupeeService;

    public ErupeeController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
        this.erupeeService = new ErupeeService(configuration);
    }

    @POST
    @Path("/createVoucher")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> createVoucher(@Context HttpServletRequest request,
            VoucherRequest body) {
        Set<ConstraintViolation<VoucherRequest>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        ApiResponse<Object> res = this.erupeeService.creatVoucher(body);
        System.out.println("RES ==>\n" + Helper.toJsonString(res));

        return res;

    }

    // e-rupi voucher status api 

    @POST
    @Path("/voucherStatus")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResponse<Object> voucherStatus(@Context HttpServletRequest request,VoucherStatus body) {
        Set<ConstraintViolation<VoucherStatus>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            // Construct error message from violations
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce("", (acc, msg) -> acc.isEmpty() ? msg : acc + "; " + msg);
            return new ApiResponse<>(false, errorMessage, null);
        }

        ApiResponse<Object> res = this.erupeeService.voucherStatus(body);
        System.out.println("RES ==>\n" + Helper.toJsonString(res));

        return res;
    }

}
