package org.transmartproject.rest.marshallers

import org.transmartproject.export.DataTypeRetrieved

class DataTypeSerializationHelper  extends AbstractHalOrJsonSerializationHelper<DataTypeRetrieved>{

    final Class targetType = DataTypeRetrieved
    final String collectionName = 'dataTypeRetrieved'

    @Override
    Map<String, Object> convertToMap(DataTypeRetrieved dataTypeRetrieved) {
        def cohortInfoList = []
        def cohortsMap = [:]
        dataTypeRetrieved.OntologyTermsMap.each { ID, terms ->
            terms.collect { term ->
                if (ID in cohortsMap.keySet()) {
                    cohortsMap[ID].add([subjects: term.patients.collect({ it.id }), conceptPath: term.fullName])
                } else {
                    cohortsMap[ID] = [[subjects: term.patients.collect({ it.id }), conceptPath: term.fullName]]
                }
            }
        }
        cohortsMap.each{ key, value ->
            cohortInfoList.add([concepts:value])
        }
        def datatypeMap = [dataType:dataTypeRetrieved.dataType,
                           dataTypeCode: dataTypeRetrieved.dataTypeCode,
                           cohorts:cohortInfoList]
    }

}
