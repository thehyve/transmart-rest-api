package org.transmartproject.rest

import grails.validation.Validateable
import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.apache.commons.lang.NullArgumentException
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.db.RestExportService
import org.transmartproject.db.querytool.QtQueryResultInstance

import java.nio.file.Path

class ExportController {

    static responseFormats = ['json', 'hal']

    @Autowired
    RestExportService restExportService

    def sendFileService

    /**POST request on /export/
     *  Returns a zipfile containing the data from the selected cohort.
     *
     */
    def export(ExportCommand exportCommand) {
        throwIfInvalid exportCommand
        //TODO: IDEA restExportService.export() returns list with results object instead of files.
        def arguments = retrieveArguments()
        Path tmpPath = restExportService.createTmpDir()
        arguments.each { it ->
            def exportFiles = restExportService.export(it)
            def parsedFiles = restExportService.parseFiles(exportFiles, it.exportDataFormat)
            restExportService.createDirStructure(parsedFiles, tmpPath, it)
        }
        File zipFile = restExportService.createZip(tmpPath)
        sendFileService.sendFile servletContext, request, response, zipFile
    }

    /**GET request on /export/datatypes
     *  Returns datatypes and patient number of given concepts.
     *
     */
    def datatypes(){
        def jsonSlurper = new JsonSlurper()
        if (!(params.containsKey('concepts'))){
            throw new NoSuchElementException(
                    "No parameter named concepts."
            )
        }
        def test = params.get('concepts').decodeURL()
        try {
            def concept_arguments = jsonSlurper.parseText(test)
            if (concept_arguments==null){
                throw new NullArgumentException(
                        "Parameter concepts has no value."
                )
            }
            List datatypes = []
            concept_arguments.each { it ->
                List conceptKeysList = it.conceptKeys
                datatypes += restExportService.getDataTypes(conceptKeysList)
            }
            respond(restExportService.formatDataTypes(datatypes))
        } catch(JsonException e){
            "Given value was non valid JSON."
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

