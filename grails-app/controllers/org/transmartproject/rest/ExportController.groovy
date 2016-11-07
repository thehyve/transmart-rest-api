package org.transmartproject.rest

import grails.validation.Validateable
import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.db.RestExportService

class ExportController {

    static responseFormats = ['json', 'hal']


    @Autowired
    RestExportService restExportService

    def sendFileService


    def export(ExportCommand exportCommand) {
        throwIfInvalid exportCommand
        def files = restExportService.export(arguments)
        sendFileService.sendFile servletContext, request, response, files[0]  //TODO: send all files, for instance as a zip
    }

    /**GET request on /export/datatypes
     *  Returns datatypes and patient number of given concepts.
     *
     */
    def datatypes() throws NoSuchResourceException {
        if (!(params.containsKey('concepts'))){
            throw new NoSuchResourceException("No parameter named concepts was given.")
        }
        if (params.get('concepts') == "") {
            throw new InvalidArgumentsException("Parameter concepts has no value.")
        }
        def jsonSlurper = new JsonSlurper()
        def conceptParameters = params.get('concepts').decodeURL()
        try {
            def conceptArguments = jsonSlurper.parseText(conceptParameters)
            List dataTypes = []
            int cohortNumber = 1
            conceptArguments.each { it ->
                List conceptKeysList = it.conceptKeys
                dataTypes = restExportService.getDataTypes(conceptKeysList, dataTypes, cohortNumber)
                cohortNumber += 1
            }
            respond(dataTypes)
        } catch(JsonException e){
            throw new InvalidArgumentsException("Given parameter was non valid JSON.")
        }

    }

    private void throwIfInvalid(command) {
        if (command.hasErrors()) {
            List errorStrings = command.errors.allErrors.collect {
                g.message(error: it, encodeAs: 'raw')
            }
            throw new InvalidArgumentsException("Invalid input: $errorStrings")
        }
    }

}

@Validateable
class ExportCommand {
    Map arguments = [:]

    static constraints = {
        arguments nullable: false
    }
}

