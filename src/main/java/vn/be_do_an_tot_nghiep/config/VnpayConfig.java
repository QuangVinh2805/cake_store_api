package vn.be_do_an_tot_nghiep.config;

import java.util.HashMap;
import java.util.Map;

public class VnpayConfig {

    // 👉 Sandbox
    public static final String TMN_CODE = "XMBAEJ5Q"; // lấy trong portal sandbox
    public static final String HASH_SECRET = "XHNSGEKNSMDIJ19V2OMAJWZL16D50T03"; // sandbox secret
    public static final String PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String RETURN_URL = "http://localhost:8080/api/order/vnpay/callback";
    public static final String vnp_Version = "2.1.0";
    public static final String vnp_Command = "pay";
    public static final String vnp_Locale = "vn";
    public static final String vnp_CurrCode = "VND";

    public static Map<String, String> getBaseParams() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", TMN_CODE);
        params.put("vnp_Locale", vnp_Locale);
        params.put("vnp_CurrCode", vnp_CurrCode);
        return params;
    }
}
