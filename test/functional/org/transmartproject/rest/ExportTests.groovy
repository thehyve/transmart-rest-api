package org.transmartproject.rest

import org.codehaus.groovy.grails.web.mime.MimeType

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasKey

/**
 * Created by piotrzakrzewski on 07/10/2016.
 */
class ExportTests extends ResourceTestCase{

    String version = "v1"

    void testExportArgumentError() {
        def resp = postAsHal("/$version/export/export") {
            contentType MimeType.JSON.name
            body {
                arguments: [
                        conceptKeys:[],
                        resultInstanceIds: [],
                        resultInstanceIds: [],
                ]
            }
        }
        assertStatus 400
    }

    void testExportClinicalData() {
        def resp = postAsHal("/$version/export/export") {
            contentType MimeType.JSON.name
            body {
                arguments: [
                        conceptKeys:[],
                        resultInstanceIds: [],
                        resultInstanceIds: [],
                ]
            }
        }
        assertStatus 200
    }

}
