package com.hedwig.morpheus.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hedwig.morpheus.domain.implementation.Report;
import com.hedwig.morpheus.util.tools.JSONUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by hugo. All rights reserved.
 */
@Component
@Scope("singleton")
public class ConfigurationReporter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<UUID, Set<Report>> reports;
    private String configurationId;

    public ConfigurationReporter() {
        reports = Collections.synchronizedMap(new HashMap<>());
    }

    public void addReport(UUID configurationId, Report report) {
        Set<Report> reportSet = reports.get(configurationId);

        if (null == reportSet) {
            reportSet = new HashSet<>();
        }

        reportSet.add(report);
        reports.put(configurationId, reportSet);
    }

    public String generateReportForConfiguration(UUID configurationId) {
        Set<Report> reportSet = reports.get(configurationId);

        if (null != reportSet) {
            try {
                return JSONUtilities.serialize(reportSet);
            } catch (JsonProcessingException e) {
                logger.error("Error when trying to serialize report for configuration: " + configurationId, e);
            }
            reports.remove(configurationId);
        }

        return "";
    }
}
