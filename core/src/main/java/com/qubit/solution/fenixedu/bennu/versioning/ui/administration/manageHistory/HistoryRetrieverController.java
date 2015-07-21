package com.qubit.solution.fenixedu.bennu.versioning.ui.administration.manageHistory;

import java.util.List;
import java.util.Map;

import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.spring.portal.SpringFunctionality;
import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;

import com.qubit.solution.fenixedu.bennu.versioning.domain.VersioningConfiguration;
import com.qubit.solution.fenixedu.bennu.versioning.service.HistoryRetriever;
import com.qubit.solution.fenixedu.bennu.versioning.service.VersionableObject;
import com.qubit.solution.fenixedu.bennu.versioning.ui.BennuVersioningBaseController;
import com.qubit.solution.fenixedu.bennu.versioning.ui.BennuVersioningController;
import com.qubit.solution.fenixedu.bennu.versioning.util.VersioningConstants;

@SpringFunctionality(app = BennuVersioningController.class, title = "label.title.administration.manageHistory")
@RequestMapping(HistoryRetrieverController.CONTROLLER_URL)
public class HistoryRetrieverController extends BennuVersioningBaseController {
    public static final String CONTROLLER_URL = "/bennuVersioning/administration/managehistory";
    private static final String SEARCH_URI = "/";
    public static final String SEARCH_URL = CONTROLLER_URL + SEARCH_URI;
    private static final String READ_URI = "/read/";
    public static final String READ_URL = CONTROLLER_URL + READ_URI;
    private static final String CHECK_VERSION_URI = "/checkVersion/";
    public static final String CHECK_VERSION_URL = CONTROLLER_URL + CHECK_VERSION_URI;

    private VersionableObject getVersionableObject(Model model) {
        return (VersionableObject) model.asMap().get("versionableObject");
    }

    private void setVersionableObject(VersionableObject versionableObject, Model model) {
        model.addAttribute("versionableObject", versionableObject);
    }

    @RequestMapping
    public String home(Model model) {
        return "forward:" + SEARCH_URL;
    }

    @RequestMapping(value = SEARCH_URI)
    public String search(@RequestParam(value = "oid", required = false) String oid, Model model,
            RedirectAttributes redirectAttributes) {
        if (oid != null) {
            return redirect(READ_URL + oid, model, redirectAttributes);
        }

        return "bennu-versioning/administration/managehistory/versionableobject/search";
    }

    @RequestMapping(value = READ_URI + "{oid}")
    public String read(@PathVariable("oid") DomainObject domainObject, Model model, RedirectAttributes redirectAttributes) {
        if (!FenixFramework.isDomainObjectValid(domainObject)) {
            addErrorMessage(BundleUtil.getString(VersioningConstants.BUNDLE, "error.read.versionableobject.oid.not.valid"), model);
            return redirect(SEARCH_URL, model, redirectAttributes);
        }
        VersionableObject versionableObject = (VersionableObject) domainObject;

        setVersionableObject(versionableObject, model);
        HistoryRetriever historyRetriever = new HistoryRetriever(versionableObject);

        List<Map<String, Object>> retrieveModificationsInVersions = historyRetriever.retrieveVersionsOnlyModifications();
        Map<String, Map<String, List<Map<String, Object>>>> retrieveRelatedObjectsVersions =
                historyRetriever.retrieveVersionsOfRelatedObjects();

        model.addAttribute("retrieveModificationsInVersions", retrieveModificationsInVersions);
        model.addAttribute("retrieveRelatedObjectsVersions", retrieveRelatedObjectsVersions);

        if (!VersioningConfiguration.getConfigurationFor(versionableObject.getClass().getName()).getActive()) {
            addInfoMessage(BundleUtil.getString(VersioningConstants.BUNDLE, "label.Versioning.object.not.active"), model);
        }
        return "bennu-versioning/administration/managehistory/versionableobject/readDetailed";
    }

    @RequestMapping(value = CHECK_VERSION_URI + "{oid}")
    public @ResponseBody String checkVersion(@PathVariable("oid") DomainObject domainObject,
            @RequestParam(value = "updateDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.S") DateTime updateDateTime,
            Model model, RedirectAttributes redirectAttributes) {
        VersionableObject versionableObject = (VersionableObject) domainObject;
        if (versionableObject == null) {
            return "null";
        }
        if (updateDateTime.isEqual(versionableObject.getVersioningUpdateDate())) {
            return "true";
        }
        return "false";
    }
}
