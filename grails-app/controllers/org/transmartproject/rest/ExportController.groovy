package org.transmartproject.rest

import grails.validation.Validateable
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.db.RestExportService
import org.transmartproject.db.querytool.QtQueryResultInstance
import org.transmartproject.rest.misc.JsonParametersParser

class ExportController {

    static responseFormats = ['json', 'hal']

    @Autowired
    RestExportService restExportService

    def sendFileService


    def export(ExportCommand exportCommand) {
        def filesList = []
        throwIfInvalid exportCommand
        //def arguments = retrieveArguments()
        exportCommand.each { it ->
            def exportFiles = restExportService.export(it)
            def parsedFiles = restExportService.parseFiles(exportFiles, it.exportDataFormat)
            filesList += parsedFiles
        }
        File zipFile = restExportService.createZip(filesList)
        sendFileService.sendFile servletContext, request, response, zipFile
    }

    def datatypes(){
        //Retrieve datatype
        def arguments = JsonParametersParser.parseConstraints(params)
    }

    private void throwIfInvalid(command) {
        if (command.hasErrors()) {
            List errorStrings = command.errors.allErrors.collect {
                g.message(error: it, encodeAs: 'raw')
            }
            throw new InvalidArgumentsException("Invalid input: $errorStrings")
        }
    }

    def retrieveArguments(){
        //Used for retrieving the resultinstanceIds
        //def yesterday = new Date() -1
        //def result = QtQueryResultInstance.findAllByStartDateGreaterThan(yesterday )
        //render(result)
        def arguments_GSE37427_merged= [
                "conceptKeys": ["MET998": "\\\\Public Studies\\Public Studies\\GSE37427\\Biomarker Data\\MET998\\",
                              "Demographics":"\\\\Public Studies\\Public Studies\\GSE37427\\Demographics\\",
                              "Trial Arm": "\\\\Public Studies\\Public Studies\\GSE37427\\Demographics\\Trial Arm\\",
                              "Control" : "\\\\Public Studies\\Public Studies\\GSE37427\\Demographics\\Trial Arm\\Control\\"],
                //"Human": "\\\\Public Studies\\Public Studies\\GSE37427\\Biomarker Data\\MET998\\Human\\"],
                "resultInstanceIds": [28741, 28740],
                "exportDataFormat": ["tsv"]
        ]
        def arguments_CLUC_separated =
                [[
                         "resultInstanceIds": [28742],
                         "conceptKeys": [
                                 "Agilent miRNA microarray": "\\\\Public Studies\\Public Studies\\CLUC\\Molecular profiling\\High-throughput molecular profiling\\Expression (miRNA)\\Agilent miRNA microarray\\",
                                 "Demographics": "\\\\Public Studies\\Public Studies\\CLUC\\Characteristics\\",
                                 "MZ ratios": "\\\\Public Studies\\Public Studies\\CLUC\\Molecular profiling\\High-throughput molecular profiling\\Expression (protein)\\LC-MS-MS\\Protein level\\TPNT\\MZ ratios\\"
                         ],
                         "exportDataFormat": ["csv", "tsv"]
                 ], [
                         "resultInstanceIds": [28743],
                         "conceptKeys": [
                                 "Agilent miRNA microarray": "\\\\Public Studies\\Public Studies\\CLUC\\Molecular profiling\\High-throughput molecular profiling\\Expression (miRNA)\\Agilent miRNA microarray\\",
                                 "Demographics": "\\\\Public Studies\\Public Studies\\CLUC\\Characteristics\\",
                                 "MZ ratios": "\\\\Public Studies\\Public Studies\\CLUC\\Molecular profiling\\High-throughput molecular profiling\\Expression (protein)\\LC-MS-MS\\Protein level\\TPNT\\MZ ratios\\"
                         ],
                         "exportDataFormat": ["tsv"]
                 ]]
        def arguments_CLUC_merged =
                [[
                         "resultInstanceIds": [28742, 28743],
                         "conceptKeys": [
                                 "Agilent miRNA microarray": "\\\\Public Studies\\Public Studies\\CLUC\\Molecular profiling\\High-throughput molecular profiling\\Expression (miRNA)\\Agilent miRNA microarray\\",
                                 "Demographics": "\\\\Public Studies\\Public Studies\\CLUC\\Characteristics\\",
                                 "MZ ratios": "\\\\Public Studies\\Public Studies\\CLUC\\Molecular profiling\\High-throughput molecular profiling\\Expression (protein)\\LC-MS-MS\\Protein level\\TPNT\\MZ ratios\\"
                         ],
                         "exportDataFormat": ["csv", "tsv"]
                 ]]
        def argumentsJSON = JsonOutput.toJson(arguments_CLUC_separated)
        def jsonSlurper = new JsonSlurper()
        def arguments = jsonSlurper.parseText(argumentsJSON)
        arguments
    }


}

@Validateable
class ExportCommand {
    Map arguments = [:]

    static constraints = {
        arguments nullable: false
    }
}
