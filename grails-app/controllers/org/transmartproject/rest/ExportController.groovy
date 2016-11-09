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
        respond restExportService.retrieveDataTypes(params)
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

