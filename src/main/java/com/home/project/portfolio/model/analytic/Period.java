package com.home.project.portfolio.model.analytic;

import java.time.LocalDateTime;

/**
 * Period that is used to retrieve operations
 */
public enum Period {
    LAST_DAY {
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofDays(1);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return false;
        }
    },
    LAST_WEEK {
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofWeeks(1);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return false;
        }
    },
    LAST_MONTH {
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofMonths(1);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return false;
        }
    },
    LAST_SIX_MONTH {
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofMonths(6);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return false;
        }
    },
    LAST_YEAR {
        @Override
        public java.time.Period getPeriodDuration() {
            return java.time.Period.ofYears(1);
        }

        @Override
        public boolean isIntermediatePeriod() {
            return false;
        }
    },
    ALL_TIME {
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

}
