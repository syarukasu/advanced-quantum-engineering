package com.syaru.advancedquantumengineering.integration;

import java.math.BigInteger;

/** Exact reservation and physical state belong to the existing Advanced AE CPU. */
public interface AqeExactCpuAccess {
    void aqe$installExactSubmission(AqePhysicalExecution.Submission submission);
    BigInteger aqe$exactReservation();
    boolean aqe$hasPhysicalExecution();
}
