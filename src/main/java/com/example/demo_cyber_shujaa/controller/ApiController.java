package com.example.demo_cyber_shujaa.controller;


import com.example.demo_cyber_shujaa.dto.Hello;
import com.example.demo_cyber_shujaa.dto.Response;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/demo")
public class ApiController {

    @PostMapping("/secured")
    public ResponseEntity<Response> hello(@RequestBody @Valid Hello request) {
        return ResponseEntity.ofNullable(Response.ofSuccess(0, "From secured validated object"));
    }

    @GetMapping("/unsecured")
    public ResponseEntity<Response> getStatus() {

        return ResponseEntity.ofNullable(Response.ofSuccess(0, "unsecured endpoint"));
    }
}
