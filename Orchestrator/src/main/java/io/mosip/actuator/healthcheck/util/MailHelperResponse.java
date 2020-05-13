package io.mosip.actuator.healthcheck.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailHelperResponse {
	 private String body;
     private String regexout;
     public MailHelperResponse(String b, String r){
         this.body = b;
         this.regexout = r;
     }
}
