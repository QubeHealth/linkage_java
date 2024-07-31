package com.linkage.core.constants;

import java.util.Arrays;
import java.util.List;

public final class Constants {

    public static final class EmailKeywords {

        public static final String QUERY_REPLY = "query reply";
        public static final String SUPPORTING_DOCUMENT = "supporting document";
        public static final String FINAL_BILL_AND_DISCHARGE_SUMMARY = "final bill and discharge summary";
        public static final String FINAL_CASHLESS_CREDIT_REQUEST = "final cashless credit request";
        public static final String INITIAL_CASHLESS_CREDIT_REQUEST = "initial cashless credit request";
        public static final String PRE_AUTH = "pre auth";
        public static final String CASHLESS_CREDIT_REQUEST = "CASHLESS CREDIT REQUEST";
        public static final String ADDITIONAL_INFORMATION = "addtional information";
        public static final String ERROR = "error";
        public static final String INITIAL_CASHLESS_APPROVED = "Initial Cashless Approved Amount:-";
        public static final String FINAL_CASHLESS_APPROVED = "Final Cashless Approved Amount:-";

        public static final String USER_ID = "user_id";
        public static final String TYPE = "type";
        public static final String EMAIL_TYPE = "email_type";
        public static final String PATIENT_NAME = "patient_name";
        public static final String CLAIM_NO = "claim_no";
        public static final String SUBJECT = "subject";
        public static final String BODY = "body";
        public static final String TPA_DESK_ID = "tpa_desk_id";
        public static final String EMPLOYEE_NAME = "employee_name";
        public static final String EMPLOYEE_CODE = "employee_code";
        public static final String FINAL_CASHLESS_APPROVED_AMT = "final_cashless_approved_amount";
        public static final String FINAL_CASHLESS_REQUEST_AMT = "final_cashless_request_amount";
        public static final String DOCUMENT_REQUIRED = "document_required";
        public static final String PARTNERED_EMP_ID = "partnered_emp_id";
        public static final String INITIAL_CASHLESS_APPROVED_AMT = "requested_amount_initial";
        public static final String INITIAL_CASHLESS_REQUEST_AMT = "approved_amount_initial";
        public static final String GCP_PATH = "gcp_path";
        public static final String GCP_FILE_NAME = "gcp_file_name";
        public static final String IS_ACTIVE = "is_active";
        public static final String PF_REQUEST_ID = "pf_request_id";
        public static final String POLICY_NO = "policy_no";
        public static final String STATUS = "status";
        public static final String METADATA = "metadata";
        public static final String FILE_PATH = "file_path";
        public static final String FILE_NAME = "file_name";
        public static final String HSP_ID = "hsp_id";
        public static final String ADJUDICATION_DATA_ID = "adjudication_data_id";
        public static final String PF_DOCUMENT_ID = "pf_document_id";
        public static final String DOCUMENT_URL = "document_url";
        public static final String TPA = "TPA";
        public static final String CREATED_BY = "created_by";
        public static final String UPDATED_BY = "updated_by";
        public static final String PF_EMAILER_ID = "pf_emailer_id";
        public static final String PF_REQ_ID = "pf_req_id";

        public static final String PENDING = "PENDING";
        public static final String APPROVED = "APPROVED";


        public static final String[] keywordsArray = { SUPPORTING_DOCUMENT, QUERY_REPLY,
                FINAL_BILL_AND_DISCHARGE_SUMMARY, PRE_AUTH,
                CASHLESS_CREDIT_REQUEST, ADDITIONAL_INFORMATION };

        public static final List<String> adjudicatorKeywords = Arrays.asList(
                "addtional information", "cashless credit request");

        public static final List<String> tpaKeywords = Arrays.asList(
                "pre auth", "final bill and discharge summary", "query reply");
    }

}
