package com.hedwig.morpheus.domain.implementation;

import java.util.Objects;

/**
 * Created by hugo. All rights reserved.
 */
public class Result {
    boolean success;
    String description;

    public Result() {
    }

    public Result(boolean success, String description) {
        this.success = success;
        this.description = description;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return success == result.success && Objects.equals(description, result.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, description);
    }
}
