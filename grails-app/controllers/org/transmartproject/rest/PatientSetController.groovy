package org.transmartproject.rest

import org.codehaus.groovy.grails.web.mime.MimeType
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.core.querytool.QueryDefinitionXmlConverter
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.rest.misc.CurrentUser

import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.BUILD_COHORT
import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.READ

/**
 * Exposes patient set resources.
 */
class PatientSetController {

    static responseFormats = ['json', 'hal']

    @Autowired
    private QueriesResource queriesResource

    @Autowired
    QueryDefinitionXmlConverter queryDefinitionXmlConverter

    @Autowired
    CurrentUser currentUser

    /**
     * Get all patient sets saved by the current user.
     *
     * GET /patient_sets
     */
    def index() {
        String noPatients = "There are no saved patient sets by $currentUser.username"
        def patientSets = queriesResource.getQueryResultsSummaryByUsername(currentUser.getUsername() )
        patientSets = patientSets == null ? noPatients : patientSets
        respond patientSets
    }

    /**
     * Show details of a patient set
     *
     * GET /patient_sets/<result_instance_id>
     */
    def show(Long id) {
        QueryResult queryResult = queriesResource.getQueryResultFromId(id)

        currentUser.checkAccess(READ, queryResult)

        respond queryResult
    }

    /**
     * Create a new patient set.
     *
     * POST /patient_sets
     */
    def save() {
        if (!request.contentType) {
            throw new InvalidRequestException('No content type provided')
        }
        MimeType mimeType = new MimeType(request.contentType)

        if (!(mimeType in [MimeType.XML, MimeType.TEXT_XML])) {
            throw new InvalidRequestException("Content type should been " +
                    "text/xml or application/xml; got $mimeType")
        }

        QueryDefinition queryDefinition =
                queryDefinitionXmlConverter.fromXml(request.reader)

        currentUser.checkAccess(BUILD_COHORT, queryDefinition)

        respond queriesResource.runQuery(queryDefinition, currentUser.username),
                [status: 201]
    }

    /**
     * Disable created patient set.
     *
     * DELETE /patient_sets/<result_instance_id>
     */
    def delete(Long id) {
        respond queriesResource.runDisablingQuery(id, currentUser.username),
                [status: 204]
    }
}
