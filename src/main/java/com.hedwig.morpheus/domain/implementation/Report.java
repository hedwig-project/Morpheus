package com.hedwig.morpheus.domain.implementation;

import com.hedwig.morpheus.domain.enums.ReportingResult;
import com.hedwig.morpheus.domain.enums.ReportingType;

/**
 * Created by hugo. All rights reserved.
 */
public class Report {
    ReportingType type;
    String identification;
    ReportingResult result;
    String detailedDescription;

    public Report() {
    }

    public ReportingType getType() {
        return type;
    }

    public void setType(ReportingType type) {
        this.type = type;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public ReportingResult getResult() {
        return result;
    }

    public void setResult(ReportingResult result) {
        this.result = result;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public Report reportType(ReportingType type) {
        this.type = type;
        return this;
    }

    public Report reportIdentification(String identification) {
        this.identification = identification;
        return this;
    }

    public Report reportResult(ReportingResult result) {
        this.result = result;
        return this;
    }

    public Report reportDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
        return this;
    }
}
