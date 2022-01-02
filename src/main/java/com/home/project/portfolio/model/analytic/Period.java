package com.home.project.portfolio.model.analytic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.home.project.portfolio.exception.IncorrectDurationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Period that is used to retrieve operations
 */
@Getter
@RequiredArgsConstructor
public enum Period {
    LAST_DAY("day"){
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofDays(1);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return false;
        }
    },
    LAST_WEEK("week") {
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofWeeks(1);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return false;
        }
    },
    LAST_MONTH("month") {
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofMonths(1);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return false;
        }
    },
    LAST_SIX_MONTH("month") {
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofMonths(6);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return false;
        }
    },
    LAST_YEAR("year") {
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofYears(1);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return false;
        }
    },
    ALL_TIME("all_time") {
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofYears(20);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return true;
        }
};

    public abstract java.time.Period getPeriodDuration();
    public abstract boolean isIntermediatePeriod();
    private final String interval;

    @JsonCreator
    public static Period parse(String value) {
        return Stream.of(values())
                .filter(period -> value.equalsIgnoreCase(period.getInterval()))
                .findFirst()
                .orElseThrow(() -> new IncorrectDurationException(HttpStatus.BAD_REQUEST,
                        value + " is not allowed. Available periods: "
                                + Arrays.toString(Arrays.stream(values())
                                .map(Period::getInterval)
                                .toArray())));
    }
}
