package com.qubit.solution.fenixedu.bennu.versioning.ui;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.fenixedu.bennu.spring.portal.SpringApplication;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

public class BennuVersioningBaseController {

    //The HTTP Request that can be used internally in the controller
    protected @Autowired HttpServletRequest request;

    //The entity in the Model

    // The list of INFO messages that can be showed on View
    protected void addInfoMessage(String message, Model m) {
        ((List<String>) m.asMap().get("infoMessages")).add(message);
    }

    // The list of WARNING messages that can be showed on View
    protected void addWarningMessage(String message, Model m) {
        ((List<String>) m.asMap().get("warningMessages")).add(message);
    }

    // The list of ERROR messages that can be showed on View
    protected void addErrorMessage(String message, Model m) {
        ((List<String>) m.asMap().get("errorMessages")).add(message);
    }

    @ModelAttribute
    protected void addModelProperties(Model model) {
        model.addAttribute("infoMessages", new ArrayList<String>());
        model.addAttribute("warningMessages", new ArrayList<String>());
        model.addAttribute("errorMessages", new ArrayList<String>());

        //Add here more attributes to the Model
        //model.addAttribute(<attr1Key>, <attr1Value>);
        //....
    }

}
