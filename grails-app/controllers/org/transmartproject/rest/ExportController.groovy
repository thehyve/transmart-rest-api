package org.transmartproject.rest

import grails.validation.Validateable
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.db.RestExportService
import org.transmartproject.db.querytool.QtQueryResultInstance

class ExportController {

    static responseFormats = ['json', 'hal']


    @Autowired
    RestExportService restExportService

    def sendFileService


    def export(ExportCommand exportCommand) {
        //Used for retrieving the resultinstanceIds
        //def yesterday = new Date() -1
        //def result = QtQueryResultInstance.findAllByStartDateGreaterThan(yesterday )
        //render(result)
        def arguments = [
                conceptKeys: ["MET998": "\\\\Public Studies\\Public Studies\\GSE37427\\Biomarker Data\\MET998\\",
                              "Demographics":"\\\\Public Studies\\Public Studies\\GSE37427\\Demographics\\",
                              "Trial Arm": "\\\\Public Studies\\Public Studies\\GSE37427\\Demographics\\Trial Arm\\",
                             "Control" : "\\\\Public Studies\\Public Studies\\GSE37427\\Demographics\\Trial Arm\\Control\\"],
                            //"Human": "\\\\Public Studies\\Public Studies\\GSE37427\\Biomarker Data\\MET998\\Human\\"],
                resultInstanceIds: [28741, 28740]
        ]
        throwIfInvalid exportCommand
        def filesList = []
        arguments.resultInstanceIds.each { it ->
            def files = restExportService.export(conceptKeys: arguments.conceptKeys, resultInstanceIds: [it])
            files = restExportService.parseFiles(files)
            filesList.add(files)
        }
        File zipFile = restExportService.createZip(filesList)
        sendFileService.sendFile servletContext, request, response, zipFile
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
