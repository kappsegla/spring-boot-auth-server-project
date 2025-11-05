package org.fungover.oauthclient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevToolsController {
    @RequestMapping("/.well-known/appspecific/com.chrome.devtools.json")
    public ResponseEntity<Void> handleDevToolsRequest() {
        return ResponseEntity.ok().build();
    }
}