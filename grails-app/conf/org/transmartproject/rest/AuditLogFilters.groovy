package org.transmartproject.rest

import org.apache.commons.logging.Log;
import org.transmartproject.core.users.User

class AuditLogFilters {

    def accessLogService
    def auditLogService
    User currentUserBean

    def filters = {
        lowDim(controller: 'observation', action:'*') {
            after = { model ->
                def fullUrl = "${request.forwardURI}${request.queryString ? '?' + request.queryString : ''}"
                def ip = request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr

                accessLogService.report(currentUserBean, 'REST API Data Retrieval',
                        eventMessage:  "User (IP: ${ip}) got low dim. data with ${fullUrl}",
                        requestURL: fullUrl)

                auditLogService.report("REST API access (low dim)", request,
                        user: currentUserBean,
                        action: fullUrl as String
                )
            }
        }

        highDim(controller: 'highDim', action:'*') {
            after = { model ->
                def fullUrl = "${request.forwardURI}${request.queryString ? '?' + request.queryString : ''}"
                def ip = request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr

                accessLogService.report(currentUserBean, 'REST API Data Retrieval',
                        eventMessage:  "User (IP: ${ip}) got high dim. data with ${fullUrl}",
                        requestURL: fullUrl)

                auditLogService.report("REST API access (high dim)", request,
                        user: currentUserBean,
                        action: fullUrl as String
                )
            }
        }
        resources(controller: 'study|concept|subject|patientSet', action: '*') {
            before = { model ->
                def fullUrl = "${request.forwardURI}${request.queryString ? '?' + request.queryString : ''}"
                auditLogService.report("REST API access (${controllerName}.${actionName})", request,
                        user: currentUserBean,
                        action: fullUrl as String
                )
            }
        }
    }

}
