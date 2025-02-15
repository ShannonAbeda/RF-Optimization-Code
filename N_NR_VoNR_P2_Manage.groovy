/*
CPR NAME: N_NR_VoNR_P2_Rules_Manage
Developed by: Shannon Abeda
Date: Oct 1 24


V1 (Oct 1 24):
    This rule is to enforce parameter changes for VoNR updates as per requested by Marie France Bourque 
    This CPR first will find the NR freqBandIndicator and apply the updates to those parameters
    The condition is that the specific NRCELL must match the NRSYSINFO_PROFILE_DN in order for those updates to be enforced
    The CPR is broken down into multiple function statements to simply redability
    Due to the complexity of theses rule changes, I create this CPR in lieu of a simple rule as NRSYSINFO_PROFILE_DN cannot be obtain via
    Simple rule

*/
cpr_name = "N_NR_VoNR_P2_Rules_Manage";
logger.info(" **** Beginning of " + cpr_name);
def number_of_nrtbs = 0;

//These sites have null MO'S
def blackListSites = [1557559, 1557562, 1772059, 1555165];

// This function here is for N5 Bands and will do the inconsistency check for theses values
// Function takes in NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index
// Returns: None
def n5_FreqBand = { NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS ->
    logger.info(" INFO: We are evaluating the cell: ${NRcell}");
    logger.info(" INFO: The freqBand is: ${current_freqBand}");
    logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this cell is: ${NRSYSINFO_PROFILE_DN}");
    logger.info(" INFO: The Index_DN_NRCELL on this cell is: ${Index_DN_NRCELL}");
    logger.info(" INFO: The current_qRxLevMin_NRCELL is: ${current_qRxLevMin_NRCELL} and the needed value is: -60");

    
    

    if(current_qRxLevMin_NRCELL != -60) {
        logger.info(" INFO: Updating the qRxLevMin");
        addRecommendation("UPDATE", NRCELL, "UPDATE qRxLevMin to correct value", "qRxLevMin", -60)
    }

    else  {
        logger.info(" INFO: current_qRxLevMin_NRCELL is aligned with recommended value");
    }
    logger.info(" INFO: End of evaluation of ${NRcell} on freqBand: ${current_freqBand}");
    logger.info("**********************************************************************************************************************************************************************************************");

    
    if(NRSYSINFO_PROFILES.size() != 0) {
        for(NRSYSINFO_PROFILE in NRSYSINFO_PROFILES) {
            NRSYSINFO_PROFILE_DistName  = NRSYSINFO_PROFILE.getParameter("distName"); // EG: PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRSYSINFO_PROFILE-1
            NRSYSINFO_PROFILE_DN_Index  = NRSYSINFO_PROFILE_DistName.substring(NRSYSINFO_PROFILE_DistName.indexOf("/") + 1); // EG: MRBTS-1549769/NRBTS-1549769/NRSYSINFO_PROFILE-1
            current_sNonIntraSearchP    = NRSYSINFO_PROFILE.getParameterAsInteger("sNonIntraSearchP");
            current_threshServingLowP   = NRSYSINFO_PROFILE.getParameterAsInteger("threshServingLowP");
            current_qHyst               = NRSYSINFO_PROFILE.getParameterAsInteger("qHyst");
            logger.info(" INFO: The current distName on this NRSYSINFO_PROFILE is: ${NRSYSINFO_PROFILE_DistName}");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this NRCELL is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: The FreqBand on this cell is: ${current_freqBand}");
            logger.info(" INFO: current_sNonIntraSearchP is ${current_sNonIntraSearchP} and the needed value is: 2")
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN_Index is: ${NRSYSINFO_PROFILE_DN_Index} and the NRSYSINFO_PROFILE_DN under this NRCELL is: ${NRSYSINFO_PROFILE_DN} ");
            logger.info(" INFO: current_threshServingLowP is ${current_threshServingLowP} and the needed value is: 2");
            logger.info(" INFO: current_qHyst is ${current_qHyst} and the needed value is: 2");
            

            if(NRSYSINFO_PROFILE_DN_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: The NRSYSINFO_PROFILE_DN cell is aligned");
                logger.info(" INFO: We will update the NRSYSINFO_PROFILE Parameters");


                if(current_sNonIntraSearchP != 2) {
                    logger.info(" INFO: Updating sNonIntraSearchP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE sNonIntraSearchP to correct value", "sNonIntraSearchP", 2);
                }

                else {
                    logger.info(" INFO: sNonIntraSearchP is aligned with the recommended value");
                }

                if(current_threshServingLowP != 2) {
                    logger.info(" INFO: Updating the threshServingLowP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE threshServingLowP to correct value", "threshServingLowP", 2)
                }

                else {
                    logger.info(" INFO: threshServingLowP is aligned with the recommended value");
                }

                if(current_qHyst != 2) {
                    logger.info(" INFO: Updating the qHyst");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE qHyst to correct value", "qHyst", 2);
                }

                else {
                    logger.info(" INFO: qHyst aligned with recommended value");
                }
                logger.info(" INFO: End of evaluation on ${NRSYSINFO_PROFILE_DistName} on NRCELL: ${NRcell} freqBand ${current_freqBand}");
                logger.info("**********************************************************************************************************************************************************************************************");

            }

            else {
                logger.info(" INFO: The NRSYSINFO_PROFILE_DN is not aligned with on this cell");
                logger.info("**********************************************************************************************************************************************************************************************");
            }
        }
    }
    logger.info(" INFO: Inconsistency check for NRSYSINFO_PROFILE Complete");
    logger.info(" INFO: Inconsistency checking now for NRIAFIM");
    logger.info("**********************************************************************************************************************************************************************************************");
                
    if(NRIAFIMS.size() != 0) {
        for(NRIAFIM in NRIAFIMS) {
            NRIAFIMS_DistName                   = NRIAFIM.getParameter("distName");
            current_qRxLevMin_NRIAFIM           = NRIAFIM.getParameterAsInteger("qRxLevMin");
            current_sIntraSearchP               = NRIAFIM.getParameterAsInteger("sIntraSearchP");
            current_tReselectionNr_NRIAFIM      = NRIAFIM.getParameterAsInteger("tReselectionNr");
            NRIAFIMS_Sys_Index                  = NRIAFIMS_DistName.split("/")[1..3].join("/")
    
            logger.info(" INFO: Evaluating NRIAFIM")
            logger.info(" INFO: The currentFreqBand is: ${current_freqBand}");
            logger.info(" INFO: The current distName on this NRIAFIM profile is: ${NRIAFIMS_DistName}");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this NRCELL is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: The FreqBand on this cell is: ${current_freqBand}");
            logger.info(" INFO: The current NRIAFIMS_Sys_Index is: ${NRIAFIMS_Sys_Index} and the needed value is: ${NRSYSINFO_PROFILE_DN} ");
            logger.info(" INFO: current_qRxLevMin_NRIAFIM is ${current_qRxLevMin_NRIAFIM} and the needed value is: -60");
            logger.info(" INFO: current_sIntraSearchP is ${current_sIntraSearchP} and the needed value is: 31");
            logger.info(" INFO: current_tReselectionNr_NRIAFIM is ${current_tReselectionNr_NRIAFIM} and the needed value is: 1");


            if(NRIAFIMS_Sys_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: NRIAFIMS_Sys_Index equals to NRSYSINFO_PROFILE_DN_Index")

                if(current_qRxLevMin_NRIAFIM != -60) {
                    logger.info(" INFO: Updating qRxLevMin");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE qRxLevMin to correct value", "qRxLevMin", -60);
                }

                if(current_sIntraSearchP != 31) {
                    logger.info(" INFO: Updating the sIntraSearchP");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE sIntraSearchP to correct value", "sIntraSearchP", 31)
                }

                if(current_tReselectionNr_NRIAFIM != 1) {
                    logger.info(" INFO: Updating the tReselectionNr");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE tReselectionNr to correct value", "tReselectionNr", 1);
                }
                logger.info(" INFO: End of evaluation of ${NRIAFIMS_DistName} on NRCELL: ${NRcell} Band: ${current_freqBand}");
                logger.info("**********************************************************************************************************************************************************************************************");
            }

            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }
        }
    }
    logger.info(" INFO: Inconsistency check for NRIAFIM complete");
    logger.info(" INFO: End of FreqBand ${current_freqBand}");
    logger.info("**********************************************************************************************************************************************************************************************");
}

// This function here is for N25 Bands and will do the inconsistency check for theses values
// Function takes in NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index
// Returns: None
def n25_FreqBand = { NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS ->
    logger.info(" INFO: We are evaluating the cell: ${NRcell}");
    logger.info(" INFO: The freqBand is: ${current_freqBand}");
    logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this cell is: ${NRSYSINFO_PROFILE_DN}");
    logger.info(" INFO: The Index_DN_NRCELL on this cell is: ${Index_DN_NRCELL}");
    logger.info(" INFO: The current_qRxLevMin_NRCELL is: ${current_qRxLevMin_NRCELL} and the needed value is: -58");

    if(current_qRxLevMin_NRCELL != -58) {
        logger.info(" INFO: Updating the qRxLevMin");
        addRecommendation("UPDATE", NRCELL, "UPDATE qRxLevMin to correct value", "qRxLevMin", -58)
    }

    else {
        logger.info(" INFO: qRxLevMin aligned with recommended value");
    }
    logger.info(" INFO: End of evaluation of NRcell: ${NRcell} on freqBand: ${current_freqBand}")
    logger.info("**********************************************************************************************************************************************************************************************");

    logger.info(" INFO: Now evaluating NRSYSINFO_PROFILE");
    if(NRSYSINFO_PROFILES.size() != 0) {
        for(NRSYSINFO_PROFILE in NRSYSINFO_PROFILES) {
            NRSYSINFO_PROFILE_DistName  = NRSYSINFO_PROFILE.getParameter("distName"); // EG: PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRSYSINFO_PROFILE-1
            NRSYSINFO_PROFILE_DN_Index  = NRSYSINFO_PROFILE_DistName.substring(NRSYSINFO_PROFILE_DistName.indexOf("/") + 1);
            current_sNonIntraSearchP    = NRSYSINFO_PROFILE.getParameterAsInteger("sNonIntraSearchP");
            current_threshServingLowP   = NRSYSINFO_PROFILE.getParameterAsInteger("threshServingLowP");
            current_qHyst               = NRSYSINFO_PROFILE.getParameterAsInteger("qHyst");
            logger.info(" INFO: The current distName on this NRSYSINFO_PROFILE is: ${NRSYSINFO_PROFILE_DistName}");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this NRCELL is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: The FreqBand on this cell is: ${current_freqBand}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN_Index is: ${NRSYSINFO_PROFILE_DN_Index} and the NRSYSINFO_PROFILE_DN under this NRCELL is: ${NRSYSINFO_PROFILE_DN} ");
            logger.info(" INFO: current_sNonIntraSearchP is ${current_sNonIntraSearchP} and the needed value is: 2")
            logger.info(" INFO: current_threshServingLowP is ${current_threshServingLowP} and the needed value is: 2");
            logger.info(" INFO: current_qHyst is ${current_qHyst} and the needed value is: 2");



            if(NRSYSINFO_PROFILE_DN_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: The NRSYSINFO_PROFILE_DN_Index equals to Index_DN_NRCELL");
        
                if(current_sNonIntraSearchP != 1) {
                    logger.info(" INFO: Updating sNonIntraSearchP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE sNonIntraSearchP to correct value", "sNonIntraSearchP", 1);
                }

                else {
                    logger.info(" INFO: sNonIntraSearchP is aligned with recommended value");
                }

                if(current_threshServingLowP != 1) {
                    logger.info(" INFO: Updating the threshServingLowP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE threshServingLowP to correct value", "threshServingLowP", 1)
                }

                else {
                    logger.info(" INFO: threshServingLowP is aligned with recommended value");
                }

                if(current_qHyst != 2) {
                    logger.info(" INFO: Updating the qHyst");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE qHyst to correct value", "qHyst", 2);
                }

                else {
                    logger.info(" INFO: qHyst is aligned with recommended value");
                }
                logger.info(" INFO: End of evaluation on ${NRSYS_PROFILE_DistName} on NRCELL: ${NRcell} freqBand: ${current_freqBand}")
                logger.info("**********************************************************************************************************************************************************************************************");
            }

            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }

        }
    }
    logger.info(" INFO: Inconsistency checking Complete");
    logger.info(" INFO: Inconsistency checking now for NRIAFIM");
    logger.info("**********************************************************************************************************************************************************************************************");

    if(NRIAFIMS.size() != 0) {
        for(NRIAFIM in NRIAFIMS) {
            NRIAFIMS_DistName                   = NRIAFIM.getParameter("distName");
            current_qRxLevMin_NRIAFIM           = NRIAFIM.getParameterAsInteger("qRxLevMin");
            current_sIntraSearchP               = NRIAFIM.getParameterAsInteger("sIntraSearchP");
            current_tReselectionNr_NRIAFIM      = NRIAFIM.getParameterAsInteger("tReselectionNr");
            NRIAFIMS_Sys_Index                  = NRIAFIMS_DistName.split("/")[1..3].join("/")
    
            logger.info(" INFO: The currentFreqBand is: ${current_freqBand}");
            logger.info(" INFO: The current distName on this NRIAFIM profile is: ${NRIAFIMS_DistName}");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this NRCELL is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: The FreqBand on this cell is: ${current_freqBand}");
            logger.info(" INFO: The current NRIAFIMS_Sys_Index is: ${NRIAFIMS_Sys_Index} and the needed value is ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: current_qRxLevMin_NRIAFIM is ${current_qRxLevMin_NRIAFIM} and the needed value is: -58");
            logger.info(" INFO: current_sIntraSearchP is ${current_sIntraSearchP} and the needed value is: 29");
            logger.info(" INFO: current_tReselectionNr_NRIAFIM is ${current_tReselectionNr_NRIAFIM} and the needed value is: 1");

            if(NRIAFIMS_Sys_Index == NRSYSINFO_PROFILE_DN) {

                if(current_qRxLevMin_NRIAFIM != -58) {
                    logger.info(" INFO: Updating qRxLevMin");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE qRxLevMin to correct value", "qRxLevMin", -58);
                }

                if(current_sIntraSearchP != 29) {
                    logger.info(" INFO: Updating the sIntraSearchP");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE sIntraSearchP to correct value", "sIntraSearchP", 29)
                }

                if(current_tReselectionNr_NRIAFIM != 1) {
                    logger.info(" INFO: Updating the tReselectionNr");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE tReselectionNr to correct value", "tReselectionNr", 1);
                }
                logger.info(" INFO: End of evaluation of ${NRIAFIMS_DistName} on NRCELL: ${NRcell} Band: ${current_freqBand}");
                logger.info("**********************************************************************************************************************************************************************************************");
            }

            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }
        }
    }
    logger.info(" INFO: End of FreqBand ${current_freqBand}");
    logger.info("**********************************************************************************************************************************************************************************************");
}
// This function here is for N66 Bands and will do the inconsistency check for theses values
// Function takes in NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index
// Returns: None
def n66_FreBand = { NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS->
     logger.info(" INFO: We are evaluating the cell: ${NRcell}");
    logger.info(" INFO: The freqBand is: ${current_freqBand}");
    logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this cell is: ${NRSYSINFO_PROFILE_DN}");
    logger.info(" INFO: The Index_DN_NRCELL on this cell is: ${Index_DN_NRCELL}");
    logger.info(" INFO: The current_qRxLevMin_NRCELL is: ${current_qRxLevMin_NRCELL} and the needed value is: -58");

    if(current_qRxLevMin_NRCELL != -58) {
        logger.info(" INFO: Updating the qRxLevMin");
        addRecommendation("UPDATE", NRCELL, "UPDATE qRxLevMin to correct value", "qRxLevMin", -58)
    }

    else {
        logger.info(" INFO: qRxLevMin aligned with recommended value");
    }
    logger.info(" INFO: End of evaluation of ${NRcell} on FreqBand: ${current_freqBand}");
    logger.info("**********************************************************************************************************************************************************************************************");


    if(NRSYSINFO_PROFILES.size() != 0) {
        for(NRSYSINFO_PROFILE in NRSYSINFO_PROFILES) {
            NRSYSINFO_PROFILE_DistName  = NRSYSINFO_PROFILE.getParameter("distName"); // EG: PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRSYSINFO_PROFILE-1
            NRSYSINFO_PROFILE_DN_Index  = NRSYSINFO_PROFILE_DistName.substring(NRSYSINFO_PROFILE_DistName.indexOf("/") + 1);
            current_sNonIntraSearchP    = NRSYSINFO_PROFILE.getParameterAsInteger("sNonIntraSearchP");
            current_threshServingLowP   = NRSYSINFO_PROFILE.getParameterAsInteger("threshServingLowP");
            current_qHyst               = NRSYSINFO_PROFILE.getParameterAsInteger("qHyst");
            logger.info(" INFO: The current distName on this NRSYSINFO_PROFILE is: ${NRSYSINFO_PROFILE_DistName}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN_Index is: ${NRSYSINFO_PROFILE_DN_Index} and the NRSYSINFO_PROFILE_DN under this NRCELL is: ${NRSYSINFO_PROFILE_DN} ");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The FreqBand on this cell is: ${current_freqBand}");
            logger.info(" INFO: current_sNonIntraSearchP is ${current_sNonIntraSearchP} and the needed value is: 1")
            logger.info(" INFO: current_threshServingLowP is ${current_threshServingLowP} and the needed value is: 1");
            logger.info(" INFO: current_qHyst is ${current_qHyst} and the needed value is: 2");


            if(NRSYSINFO_PROFILE_DN_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: The NRSYSINFO_PROFILE_DN matches on this cell ");
            
                if(current_sNonIntraSearchP != 1) {
                    logger.info(" INFO: Updating sNonIntraSearchP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE sNonIntraSearchP to correct value", "sNonIntraSearchP", 1);
                }

                if(current_threshServingLowP != 1) {
                    logger.info(" INFO: Updating the threshServingLowP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE threshServingLowP to correct value", "threshServingLowP", 1)
                }

                if(current_qHyst != 2) {
                    logger.info(" INFO: Updating the qHyst");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE qHyst to correct value", "qHyst", 2);
                }
                logger.info(" INFO: End of evaluation of ${NRSYSINFO_PROFILE_DistName} on NRCELL: ${NRcell} freqBand: ${current_freqBand}");
                logger.info("**********************************************************************************************************************************************************************************************");
            }

            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }
        }
    }
            
    logger.info(" INFO: Inconsistency check for NRSYSINFO_PROFILE");
    logger.info(" INFO: Inconsistency checking now for NRIAFIM");
    logger.info("**********************************************************************************************************************************************************************************************");

    if(NRIAFIMS.size() != 0) {
        for(NRIAFIM in NRIAFIMS) {
            NRIAFIMS_DistName                   = NRIAFIM.getParameter("distName");
            current_qRxLevMin_NRIAFIM           = NRIAFIM.getParameterAsInteger("qRxLevMin");
            current_sIntraSearchP               = NRIAFIM.getParameterAsInteger("sIntraSearchP");
            current_tReselectionNr_NRIAFIM      = NRIAFIM.getParameterAsInteger("tReselectionNr");
            NRIAFIMS_Sys_Index                  = NRIAFIMS_DistName.split("/")[1..3].join("/")
    
            logger.info(" INFO: The currentFreqBand is: ${current_freqBand}");
            logger.info(" INFO: The current distName on this NRIAFIM profile is: ${NRIAFIMS_DistName}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this NRCELL is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The FreqBand on this cell is: ${current_freqBand}");
            logger.info(" INFO: The current NRIAFIMS_Sys_Index is: ${NRIAFIMS_Sys_Index} and the needed value is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: current_qRxLevMin_NRIAFIM is ${current_qRxLevMin_NRIAFIM} and the needed value is: -60");
            logger.info(" INFO: current_sIntraSearchP is ${current_sIntraSearchP} and the needed value is: 31");
            logger.info(" INFO: current_tReselectionNr_NRIAFIM is ${current_tReselectionNr_NRIAFIM} and the needed value is: 1");

            if(NRIAFIMS_Sys_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: NRIAFIMS_Sys_Index equals to NRSYSINFO_PROFILE_DN_Index")


                if(current_qRxLevMin_NRIAFIM != -58) {
                    logger.info(" INFO: Updating qRxLevMin");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE qRxLevMin to correct value", "qRxLevMin", -58);
                }

                if(current_sIntraSearchP != 29) {
                    logger.info(" INFO: Updating the sIntraSearchP");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE sIntraSearchP to correct value", "sIntraSearchP", 29)
                }

                if(current_tReselectionNr_NRIAFIM != 1) {
                    logger.info(" INFO: Updating the tReselectionNr");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE tReselectionNr to correct value", "tReselectionNr", 1);
                }
                logger.info(" INFO: End of evaluation of ${NRIAFIMS_DistName} on NRCELL: ${NRcell} Band: ${current_freqBand}");
                logger.info("**********************************************************************************************************************************************************************************************");
            }

            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }
        }
    }
    logger.info(" INFO: Inconsistency check for NRIAFIM complete");
    logger.info(" INFO: End of FreqBand ${current_freqBand}");
    logger.info("**********************************************************************************************************************************************************************************************");
}

// This function here is for N71 Bands and will do the inconsistency check for theses values
// Function takes in NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index
// Returns: None
def n71_FreqBand = { NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS ->
    logger.info(" INFO: We are evaluating the cell: ${NRcell}");
    logger.info(" INFO: The freqBand is: ${current_freqBand}");
    logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this cell is: ${NRSYSINFO_PROFILE_DN}");
    logger.info(" INFO: The Index_DN_NRCELL on this cell is: ${Index_DN_NRCELL}");
    logger.info(" INFO: The current_qRxLevMin_NRCELL is: ${current_qRxLevMin_NRCELL} and the needed value is: -60");

    if(current_qRxLevMin_NRCELL != -60) {
        logger.info(" INFO: Updating the qRxLevMin");
        addRecommendation("UPDATE", NRCELL, "UPDATE qRxLevMin to correct value", "qRxLevMin", -60)
    }

    else {
        logger.info(" INFO: qRxLevMin aligned with recommended value");
    }
    logger.info(" INFO: End of evaluation of NRCELL: ${NRcell} on Band: ${current_freqBand}");
    logger.info("**********************************************************************************************************************************************************************************************");


    if(NRSYSINFO_PROFILES.size() > 0) {
        for(NRSYSINFO_PROFILE in NRSYSINFO_PROFILES) {
            NRSYSINFO_PROFILE_DistName  = NRSYSINFO_PROFILE.getParameter("distName"); // EG: PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRSYSINFO_PROFILE-1
            NRSYSINFO_PROFILE_DN_Index  = NRSYSINFO_PROFILE_DistName.substring(NRSYSINFO_PROFILE_DistName.indexOf("/") + 1)
            current_sNonIntraSearchP    = NRSYSINFO_PROFILE.getParameterAsInteger("sNonIntraSearchP");
            current_threshServingLowP   = NRSYSINFO_PROFILE.getParameterAsInteger("threshServingLowP");
            current_qHyst               = NRSYSINFO_PROFILE.getParameterAsInteger("qHyst");
            logger.info(" INFO: The current distName on this NRSYSINFO_PROFILE is: ${NRSYSINFO_PROFILE_DistName}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN_Index is: ${NRSYSINFO_PROFILE_DN_Index} and the NRSYSINFO_PROFILE_DN under this NRCELL is: ${NRSYSINFO_PROFILE_DN} ");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The FreqBand on this cell is: ${current_freqBand}");
            logger.info(" INFO: current_sNonIntraSearchP is ${current_sNonIntraSearchP} and the needed value is: 2")
            logger.info(" INFO: current_threshServingLowP is ${current_threshServingLowP} and the needed value is: 2");
            logger.info(" INFO: current_qHyst is ${current_qHyst} and the needed value is: 2");


            if(NRSYSINFO_PROFILE_DN_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: DN_INDEX matches the Profile Index ");

                if(current_sNonIntraSearchP != 2) {
                    logger.info(" INFO: Updating sNonIntraSearchP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE sNonIntraSearchP to correct value", "sNonIntraSearchP", 2);
                }

                else {
                    logger.info(" INFO: sNonIntraSearchP is already aligned with recommended value");
                }

                if(current_threshServingLowP != 2) {
                    logger.info(" INFO: Updating the threshServingLowP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE threshServingLowP to correct value", "threshServingLowP", 2)
                }

                else {
                    logger.info(" INFO: threshServingLowP is already aligned with recommended value");
                }

                if(current_qHyst != 2) {
                    logger.info(" INFO: Updating the qHyst");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE qHyst to correct value", "qHyst", 2);
                }

                else {
                    logger.info(" INFO: qHyst already aligned with recommended value");
                }
                logger.info(" INFO: End of evaluation of ${NRSYSINFO_PROFILE_DistName} on NRCELL: ${NRcell} band: ${current_freqBand}");
                logger.info("**********************************************************************************************************************************************************************************************");
            }


            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }
        }
    }
    
    logger.info(" INFO: Inconsistency check for NRSYSINFO_PROFILE Complete");
    logger.info(" INFO: Inconsistency checking now for NRIAFIM");
    logger.info("**********************************************************************************************************************************************************************************************");

    if(NRIAFIMS.size() > 0) {
        for(NRIAFIM in NRIAFIMS) {
            NRIAFIMS_DistName                   = NRIAFIM.getParameter("distName");
            current_qRxLevMin_NRIAFIM           = NRIAFIM.getParameterAsInteger("qRxLevMin");
            current_sIntraSearchP               = NRIAFIM.getParameterAsInteger("sIntraSearchP");
            current_tReselectionNr_NRIAFIM      = NRIAFIM.getParameterAsInteger("tReselectionNr");
            NRIAFIMS_Sys_Index                  = NRIAFIMS_DistName.split("/")[1..3].join("/")
    
            logger.info(" INFO: The currentFreqBand is: ${current_freqBand}");
            logger.info(" INFO: The current distName on this NRIAFIM profile is: ${NRIAFIMS_DistName}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this NRCELL is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The FreqBand on this cell is: ${current_freqBand}");
            logger.info(" INFO: The current NRIAFIMS_Sys_Index is: ${NRIAFIMS_Sys_Index} and the needed value is: ${NRSYSINFO_PROFILE_DN} ");
            logger.info(" INFO: current_qRxLevMin_NRIAFIM is ${current_qRxLevMin_NRIAFIM} and the needed value is: -60");
            logger.info(" INFO: current_sIntraSearchP is ${current_sIntraSearchP} and the needed value is: 31");
            logger.info(" INFO: current_tReselectionNr_NRIAFIM is ${current_tReselectionNr_NRIAFIM} and the needed value is: 1");

            if(NRIAFIMS_Sys_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: NRIAFIMS_Sys_Index equals to NRSYSINFO_PROFILE_DN_Index");


                if(current_qRxLevMin_NRIAFIM != -60) {
                    logger.info(" INFO: Updating qRxLevMin");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE qRxLevMin to correct value", "qRxLevMin", -60);
                }

                else {
                    logger.info(" INFO: qRxLevMin is already aligned with recommended value");
                }

                if(current_sIntraSearchP != 31) {
                    logger.info(" INFO: Updating the sIntraSearchP");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE sIntraSearchP to correct value", "sIntraSearchP", 31)
                }

                else {
                    logger.info(" INFO: sIntraSearchP is already aligned with recommended value");
                }

                if(current_tReselectionNr_NRIAFIM != 1) {
                    logger.info(" INFO: Updating the tReselectionNr");
                    addRecommendation("UPDATE", NRIAFIM, "UPDATE tReselectionNr to correct value", "tReselectionNr", 1);
                }
                
                else {
                    logger.info(" INFO: tReselectionNr is already aligned with recommended value");
                }
                logger.info(" INFO: End of evaluation of ${NRIAFIMS_DistName} on NRCELL: ${NRcell} Band: ${current_freqBand}");
                logger.info("**********************************************************************************************************************************************************************************************");
            }

            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }
        }
    }
    logger.info(" INFO: Inconsistency check for NRIAFIM complete");
    logger.info(" INFO: End of FreqBand ${current_freqBand}");
    logger.info("**********************************************************************************************************************************************************************************************");
}

// This function here is for N78_F1 Bands and will do the inconsistency check for theses values
// Function takes in NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index
// Returns: None
def n78_F1_FreqBand = { NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS ->
    logger.info(" INFO: We are evaluating the cell: ${NRcell}");
    logger.info(" INFO: The freqBand is: N78_F1");
    logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this cell is: ${NRSYSINFO_PROFILE_DN}");
    logger.info(" INFO: The Index_DN_NRCELL on this cell is: ${Index_DN_NRCELL}");
    logger.info(" INFO: The current_qRxLevMin_NRCELL is: ${current_qRxLevMin_NRCELL} and the needed value is: -55");



    if(current_qRxLevMin_NRCELL != -55) {
        logger.info(" INFO: Updating the qRxLevMin");
        addRecommendation("UPDATE", NRCELL, "UPDATE qRxLevMin to correct value", "qRxLevMin", -55)
    }

    else {
        logger.info(" INFO: qRxLevMin is already aligned with recommended value")
    }
    logger.info(" INFO: End of evaluation of NRcell on ${NRcell} on Band: N78_F1");
    logger.info("**********************************************************************************************************************************************************************************************");


    if(NRSYSINFO_PROFILES.size() > 0) {
        for(NRSYSINFO_PROFILE in NRSYSINFO_PROFILES) {
            NRSYSINFO_PROFILE_DistName  = NRSYSINFO_PROFILE.getParameter("distName"); // EG: PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRSYSINFO_PROFILE-1
            NRSYSINFO_PROFILE_DN_Index  = NRSYSINFO_PROFILE_DistName.substring(NRSYSINFO_PROFILE_DistName.indexOf("/") + 1);
            current_sNonIntraSearchP    = NRSYSINFO_PROFILE.getParameterAsInteger("sNonIntraSearchP");
            current_threshServingLowP   = NRSYSINFO_PROFILE.getParameterAsInteger("threshServingLowP");
            current_qHyst               = NRSYSINFO_PROFILE.getParameterAsInteger("qHyst");
            logger.info(" INFO: The current distName on this NRSYSINFO_PROFILE is: ${NRSYSINFO_PROFILE_DistName}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN_Index is: ${NRSYSINFO_PROFILE_DN_Index} and the NRSYSINFO_PROFILE_DN under this NRCELL is: ${NRSYSINFO_PROFILE_DN} ");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The FreqBand on this cell is: N78_F1");
            logger.info(" INFO: current_sNonIntraSearchP is ${current_sNonIntraSearchP} and the needed value is: 3")
            logger.info(" INFO: current_threshServingLowP is ${current_threshServingLowP} and the needed value is: 3");
            logger.info(" INFO: current_qHyst is ${current_qHyst} and the needed value is: 2");

            if(NRSYSINFO_PROFILE_DN_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: The NRSYSINFO_PROFILE_DN Index matches NRSYSINFO_PROFILE_DistName")
                
                if(current_sNonIntraSearchP != 3) {
                    logger.info(" INFO: Updating sNonIntraSearchP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE sNonIntraSearchP to correct value", "sNonIntraSearchP", 3);
                }

                else {
                    logger.info(" INFO: sNonIntraSearchP is already aligned with recommended value");
                }

                if(current_threshServingLowP != 3) {
                    logger.info(" INFO: Updating the threshServingLowP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE threshServingLowP to correct value", "threshServingLowP", 3)
                }

                else {
                    logger.info(" INFO: threshServingLowP is already aligned with recommended value");
                }

                if(current_qHyst != 2) {
                    logger.info(" INFO: Updating the qHyst");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE qHyst to correct value", "qHyst", 2);
                }

                    else {
                    logger.info(" INFO: qHyst is already aligned with recommended value");
                }
                logger.info(" INFO: End of evaluation of ${NRSYSINFO_PROFILE_DistName} on NRCELL: ${NRcell} Band: ${current_freqBand}");
                logger.info("**********************************************************************************************************************************************************************************************");
            }

            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }
        }
    }
        
    logger.info(" INFO: Inconsistency check for NRSYSINFO_PROFILE Complete");
    logger.info(" INFO: Inconsistency checking now for NRIAFIM");
    logger.info("**********************************************************************************************************************************************************************************************");

    if(NRIAFIMS.size() != 0) {
        for(NRIAFIM in NRIAFIMS) {
            NRIAFIMS_DistName                   = NRIAFIM.getParameter("distName");
            current_qRxLevMin_NRIAFIM           = NRIAFIM.getParameterAsInteger("qRxLevMin");
            current_sIntraSearchP               = NRIAFIM.getParameterAsInteger("sIntraSearchP");
            current_tReselectionNr_NRIAFIM      = NRIAFIM.getParameterAsInteger("tReselectionNr");
            NRIAFIMS_Sys_Index                  = NRIAFIMS_DistName.split("/")[1..3].join("/")

            logger.info(" INFO: The currentFreqBand is: N78_F1");
            logger.info(" INFO: The current distName on this NRIAFIM profile is: ${NRIAFIMS_DistName}");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this NRCELL is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: The current NRIAFIMS_Sys_Index is: ${NRIAFIMS_Sys_Index} and the needed value is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: current_qRxLevMin_NRIAFIM is ${current_qRxLevMin_NRIAFIM} and the needed value is: -55");
            logger.info(" INFO: current_sIntraSearchP is ${current_sIntraSearchP} and the needed value is: 26");
            logger.info(" INFO: current_tReselectionNr_NRIAFIM is ${current_tReselectionNr_NRIAFIM} and the needed value is: 1");
        

            if(NRIAFIMS_Sys_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: NRIAFIMS_Sys_Index equals to NRSYSINFO_PROFILE_DN_Index")
        
                if(current_qRxLevMin_NRIAFIM != -55) {
                    logger.info(" INFO: Updating qRxLevMin");
                    addRecommendation("UPDATE", NRIAFIMS, "UPDATE qRxLevMin to correct value", "qRxLevMin", -55);
                }

                if(current_sIntraSearchP != 26) {
                    logger.info(" INFO: Updating the sIntraSearchP");
                    addRecommendation("UPDATE", NRIAFIMS, "UPDATE sIntraSearchP to correct value", "sIntraSearchP", 26)
                }

                if(current_tReselectionNr_NRIAFIM != 1) {
                    logger.info(" INFO: Updating the tReselectionNr");
                    addRecommendation("UPDATE", NRIAFIMS, "UPDATE tReselectionNr to correct value", "tReselectionNr", 1);
                }
                logger.info(" INFO: End of evaluation of ${NRIAFIMS_DistName} on NRCELL: ${NRcell} Band: ${current_freqBand}");
                logger.info("**********************************************************************************************************************************************************************************************");
        
            }

            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }

        }
    }

    logger.info(" INFO: Inconsistency check for NRIAFIM complete");
    logger.info(" INFO: End of FreqBand N78_F1");
    logger.info("**********************************************************************************************************************************************************************************************");
}
// This function here is for N78_F2 Bands and will do the inconsistency check for theses values
// Function takes in NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index
// Returns: None
def n78_F2_FreqBand = { NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS ->
    logger.info(" INFO: We are evaluating the cell: ${NRcell}");
    logger.info(" INFO: The freqBand is: N78_F2");
    logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this cell is: ${NRSYSINFO_PROFILE_DN}");
    logger.info(" INFO: The Index_DN_NRCELL on this cell is: ${Index_DN_NRCELL}");
    logger.info(" INFO: The current_qRxLevMin_NRCELL is: ${current_qRxLevMin_NRCELL} and the needed value is: -55");
    

    if(current_qRxLevMin_NRCELL != -55) {
        logger.info(" INFO: Updating the qRxLevMin");
        addRecommendation("UPDATE", NRCELL, "UPDATE qRxLevMin to correct value", "qRxLevMin", -55)
    }

    else {
        logger.info(" INFO: qRXLevMin aligned")
    }
    logger.info(" INFO: End of evaluation of ${NRcell} on FreqBand: N78_F2");
    logger.info("**********************************************************************************************************************************************************************************************");


    if(NRSYSINFO_PROFILES.size() > 0) {
        for(NRSYSINFO_PROFILE in NRSYSINFO_PROFILES) {
            NRSYSINFO_PROFILE_DistName  = NRSYSINFO_PROFILE.getParameter("distName"); // EG: PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRSYSINFO_PROFILE-1
            NRSYSINFO_PROFILE_DN_Index  = NRSYSINFO_PROFILE_DistName.substring(NRSYSINFO_PROFILE_DistName.indexOf("/") + 1);
            current_sNonIntraSearchP    = NRSYSINFO_PROFILE.getParameterAsInteger("sNonIntraSearchP");
            current_threshServingLowP   = NRSYSINFO_PROFILE.getParameterAsInteger("threshServingLowP");
            current_qHyst               = NRSYSINFO_PROFILE.getParameterAsInteger("qHyst");
            logger.info(" INFO: The current distName on this NRSYSINFO_PROFILE is: ${NRSYSINFO_PROFILE_DistName}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN_Index on this cell is ${NRSYSINFO_PROFILE_DN_Index} and the NRSYSINFO_PROFILE_DN on this NRCELL is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The FreqBand on this cell is: N78_F2");
            logger.info(" INFO: current_sNonIntraSearchP is ${current_sNonIntraSearchP} and the needed value is: 2")
            logger.info(" INFO: current_threshServingLowP is ${current_threshServingLowP} and the needed value is: 2");
            logger.info(" INFO: current_qHyst is ${current_qHyst} and the needed value is: 2");

            if(NRSYSINFO_PROFILE_DN_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: The NRSYSINFO_PROFILE_DN is equal to NRSYSINFO_PROFILE_DistName cell ");

                if(current_sNonIntraSearchP != 3) {
                    logger.info(" INFO: Updating sNonIntraSearchP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE sNonIntraSearchP to correct value", "sNonIntraSearchP", 3);
                }

                if(current_threshServingLowP != 3) {
                    logger.info(" INFO: Updating the threshServingLowP");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE threshServingLowP to correct value", "threshServingLowP", 3)
                }

                if(current_qHyst != 2) {
                    logger.info(" INFO: Updating the qHyst");
                    addRecommendation("UPDATE", NRSYSINFO_PROFILE, "UPDATE qHyst to correct value", "qHyst", 2);
                }
                logger.info(" INFO: End of evaluation of ${NRSYSINFO_PROFILE_DistName} on ${NRcell} freqBand: N78_F2")
                logger.info("**********************************************************************************************************************************************************************************************");
            }

            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }
        }
    }
        

    logger.info(" INFO: Inconsistency check for NRSYSINFO_PROFILE Complete");
    logger.info(" INFO: Inconsistency checking now for NRIAFIM");

    if(NRIAFIMS.size() != 0) {
        for(NRIAFIM in NRIAFIMS) {
            NRIAFIMS_DistName                   = NRIAFIM.getParameter("distName");
            current_qRxLevMin_NRIAFIM           = NRIAFIM.getParameterAsInteger("qRxLevMin");
            current_sIntraSearchP               = NRIAFIM.getParameterAsInteger("sIntraSearchP");
            current_tReselectionNr_NRIAFIM      = NRIAFIM.getParameterAsInteger("tReselectionNr");
            NRIAFIMS_Sys_Index                  = NRIAFIMS_DistName.split("/")[1..3].join("/")

            logger.info(" INFO: Evaluating NRIAFIM")
            logger.info(" INFO: The currentFreqBand is: ${current_freqBand}");
            logger.info(" INFO: The current distName on this NRIAFIM profile is: ${NRIAFIMS_DistName}");
            logger.info(" INFO: The NRCELL Disname is: ${NRcell}");
            logger.info(" INFO: The NRSYSINFO_PROFILE_DN on this NRCELL is: ${NRSYSINFO_PROFILE_DN}");
            logger.info(" INFO: The FreqBand on this cell is: N78_F2");
            logger.info(" INFO: The current NRIAFIMS_Sys_Index is: ${NRIAFIMS_Sys_Index} and the needed value is: ${NRSYSINFO_PROFILE_DN} ");
            logger.info(" INFO: current_qRxLevMin_NRIAFIM is ${current_qRxLevMin_NRIAFIM} and the needed value is: -55");
            logger.info(" INFO: current_sIntraSearchP is ${current_sIntraSearchP} and the needed value is: 26");
            logger.info(" INFO: current_tReselectionNr_NRIAFIM is ${current_tReselectionNr_NRIAFIM} and the needed value is: 1");
            logger.info("**********************************************************************************************************************************************************************************************");

                

            if(NRIAFIMS_Sys_Index == NRSYSINFO_PROFILE_DN) {
                logger.info(" INFO: NRIAFIMS_Sys_Index equals to NRSYSINFO_PROFILE_DN_Index")
    
                if(current_qRxLevMin_NRIAFIM != -55) {
                    logger.info(" INFO: Updating qRxLevMin");
                    addRecommendation("UPDATE", NRIAFIMS, "UPDATE qRxLevMin to correct value", "qRxLevMin", -55);
                }

                else {
                    logger.info(" INFO: qRxLevMin aligned with recommended value");
                }

                if(current_sIntraSearchP != 26) {
                    logger.info(" INFO: Updating the sIntraSearchP");
                    addRecommendation("UPDATE", NRIAFIMS, "UPDATE sIntraSearchP to correct value", "sIntraSearchP", 26)
                }

                else {
                    logger.info(" INFO: sIntraSearchP aligned with recommended value");
                }

                if(current_tReselectionNr_NRIAFIM != 1) {
                    logger.info(" INFO: Updating the tReselectionNr");
                    addRecommendation("UPDATE", NRIAFIMS, "UPDATE tReselectionNr to correct value", "tReselectionNr", 1);
                }

                else {
                    logger.info(" INFO: tReselectionNr aligned with recommended value");
                }
                logger.info(" INFO: End of evaluation of ${NRIAFIMS_DistName} on NRCELL: ${NRcell} Band: ${current_freqBand}");
                logger.info("**********************************************************************************************************************************************************************************************");
                
            }

            else {
                logger.info(" INFO: This cell does contain" + NRSYSINFO_PROFILE_DN);
                logger.info("**********************************************************************************************************************************************************************************************");
            }
        }
    }
            
    logger.info(" INFO: Inconsistency check for NRIAFIM complete");
    logger.info(" INFO: End of FreqBand N78_F2");
    logger.info("**********************************************************************************************************************************************************************************************");
}
// This function here is to determine which N78 Frequency band is on this cell (N78_F1 or N78_F2)
// Function takes in cellTechnology, NRCELL, cellReferenceTable_gNBID_lcrId, cellReferenceTable_freqBandIndicatorNR, cellReferenceTable_ssbFrequency, UNKNOWN_SSBFREQ, freqBandN78
// Returns: freqBandN78
def freqBandIndicator { cellTechnology, NRCELL, cellReferenceTable_gNBID_lcrId, cellReferenceTable_freqBandIndicatorNR, cellReferenceTable_ssbFrequency, UNKNOWN_SSBFREQ, freqBandN78 ->
    logger.info(" INFO: The currentFreqBand on this cell is N78");
                    
    //This section of code is from N_5G CA MANAGE and determines the if the Band if F1 or F2
    // Written by Sam
    if(cellTechnology == "FDD") {
        nrarfcn_integer = NRCELL.getMos("NRCELL_FDD").getParameterAsInteger("nrarfcnDl");
        bw_integer      = NRCELL.getMos("NRCELL_FDD").getParameterAsInteger("chBwDl");
    } 


    
    else {
        nrarfcn_integer = NRCELL.getParameterAsInteger("nrarfcn");
        bw_integer      = NRCELL.getParameterAsInteger("chBw");
    }

    
    gscn_integer = NRCELL.getParameterAsInteger("gscnOrSsPbchArfcn");
    arfcnSsbPbch = NRCELL.getParameter("arfcnSsbPbch");
    logger.info("We are analysing the NRCELL cellName : " + NRCELL.getParameter("cellName") + " with the arfcnSsbPbch : " + arfcnSsbPbch + " with the gscn_integer : " + gscn_integer +  " with the nrarfcn_integer : " + nrarfcn_integer);
    if( (gscn_integer != null && gscn_integer > 2)      ||
    (arfcnSsbPbch != null && arfcnSsbPbch != "-1")  ){
        Integer ssbfreq_integer = 0;
        if(arfcnSsbPbch == null || arfcnSsbPbch == "-1") {
            if(nrarfcn_integer >= 600000) {
                deltafglobal = 15;
                frefoff = 3000000;
                nrefoff = 600000;
                n = gscn_integer - 7499;
                ssbref = 1000 * (3000 + n * 1.44);
            }


            else {
                deltafglobal = 5;
                frefoff = 0;
                nrefoff = 0;
                m_mapping = [3, 1, 5];
                m = m_mapping[nrarfcn_integer % m_mapping.size()];
                n = ((gscn_integer - ((m - 3)/2)) / 3);
                ssbref = (n * 1200) + (m * 50);
            } 
            ssbfreq_integer = (((ssbref-frefoff)/deltafglobal)+nrefoff);
            logger.info("SSBFREQ with formula : ssbfreq_integer = " + ssbfreq_integer + " (gscn_integer = " + gscn_integer + " and n = " + n + " and ssbref = " + ssbref + ")");
        } 
                
        else {
            ssbfreq_integer = Integer.parseInt(arfcnSsbPbch);
            logger.info("SSBFREQ with parameter : ssbfreq_integer = " + arfcnSsbPbch);
        }
        cellReferenceTable_freqBandIndicatorNR      .add(NRCELL.getParameter("freqBandIndicatorNR"));
        cellReferenceTable_ssbFrequency             .add(ssbfreq_integer.toString());
        cellReferenceTable_gNBID_lcrId              .add(NRBTS.getParameter("originalId")+"_"+NRCELL.getParameter("lcrId"));
                                                    
                                                
        // ? Build list of unique N78 ssbFreq
        if(NRCELL.getParameter("freqBandIndicatorNR") == "78") {
            if(!N78_ssbFreq_list.contains(ssbfreq_integer)) {
                N78_ssbFreq_list.add(ssbfreq_integer);
                N78_bw_list.add(bw_integer);
            }
        }
    } 
                        
    else {
        logger.info("ERROR : This NRCELL we can't query the SSBFREQ: " + NRCELL.getParameter("distName").substring(10));
        UNKNOWN_SSBFREQ = true;
        cellwithunknownSSBFreq.add(NRCELL.getParameter("distName").substring(10));
    }


    if(!UNKNOWN_SSBFREQ) {
        logger.info(" ************* BEFORE : cellReferenceTable_freqBandIndicatorNR : " + cellReferenceTable_freqBandIndicatorNR);
        if(N78_ssbFreq_list.size() != 0) {
            if(N78_ssbFreq_list.size() == 1) {       
                // ? If only one N78 carrier
                
                N78_F1_ssbFreq = N78_ssbFreq_list[0].toString();
            } 
                    
            else {
                if(N78_bw_list[0] > N78_bw_list[1]) { 
                    // ? If the first carrier has the highest BW
                    N78_F1_ssbFreq = N78_ssbFreq_list[0].toString();
                } 
                else if(N78_bw_list[1] > N78_bw_list[0]) {      
                    // ? If the second carrier has the highest BW
                    N78_F1_ssbFreq = N78_ssbFreq_list[1].toString();
                } 
                        
                else {
                    if(N78_ssbFreq_list[0] < N78_ssbFreq_list[1]) { 
                        // ? If the first carrier has the lowest ssbFreq
                        N78_F1_ssbFreq = N78_ssbFreq_list[0].toString();
                    } 
                            
                    else {
                        // ? If the second carrier has the lowest ssbFreq
                        N78_F1_ssbFreq = N78_ssbFreq_list[1].toString();
                    }
                    
                }   
            }

            logger.info(" *** N78_ssbFreq_list is : " + N78_ssbFreq_list);
            logger.info(" *** N78_bw_list is : " + N78_bw_list);
            logger.info(" *** N78_F1_ssbFreq is : " + N78_F1_ssbFreq);

            for(i = 0 ; i < cellReferenceTable_gNBID_lcrId .size(); i++) {
                if(cellReferenceTable_freqBandIndicatorNR[i] == "78") {
                        //Freq = N78_F1
                    if(cellReferenceTable_ssbFrequency[i] == N78_F1_ssbFreq) {
                        cellReferenceTable_freqBandIndicatorNR[i] = "78-F1";
                        freqBandN78 = "N78_F1";
                        logger.info(" INFO: The current freqBand is: ${freqBandN78}")
                    } 

                    //Freq = N78_F2
                    else {
                        cellReferenceTable_freqBandIndicatorNR[i] = "78-F2";
                        freqBandN78 = "N78_F2";
                        logger.info(" INFO: The current freqBand is: ${freqBandN78}")
                    }   
                }
            }
        }
    } 
    logger.info(" ************* AFTER : cellReferenceTable_freqBandIndicatorNR : " + cellReferenceTable_freqBandIndicatorNR);
    return freqBandN78

}

//Main 
for(MRBTS in _context.getMos("MRBTS")){
    number_of_nrtbs++;

    //Define MO
    def NRBTS_S                     = MRBTS.getMos("NRBTS");
    def NRCELLS                     = NRBTS_S.getMos("NRCELL");

    //Prevention of blackList Sites will be removed once cloudRan is completed
    def okayToStart = true;

    //Information of Site
    def mrbtsname                                     = MRBTS.getParameter("name");
	def gNodeBID                                      = MRBTS.getParameterAsInteger("originalId");
	def nrbtsname                                     = NRBTS_S.getParameter("name");
	def siteID                                        = NRCELLS.getPhysicalParameter("SITEID");
	def site_region                                   = NRCELLS.getPhysicalParameter("TERRITORY");
	def site_zone                                     = NRCELLS.getPhysicalParameter("ZONE");


    //Site information
    logger.info("**********************************************************************************************************************************************************************************************");
	logger.info("---> INFO: Here is the Evaluated mrbtsname: '"+mrbtsname +" and the gNodeBID: "+gNodeBID + " and the site_region, site_zone and nrbtsname: "+site_region + " ; "+ site_zone+ " ; "+nrbtsname);
	logger.info("**********************************************************************************************************************************************************************************************");
    
    if(blackListSites.contains(gNodeBID)) {
        logger.info(" INFO: the NRSYSINFO_PROFILE uids is empty on this site")
        okayToStart = false;
    }

    else {
        logger.info(" INFO: This gNodeBID is not contained within the blackListSites");
    }

    //Conditions needed for N78 Band Indicator Function
    def N78_ssbFreq_list                                    = [];
    def N78_bw_list                                         = [];
    def cellReferenceTable_freqBandIndicatorNR              = [];
    def cellReferenceTable_ssbFrequency                     = [];
    def cellReferenceTable_gNBID_lcrId                      = [];
    def UNKNOWN_SSBFREQ                                     = false;

    //Declaration of Variables here
    def NRSYSINFO_PROFILE_DistName;
    def NRIAFIMS_DistName;
    def freqBandN78;
    def bandType;

    //Index values will be removed from distName (OrginalId from NRSYSINFO_PROFILE)
    def NRIAFIMS_Sys_Index; 
    def NRSYSINFO_PROFILE_DN_Index;
    def Index_DN_NRCELL;
    def NRSYSINFO_PROFILE_DN; 

    //NRCELL Values
    def current_qRxLevMin_NRCELL;
    def current_freqBand;
    def NRcell;
    def cellTechnology;



    //Declaration of Current Values for NRIAFIM
    def current_sNonIntraSearchP;
    def current_threshServingLowP;
    def current_qHyst;
    def current_qRxLevMin_NRIAFIM;
    def current_sIntraSearchP;
    def current_tReselectionNr_NRIAFIM;


    if(okayToStart) {
        def NRSYSINFO_PROFILES          = NRBTS_S.getMos("NRSYSINFO_PROFILE");
        def NRIAFIMS                    = NRSYSINFO_PROFILES.getMos("NRIAFIM");
        for(NRBTS in NRBTS_S){
            for(NRCELL in NRCELLS) {

                //NRCELL INFORMATION
                NRcell                              = NRCELL.getParameter("distName");
                cellTechnology                      = NRCELL.getParameter("cellTechnology");
                current_freqBand                    = NRCELL.getParameter("freqBandIndicatorNR");
                current_qRxLevMin_NRCELL            = NRCELL.getParameterAsInteger("qRxLevMin");
                NRSYSINFO_PROFILE_DN                = NRCELL.getParameter("nrSysInfoProfileDN"); // EG: MRBTS-1549769/NRBTS-1549769/NRSYSINFO_PROFILE-1
                Index_DN_NRCELL                     = NRSYSINFO_PROFILE_DN.split("/")[2].replace("NRSYSINFO_PROFILE-", "");; // Index NRSYSINFO_PROFILE-1 -> 1
                
                //Error preventing
                if(current_freqBand != null) {
                    logger.info(" INFO: We will now evaluated the different bands on update parameters according to the Band");
                    logger.info("**********************************************************************************************************************************************************************************************");

                    //FreqBand: N5
                    if(current_freqBand == "5") {
                        n5_FreqBand(NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS)
                    } //End FreqBand 5
                    
                    //FreqBand: N25
                    else if(current_freqBand == "25") {
                      n25_FreqBand(NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS)
                    } 
                        
                    //FreqBand N66
                    else if(current_freqBand == "66") {
                        n66_FreqBand(NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS)
                       
                    } 

                    //FreqBand N71
                    else if(current_freqBand == "71") {
                        n71_FreqBand(NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS);
                        
                    } 
                    
                    //FreqBand N78_F1 + N_78_F2
                    else if(current_freqBand == "78") {
                        //BandType Function
                        bandType = freqBandIndicator(cellTechnology, NRCELL, cellReferenceTable_gNBID_lcrId, cellReferenceTable_freqBandIndicatorNR, cellReferenceTable_ssbFrequency, freqBandN78);

                        switch(bandType) {
                            Case "N78_F1":
                                //BandType is N78_F1
                                n78_F1_FreqBand(NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS);
                                break;
                            Case "N78_F2":
                                //BandType is N78_F1
                                n78_F2_FreqBand(NRcell, current_freqBand, NRSYSINFO_PROFILE_DN, Index_DN_NRCELL, current_qRxLevMin_NRCELL, NRSYSINFO_PROFILE_DistName, NRSYSINFO_PROFILE_DN_Index, current_sNonIntraSearchP, current_threshServingLowP, current_qHyst, current_qRxLevMin_NRIAFIM, NRIAFIMS_DistName, current_sIntraSearchP, current_tReselectionNr_NRIAFIM, NRIAFIMS_Sys_Index, NRSYSINFO_PROFILES, NRIAFIMS);
                                break; 
                        }
                    }

                    //FreqBand No Listed
                    else {
                        logger.info(" INFO: Current_FreqBand: ${current_freqBand} on this cell: ${NRcell} is not listed for requirements, no updates we will made");
                        logger.info("**********************************************************************************************************************************************************************************************");
                    }
                }

                //FreqBand NULL
                else {
                    logger.info(" INFO: Current_FreqBand on this cell is null");
                    logger.info("**********************************************************************************************************************************************************************************************");
                }
                logger.info(" INFO: End of inconsistency checks on this NRcell:  ${NRcell}");
                logger.info("**********************************************************************************************************************************************************************************************");
            } // End of NRCELL
        } // End of NRBTS
    } // End of conditionStart

    else {
        logger.info(" INFO: This CPR will not run due the start flag being set to false");
        logger.info("**********************************************************************************************************************************************************************************************");
    }
} // End of MRBTS
logger.info(" ************** Total number of evaluated objects: NRBTS " + number_of_nrtbs);
logger.info(" ************** End of " + cpr_name);
