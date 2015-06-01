/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: paulo.abrantes@qub-it.com
 *
 * 
 * This file is part of FenixEdu bennu-versioning-core.
 *
 * FenixEdu bennu-versioning-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu bennu-versioning-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu bennu-versioning-core.  If not, see <http://www.gnu.org/licenses/>.
 */
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
