package pt.ist.fenixframework.dml.decorator;

import com.qubit.solution.fenixedu.bennu.versioning.domain.UpdateEntity;
import com.qubit.solution.fenixedu.bennu.versioning.domain.UpdateTimestamp;
import com.qubit.solution.fenixedu.bennu.versioning.service.ModelDecorator.FenixFrameworkDomainModelDecorator;
import com.qubit.solution.fenixedu.bennu.versioning.service.VersionableObject;

import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.ExternalizationElement;
import pt.ist.fenixframework.dml.PlainValueType;
import pt.ist.fenixframework.dml.Slot;
import pt.ist.fenixframework.dml.ValueType;

public class VersioningDecorator implements FenixFrameworkDomainModelDecorator {

    @Override
    public void decorateModel(DomainModel domainModel) {
        ValueType stringValueType = domainModel.findValueType("String");
        ValueType dateTimeValueType = domainModel.findValueType("DateTime");

        PlainValueType updateTimeStampValueType =
                new PlainValueType(UpdateTimestamp.class.getSimpleName(), UpdateTimestamp.class.getName());
        updateTimeStampValueType.addExternalizationElement(new ExternalizationElement(dateTimeValueType, "externalize"));

        PlainValueType updateEntityValueType =
                new PlainValueType(UpdateEntity.class.getSimpleName(), UpdateEntity.class.getName());
        updateEntityValueType.addExternalizationElement(new ExternalizationElement(stringValueType, "externalize"));

        domainModel.newValueType(UpdateTimestamp.class.getSimpleName(), updateTimeStampValueType);
        domainModel.newValueType(UpdateEntity.class.getSimpleName(), updateEntityValueType);

        Slot creator = new Slot("versioningCreator", stringValueType);
        Slot creationDate = new Slot("versioningCreationDate", dateTimeValueType);
        Slot updatedBy = new Slot("versioningUpdatedBy", updateEntityValueType);
        Slot updateDate = new Slot("versioningUpdateDate", updateTimeStampValueType);

        for (DomainClass domainClass : domainModel.getDomainClasses()) {
            if (domainClass.getSuperclass() != null) {
                continue;
            }
            domainClass.getInterfacesNames().add(VersionableObject.class.getName());
            domainClass.addSlot(creator);
            domainClass.addSlot(creationDate);
            domainClass.addSlot(updatedBy);
            domainClass.addSlot(updateDate);
        }

    }
}
