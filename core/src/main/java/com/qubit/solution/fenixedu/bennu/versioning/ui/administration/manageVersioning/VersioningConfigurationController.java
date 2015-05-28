package com.qubit.solution.fenixedu.bennu.versioning.ui.administration.manageVersioning;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pt.ist.fenixframework.Atomic;

import com.qubit.solution.fenixedu.bennu.versioning.domain.VersioningConfiguration;
import com.qubit.solution.fenixedu.bennu.versioning.ui.BennuVersioningBaseController;
import com.qubit.solution.fenixedu.bennu.versioning.ui.BennuVersioningController;

@SpringFunctionality(app = BennuVersioningController.class, title = "label.title.administration.manageVersioning")
@RequestMapping("/bennuVersioning/administration/manageversioning/versioningconfiguration")
public class VersioningConfigurationController extends BennuVersioningBaseController {

    @RequestMapping
    public String home(Model model) {
        return "redirect:/bennuVersioning/administration/manageversioning/versioningconfiguration/";
    }

    @RequestMapping(value = "/")
    public String search(@RequestParam(value = "classname", required = false) java.lang.String classname, @RequestParam(
            value = "active", required = false) java.lang.Boolean active, Model model) {
        List<VersioningConfiguration> searchversioningconfigurationResultsDataSet =
                filterSearchVersioningConfiguration(classname, active);

        // add the results dataSet to the model
        model.addAttribute("searchversioningconfigurationResultsDataSet", searchversioningconfigurationResultsDataSet);
        return "bennu-versioning/administration/manageversioning/versioningconfiguration/search";
    }

    private List<VersioningConfiguration> collectSearchVersioningConfigurationDataSet() {
        return new ArrayList(Bennu.getInstance().getVersioningConfigurationsSet());
    }

    private List<VersioningConfiguration> filterSearchVersioningConfiguration(java.lang.String classname, java.lang.Boolean active) {

        return collectSearchVersioningConfigurationDataSet()
                .stream()
                .filter(versioningConfiguration -> classname == null
                        || classname.length() == 0
                        || (versioningConfiguration.getClassname() != null && versioningConfiguration.getClassname().length() > 0 && versioningConfiguration
                                .getClassname().toLowerCase().contains(classname.toLowerCase())))
                .filter(versioningConfiguration -> active == null || active.equals(versioningConfiguration.getActive()))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/activate/{oid}")
    public String searchToActivateAction(@PathVariable("oid") VersioningConfiguration versioningConfiguration, Model model) {
        modify(versioningConfiguration, true);
        return "redirect:/bennuVersioning/administration/manageversioning/versioningconfiguration/";
    }

    @Atomic
    private void modify(VersioningConfiguration versioningConfiguration, boolean active) {
        versioningConfiguration.setActive(active);
    }

    @RequestMapping(value = "/deactivate/{oid}")
    public String searchToDeactivateAction(@PathVariable("oid") VersioningConfiguration versioningConfiguration, Model model) {
        modify(versioningConfiguration, false);
        return "redirect:/bennuVersioning/administration/manageversioning/versioningconfiguration/";
    }
}
