package org.fungover.resourceserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

@SpringBootApplication
public class ResourceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceServerApplication.class, args);
    }
}

interface CustomerRepository extends ListCrudRepository<Customer, Integer> {
}

record Customer(@Id Integer id, String name, String email) {
}

@Controller
@ResponseBody
class CustomerHttpController {

    private final CustomerRepository repository;

    CustomerHttpController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/customers")
    Collection<Customer> customers() {
        return this.repository.findAll();
    }
}

@Controller
@ResponseBody
class MeHttpController {
    @GetMapping("/me")
    Map<String, String> principal(Principal principal) {
        return Map.of("name", principal.getName());
    }
}
