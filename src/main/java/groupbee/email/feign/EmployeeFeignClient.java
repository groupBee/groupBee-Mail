package groupbee.email.feign;

import groupbee.email.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "employee", url = "${FEIGN_BASE_URL}", configuration = FeignConfig.class)
public interface EmployeeFeignClient {
    @GetMapping("/api/employee/info")
    Map<String,Object> getUserInfo();
}
