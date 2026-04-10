package com.ticketscale.interfaces.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendForwardController {

    @RequestMapping(value = {"/admin/**", "/admin"})
    public String forward() {
        return "forward:/admin/index.html";
    }
}
