package org.transmartproject.rest

import grails.validation.Validateable
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.db.RestExportService
import org.transmartproject.db.querytool.QtQueryResultInstance
import org.transmartproject.rest.misc.JsonParametersParser

import java.nio.file.Path

class ExportController {

    static responseFormats = ['json', 'hal']

    @Autowired
    RestExportService restExportService

    def sendFileService

    def conceptsResourceService

    def export() {
        //throwIfInvalid exportCommand
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

    def datatypes(){
        //Retrieve datatype
        //Get total number of unique patients belonging to the concepts which have the datatype.
        def concept_arguments = retrieveArguments()
        List datatypes = []
        concept_arguments.each { it ->
            List cohortDataTypes = []
            it.conceptKeys.each { conceptKey ->
                Map datatypeMap = [:]
                OntologyTerm concept = conceptsResourceService.getByKey(conceptKey)
                def datatype = restExportService.getHighDimMetaData(concept)
                datatypeMap['conceptKey'] = conceptKey
                datatypeMap['dataType'] = datatype.get('dataTypes')[0]
                datatypeMap['numOfPatients'] = 10
                cohortDataTypes.add(datatypeMap)
            }
            datatypes.add(cohortDataTypes)
        }
        print(datatypes)
        respond(datatypes)
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
        def arguments_GSE37427_merged= [[
                "conceptKeys": ["MET998": "\\\\Public Studies\\Public Studies\\GSE37427\\Biomarker Data\\MET998\\",
                              "Demographics":"\\\\Public Studies\\Public Studies\\GSE37427\\Demographics\\",
                              "Trial Arm": "\\\\Public Studies\\Public Studies\\GSE37427\\Demographics\\Trial Arm\\",
                              "Control" : "\\\\Public Studies\\Public Studies\\GSE37427\\Demographics\\Trial Arm\\Control\\"],
                "resultInstanceIds": [28741, 28740],
                "exportDataFormat": ["tsv"]
        ]]
        def arguments_two_studies_merged =[[ conceptKeys: [
                "Biomarker Data":"\\\\Public Studies\\Public Studies\\GSE8581\\Biomarker Data\\",
                "LOG":"\\\\Public Studies\\Public Studies\\GSE8581\\Biomarker Data\\GPL570_BOGUS\\LOG\\",
                "Human":"\\\\Public Studies\\Public Studies\\GSE37427\\Biomarker Data\\MET998\\Human\\"],
                                         resultInstanceIds: [28750, 28751],
                                         exportDataFormat: ['tsv']]]
        def arguments_two_studies_separated = [[ conceptKeys: [
                "Biomarker Data":"\\\\Public Studies\\Public Studies\\GSE8581\\Biomarker Data\\",
                "GPL570_BOGUS":"\\\\Public Studies\\Public Studies\\GSE8581\\Biomarker Data\\GPL570_BOGUS\\"],
                                             resultInstanceIds: [28750],
                                             exportDataFormat: ['tsv']],
                [ conceptKeys: [
                                "Human":"\\\\Public Studies\\Public Studies\\GSE37427\\Biomarker Data\\MET998\\Human\\",
                                "Biomarker Data":"\\\\Public Studies\\Public Studies\\GSE37427\\Biomarker Data\\",
                "Demographics":"\\\\Public Studies\\Public Studies\\GSE37427\\Demographics\\"],
                 resultInstanceIds: [28751],
                 exportDataFormat: ['tsv']
        ]]
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

        def highdim_datatypes =
                [["conceptKeys": [
                        "\\\\Public Studies\\Public Studies\\CLUC\\Molecular profiling\\",
                        "\\\\Public Studies\\Public Studies\\CLUC\\Molecular profiling\\High-throughput molecular profiling\\Expression (protein)\\LC-MS-MS\\Protein level\\TPNT\\MZ ratios\\"
                ]],
                 ["conceptKeys": [
                         "\\\\Public Studies\\Public Studies\\GSE37427\\Biomarker Data\\",
                         "\\\\Public Studies\\Public Studies\\GSE37427\\Biomarker Data\\MET998\\Human\\"]
                ]]

        //def argumentsJSON = JsonOutput.toJson(arguments_CLUC_separated)
        //def jsonSlurper = new JsonSlurper()
        //def arguments = jsonSlurper.parseText(argumentsJSON)
        highdim_datatypes
    }


}

@Validateable
class ExportCommand {
    Map arguments = [:]

    static constraints = {
        arguments nullable: false
    }
}
