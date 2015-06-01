package com.qubit.solution.fenixedu.bennu.versioning.ui.administration.manageTarget;

import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import pt.ist.fenixframework.Atomic;

import com.qubit.solution.fenixedu.bennu.versioning.domain.VersioningTargetConfiguration;
import com.qubit.solution.fenixedu.bennu.versioning.ui.BennuVersioningBaseController;
import com.qubit.solution.fenixedu.bennu.versioning.ui.BennuVersioningController;

@SpringFunctionality(app = BennuVersioningController.class, title = "label.title.administration.manageTarget")
@RequestMapping("/bennuVersioning/administration/managetarget")
public class VersioningTargetConfigurationController extends BennuVersioningBaseController {

    @RequestMapping
    public String home(Model model) {
        return "forward:/bennuVersioning/administration/managetarget/read";
    }

    private void setVersioningTargetConfiguration(VersioningTargetConfiguration versioningTargetConfiguration, Model m) {
        m.addAttribute("versioningTargetConfiguration", versioningTargetConfiguration);
    }

    @RequestMapping(value = "/read")
    public String read(Model model) {

        setVersioningTargetConfiguration(VersioningTargetConfiguration.getInstance(), model);
        return "bennu-versioning/administration/managetarget/versioningtargetconfiguration/read";
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public String update(Model model) {
        setVersioningTargetConfiguration(VersioningTargetConfiguration.getInstance(), model);
        return "bennu-versioning/administration/managetarget/versioningtargetconfiguration/update";
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public String update(@RequestParam(value = "jdbcurl", required = false) java.lang.String jdbcURL, @RequestParam(
            value = "username", required = false) java.lang.String username,
            @RequestParam(value = "password", required = false) java.lang.String password, Model model) {

        updateVersioningTargetConfiguration(jdbcURL, username, password, model);

        return "redirect:/bennuVersioning/administration/managetarget/read";

    }

    @Atomic
    public void updateVersioningTargetConfiguration(java.lang.String jdbcURL, java.lang.String username,
            java.lang.String password, Model m) {
        VersioningTargetConfiguration.getInstance().edit(jdbcURL, username, password);
    }

}
