/*
CB007334 DFA Requirements
VERSION #1
Developed by: Shannon Abeda
Date: April 5th, 2023
URL: https://confluence.bell.corp.bce.ca/pages/viewpage.action?spaceKey=NRD&title=CB007334+ANR+for+EN+DC%3A+Resolution+of+unknown+cells

With the CB007334: ANR for EN-DC: Resolution of Unknown Cells feature, the eNB can automatically create, 
update and remove neighbor relations towards NR cells, specifically MOs LNADJGNB and LNRELGNBCELL based on the ReportCGI measurement results from UEs.

The LNADJGNB objects that are created are incomplete, i.e. they are missing the C-Plane IP address. 
With the activation of CB007333 and CB007225 together, the C-Plane IP address is automatically updated with the SON Information Transfer procedure, 
thereby completing the LNADJGNB MO datafill. The UE capability that is required for this feature is the IE, 'reportCGI-NR-NoEN-DC-r15' which requires no EN-DC setup.

The feature also removes unused NBR relations and the removal of the last 
LNRELGNBCELL managed object related with the enbControlled LNADJGNB triggers automatic removal of this object. 
All configuration changes performed on the related LNADJGNB managed object during its removal are rejected by the eNB.

Parameters that are being changed:
-actDynTrigLteNrDualConnectivity
-actUeBasedAnrNr
-drxProfile105
    -drxinactivityT
    -drxLongCycle
    -drxOnDuratT
    -drxProfilePriority
    -drxRetransT
-actAutoNrNeighRemoval
-anrNrInactivityTimer
-anrNrCoverageConfirmTimer
-anrNrNbrRelRemGuardTime
-anrNrMeasConfList
    -paramMeasConfId
    -thresholdNrRsrp
    -thresholdNrRsrq
-anrNrParamList
    -anrDefRelDcAllowed
    -carrierFreqNrCell
    -frqNrCarSelect
    -gNBIdLength
    -paramMeasConfId
    -paramPlmnId
-anrNrParamPlmnList
    -mcc
    -mnc
    -mnclength
    -paramplmnid
-nrPciNbrRelRemThresh
-nrPciNbrRelAddThresh
-nrPciNbrRelAddHyst
*/


cpr_name = "ANR_EN_DC_UPDATE";
logger.info(" **** Beginning of " + cpr_name);
def number_of_lnbts = 0;

for(lnbts in _context.getMos("LNBTS")) {
    number_of_lnbts++;
    def mrbts = _context.getMos("MRBTS");
    def lncels = lnbts.getMos("LNCEL");
    def anrs = lnbts.getMos("ANR");
    def nrdcdprs = lnbts.getMos("NRDCDPR");

    def mnls = mrbts.getMos("MNL");
    def mnlents = mnls.getMos("MNLENT");
    def featcadms = mnlents.getMos("FEATCADM");
    def featladms = featcadms.getMos("FEATLADM");
    
    def nrdcprs = lnbts.getMos("NRDCPR");
    def lnbtsDistname = lnbts.getParameter("distName")
    def siteID = lncels.getPhysicalParameter("SITEID")
    def current_actUeBasedAnrNr = lnbts.getParameterAsInteger("actUeBasedAnrNr");
    def needed_actUeBasedAnrNr = 1
    def current_actDynTrigLteNrDualConnectivity = lnbts.getParameterAsInteger("actDynTrigLteNrDualConnectivity");
    def needed_actDynTrigLteNrDualConnectivity = 1;
    def actUeBasedAnrNr_Conditon_Flag = false; // LNBTS.actDynTrigLteNrDualConnectivity = true
    def lncel_inactivityTimer = lncels.getParameterAsInteger("inactivityTimer");
    def actUeBasedAnrNr_Updated = false;
    def current_actLteNrDualConnectivity = lnbts.getParameterAsInteger("actLteNrDualConnectivity");
    def needed_actLteNrDualConnectivity = 1;
    def current_actDynamicFirewallForS1X2 = featladms.getParameterAsInteger("actDynamicFirewallForS1X2");
    def needed_actDynamicFirewallForS1X2 = 0;
    def current_actEndcAutoX2Setup = lnbts.getParameterAsInteger("actEndcAutoX2Setup")
    def needed_actEndcAutoX2Setup = 1;
    def actEndcAutoX2Setup_Flag = false;

    logger.info(" ************** We are evaluating this eNB: " + lnbtsDistname + " with siteID : " + siteID);
    

    logger.info("**********************************************************************************************************************************************************************************************");
    logger.info("---> INFO: 1A We now check the current actUeBasedAnrNr values matches the needed value");
    logger.info("---> INFO: The current actUeBasedAnrNr on this cell is: " + current_actUeBasedAnrNr);
    logger.info("---> INFO: The needed actUeBasedAnrNr on this cell is: " + needed_actUeBasedAnrNr);
    logger.info("---> INFO: The actDynTrigLteNrDualConnectivity must be set to true in order for this parameter to be update and current it is set to: " +  current_actDynTrigLteNrDualConnectivity);

    if(current_actDynTrigLteNrDualConnectivity == needed_actDynTrigLteNrDualConnectivity) {
        logger.info("---> INFO: The actDynTrigLteNrDualConnectivity is set to " +  current_actDynTrigLteNrDualConnectivity + " and we can update the  actUeBasedAnrNr");
        actUeBasedAnrNr_Conditon_Flag = true;
        logger.info("---> INFO: The actUeBasedAnrNr_Conditon_Flag is now set to " +  actUeBasedAnrNr_Conditon_Flag);
    }

    if(actUeBasedAnrNr_Conditon_Flag) {
        if(current_actUeBasedAnrNr != needed_actUeBasedAnrNr) {
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO: 1B We now check the current actUeBasedAnrNr values  the needed values in the static table");
            addRecommendation("UPDATE", lnbts, "Changing current_actUeBasedAnrNr from: " + current_actUeBasedAnrNr + " to " + needed_actUeBasedAnrNr, "actUeBasedAnrNr", needed_actUeBasedAnrNr);
            logger.info("---> INFO: Update is now complete");
            actUeBasedAnrNr_Updated = true;
            logger.info("---> INFO: The actUeBasedAnrNr_Updated is now set to " +  actUeBasedAnrNr_Updated);
        }
    
        else {
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO: 1C actUeBasedAnrNr on this cell is equal to the needed value and does not needed updating");
    
        }
    
    }

    else {
        logger.info("**********************************************************************************************************************************************************************************************");
        logger.info("---> INFO: 1D actUeBasedAnrNr on this cell does meet the condition required for updating");

    }

    logger.info("**********************************************************************************************************************************************************************************************");
    logger.info("---> INFO: 1E Now we will check actEndcAutoX2Setup");
    logger.info("---> INFO: In order for the actEndcAutoX2Setup to be updated it must meet the condition where the LNBTS.actLteNrDualConnectivity = TRUE & FEATLADM.actDynamicFirewallForS1X2 = FALSE ");
    logger.info("---> INFO: The  LNBTS.actLteNrDualConnectivity is: " + current_actLteNrDualConnectivity + " and the needed value is: " + needed_actLteNrDualConnectivity);
    logger.info("---> INFO: The FEATLADM.actDynamicFirewallForS1X2 is: " + current_actDynamicFirewallForS1X2 + " and the needed value is: " + needed_actDynamicFirewallForS1X2);

    if((current_actLteNrDualConnectivity == needed_actLteNrDualConnectivity) && (current_actDynamicFirewallForS1X2 == needed_actDynamicFirewallForS1X2)) {
        logger.info("**********************************************************************************************************************************************************************************************");
        logger.info("---> INFO: 1F The actEndcAutoX2Setup passes the condition needed for updating");

        if(current_actEndcAutoX2Setup != needed_actEndcAutoX2Setup) {
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO: 1G The actEndcAutoX2Setup is not equal to the needed value");
            addRecommendation("UPDATE", lnbts, "Updating the actEndcAutoX2Setup from: " + current_actEndcAutoX2Setup + " to: " + needed_actEndcAutoX2Setup, "actEndcAutoX2Setup", needed_actEndcAutoX2Setup);
            actEndcAutoX2Setup_Flag = true;
        }

        else {
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO: 1E The actEndcAutoX2Setup is equal to the needed value");
        }
    }

    else {
        logger.info("**********************************************************************************************************************************************************************************************");
        logger.info("---> INFO: 1F The actEndcAutoX2Setup does NOT pass the update condition");
    }

   
    logger.info("**********************************************************************************************************************************************************************************************");
    logger.info("---> INFO: Updates on LNBTS MO parameters are now complete");
    logger.info("---> INFO: 2A We now check the LNCEL parameters");
    logger.info("---> INFO: In order for this cell to have updates completed for all the remaining features, the actUeBasedAnrNr needs to be set to true");
    logger.info("---> INFO: Currently, the condition flag for the actUeBasedAnrNr on this cell is set to: " + actUeBasedAnrNr_Updated);

    if(actUeBasedAnrNr_Updated) {

    for(lncel in lncels) {
        logger.info("**********************************************************************************************************************************************************************************************");
        logger.info("---> INFO 2B: We will now check the drProfile 105 and see if they're any discrepencies with the parameters within the structure");

        def current_drxProfile105 = lncel.getListParameter("drxProfile105");
        def current_size = current_drxProfile105.size();
        def needed_drxProfile105 = createList();
        needed_drxProfile105.addEntry([
            "drxInactivityT": "20",
            "drxLongCycle": "320",
            "drxOnDuratT": "10",
            "drxProfileIndex": "105",
            "drxProfilePriority": "30",
            "drxRetransT": "16"
        ])

        def needed_drxInactivityT = 20;
        def needed_drxLongCycle = 320;
        def needed_drxOnDuratT = 10;
        def needed_drxProfileIndex = 105;
        def needed_drxProfilePriority = 30;
        def needed_drxRetransT = 16;
        def needed_size = needed_drxProfile105.size();

        
        if(current_drxProfile105.size() == 0) {
            logger.info("INFO: ---> The DRXProfile on this cell does not exist and we will now create the drxProfile105");
            addRecommendation("UPDATE", lncel, "Creating the drxProfile 105 as it does not exist on this cell", "drxProfile105", needed_drxProfile105);
            logger.info("INFO: ---> The DRXProfile has now been created");
        }

        else {

            def current_drxInactivityT = current_drxProfile105[0]["drxInactivityT"];
            def current_drxLongCycle = current_drxProfile105[0]["drxLongCycle"];
            def current_drxOnDuratT = current_drxProfile105[0]["drxOnDuratT"];
            def current_drxProfileIndex = current_drxProfile105[0]["drxProfileIndex"];
            def current_drxProfilePriority = current_drxProfile105[0]["drxProfilePriority"];
            def current_drxRetransT = current_drxProfile105[0]["drxRetransT"];

            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO 2C: These are the current & needed values in the DRX Profile");
            logger.info("current_drxInactivityT: " + current_drxInactivityT + " needed_drxInactivityT: " + needed_drxInactivityT);
            logger.info("current_drxLongCyle: " + current_drxLongCycle + " needed_drxLongCycle: " + needed_drxLongCycle);
            logger.info("current_drxOnDuratT: " + current_drxOnDuratT + " needed_drxOnDuratT: " + needed_drxOnDuratT);
            logger.info("current_drxProfileIndex: " + current_drxProfileIndex + " needed_drxProfileIndex: " + needed_drxProfileIndex);
            logger.info("current_drxProfilePriority: " + current_drxProfilePriority + " needed_drxProfilePriority: " + needed_drxProfilePriority);
            logger.info("current_drxRetransT: " + current_drxRetransT + " needed_drxRetransT: " + needed_drxRetransT);
            logger.info("current_size: " + current_size + " needed_size: " + needed_size);


         if(current_size != needed_size ||
        current_drxInactivityT != needed_drxInactivityT ||
        current_drxLongCycle != needed_drxLongCycle ||
        current_drxOnDuratT != needed_drxOnDuratT ||
        current_drxProfileIndex != needed_drxProfileIndex ||
        current_drxProfilePriority != needed_drxProfilePriority ||
        current_drxRetransT != needed_drxRetransT) {
        
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO 2D: The values in the DRXProfile are not aligned and need to be updated");
            addRecommendation("UPDATE", lncel, "Updating the current DrxProfile105 from: " + current_drxProfile105[0] + " to: " + needed_drxProfile105[0], "drxProfile105", needed_drxProfile105);
            logger.info("---> INFO: Update is now complete");
        }

        else {
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO 2E: The values in the DRXProfile are aligned no updates are needed");
        }
        }


    } 
        logger.info("**********************************************************************************************************************************************************************************************");
        logger.info("---> INFO: Updates on LNCEL MO parameters are now complete");
        logger.info("---> INFO 3: Now we will check the parameters in the ANR Mo to see if they reflected the needed values");

        for(anr in anrs) {
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO 3A: We will now check the anrNrMeasConfList list to see if the parameters are aligned with the needed values");
            
            def current_anrNrMeasConfList = anr.getListParameter("anrNrMeasConfList");
            def needed_anrNrMeasConfList = createList();
            needed_anrNrMeasConfList.addEntry([
                "paramMeasConfId": "1",
                "thresholdNrRsrp": "-115",
                "thresholdNrRsrq": "null"
            ])


            def needed_anrNrMeasConfList_paramMeasConfId = 1;
            def needed_thresholdNrRsrp = -115;
            def needed_thresholdNrRsrq = "null";
            def needed_size = needed_anrNrMeasConfList.size();

            if(current_anrNrMeasConfList.size() == 0 ) {
                logger.info("---> INFO: the anrNrMeasConflist does not exist on this cell and we will create it");
                addRecommendation("UPDATE", anr, "Creating the anrNrMeasConflist", "anrNrMeasConfList", needed_anrNrMeasConfList);
                logger.info("---> Update is now complete");
            }

            else {
                def current_anrNrMeasConfList_paramMeasConfId = current_anrNrMeasConfList[0]["paramMeasConfId"];
                def current_thresholdNrRsrp = current_anrNrMeasConfList[0]["thresholdNrRsrp"];
                def current_thresholdNrRsrq = current_anrNrMeasConfList[0]["thresholdNrRsrq"];
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 3B: These are the current & needed anrNrMeasConfList list values");
                logger.info("current_size is: " + current_size + " and the needed size is: " + needed_size);
                logger.info("current_anrNrMeasConfList_paramMeasConfId is: " + current_anrNrMeasConfList_paramMeasConfId + " and the needed value is: " + needed_anrNrMeasConfList_paramMeasConfId);
                logger.info("current_thresholdNrRsrp is: " + current_thresholdNrRsrp + " and the needed value is: " + needed_thresholdNrRsrp);
                logger.info("current_thresholdNrRsrq is: " + current_thresholdNrRsrq + " and the needed value is: " + needed_thresholdNrRsrq);
    
                
    
                if(current_anrNrMeasConfList.size() != needed_size ||
                current_anrNrMeasConfList_paramMeasConfId != needed_anrNrMeasConfList_paramMeasConfId ||
                current_thresholdNrRsrp != needed_thresholdNrRsrp || current_thresholdNrRsrq != needed_thresholdNrRsrq) {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3C: There is a misalignement in the list and it needs to be updated");
                    addRecommendation("UPDATE", anr, "Updating the anrNrMeasConfList from: " + current_anrNrMeasConfList[0] + " to: " + needed_anrNrMeasConfList, "anrNrMeasConfList", needed_anrNrMeasConfList);
                    logger.info("---> INFO: Update is now complete");
                }
    
                else {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3D: The values in the anrNrMeasConfList are aligned with the needed values so updates are needed");
                    
                }
            }


            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO 3E: Now we will check the anrNrParamList");

            def current_anrNrParamList = anr.getListParameter("anrNrParamList");
            def needed_anrNrParamList = createList();
            needed_anrNrParamList.addEntry([
                "anrDefRelDcAllowed": "1",
                "carrierFreqNrCell": "null",
                "frqNrCarSelect": "1",
                "gNBIdLength": "22",
                "paramMeasConfId": "1",
                "paramPlmnId": "1"
            ])

            def needed_anrDefRelDcAllowed = 1;
            def needed_carrierFreqNrCell = "null";
            def needed_frqNrCarSelect = 1;
            def needed_anrNrParamList_gNBIdLength = 22;
            def needed_anrNrParamList_paramMeasConfId = 1;
            def needed_anrNrParamList_paramPlmnId = 1;
            def needed_anrNrParamList_size = needed_anrNrParamList.size()

            if(current_anrNrParamList.size() == 0 ) {
                logger.info("---> INFO: the anrNrMeasConflist does not exist on this cell and we will create it");
                addRecommendation("UPDATE", anr, "Creating the anrNrParamList", "anrNrParamList", needed_anrNrParamList);
                logger.info("---> Update is now complete");
            } 

            else {
                def current_anrDefRelDcAllowed = current_anrNrParamList[0]["anrDefRelDcAllowed"];
                def current_carrierFreqNrCell = current_anrNrParamList[0]["carrierFreqNrCell"];
                def current_frqNrCarSelect = current_anrNrParamList[0]["frqNrCarSelect"];
                def current_anrNrParamList_gNBIdLength = current_anrNrParamList[0]["gNBIdLength"];
                def current_anrNrParamList_paramMeasConfId = current_anrNrParamList[0]["paramMeasConfId"];
                def current_anrNrParamList_paramPlmnId = current_anrNrParamList[0]["paramPlmnId"];
                def current_anrNrParamList_size = current_anrNrParamList.size();
    
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 3E: These are the current and needed values for anrNrParamList");
                logger.info("current_anrDefRelDcAllowed is: " + current_anrDefRelDcAllowed + " and the needed value is: "+ needed_anrDefRelDcAllowed);
                logger.info("current_carrierFreqNrCell is: " + current_carrierFreqNrCell + " and the needed value is: " + needed_carrierFreqNrCell);
                logger.info("current_frqNrCarSelect is: " + current_frqNrCarSelect + " and the needed value is: " + needed_frqNrCarSelect);
                logger.info("current_anrNrParamList_gNBIdLength is: " + current_anrNrParamList_gNBIdLength + " and the needed value is: " +needed_anrNrParamList_gNBIdLength);
                logger.info("current_anrNrParamList_paramMeasConfId is: " + current_anrNrParamList_paramMeasConfId + " and the needed value is: " + needed_anrNrParamList_paramMeasConfId);
                logger.info("current_anrNrParamList_paramPlmnId is: " + current_anrNrParamList_paramPlmnId + " and the needed value is: " + needed_anrNrParamList_paramPlmnId);


           

            if(current_anrNrParamList.size() != needed_anrNrParamList_size 
                || current_anrDefRelDcAllowed != needed_anrDefRelDcAllowed 
                || current_carrierFreqNrCell != needed_carrierFreqNrCell 
                || current_frqNrCarSelect != needed_frqNrCarSelect 
                || current_anrNrParamList_gNBIdLength != needed_anrNrParamList_gNBIdLength 
                || current_anrNrParamList_paramMeasConfId != needed_anrNrParamList_paramMeasConfId 
                || current_anrNrParamList_paramPlmnId != needed_anrNrParamList_paramPlmnId) {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3F: There is a misalignement in the list and it needs to be updated");
                    addRecommendation("UPDATE", anr, "Updating the anrNrParamList from: " + current_anrNrParamList[0] + " to: " + needed_anrNrParamList, "anrNrParamList", needed_anrNrParamList);
                    logger.info("---> INFO: Update is now complete");
                }

                else {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3G: The needed values are aligned with the current values in the anrNrParamList and no updates are needed");

                }
            }

    

                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 3H: Now we will check the anrNrParamPlmnList");
                
                def current_anrNrParamPlmnList = anr.getListParameter("anrNrParamPlmnList");
                def needed_anrNrParamPlmnList = createList();
                needed_anrNrParamPlmnList.addEntry([
                    "mcc": "302",
                    "mnc": "610",
                    "mncLength": "3",
                    "paramPlmnId": "1"
                ])

                def needed_mcc = 302;
                def needed_mnc = 610;
                def needed_anrNrParamPlmnList_paramPlmnId = 1;
                def needed_mncLength = 3;
                def needed_anrNrParamPlmnList_size = needed_anrNrParamPlmnList.size();

                if(current_anrNrParamPlmnList.size() == 0) {
                    logger.info("---> INFO: the anrNrParamPlmnList does not exist on this cell and we will create it");
                    addRecommendation("UPDATE", anr, "Creating the anrNrParamList", "anrNrParamPlmnList", needed_anrNrParamPlmnList);
                    logger.info("---> Update is now complete");
                }

                else {
                    def current_anrNrParamPlmnList_size = current_anrNrParamPlmnList.size();
                    def current_mcc = current_anrNrParamPlmnList[0]["mcc"];
                    def current_mnc = current_anrNrParamPlmnList[0]["mnc"];
                    def current_mncLength = current_anrNrParamPlmnList[0]["mncLength"];
                    def current_anrNrParamPlmnList_paramPlmnId = current_anrNrParamPlmnList[0]["paramPlmnId"];
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3I: These are the current and needed values for anrNrParamPlmnList");
                    logger.info("current_mcc is: " + current_mcc + " and the needed value is: "+ needed_mcc);
                    logger.info("current_mnc is: " + current_mnc + " and the needed value is: " + needed_mnc);
                    logger.info("current_mncLength is: " + current_mncLength + " and the needed value is: " + needed_mncLength);
                    logger.info("current_anrNrParamPlmnList_paramPlmnId is: " + current_anrNrParamPlmnList_paramPlmnId + " and the needed value is: " + needed_anrNrParamPlmnList_paramPlmnId);

                if(current_anrNrParamPlmnList.size() != needed_anrNrParamPlmnList_size 
                || current_mcc != needed_mcc || current_mnc != needed_mnc 
                || current_anrNrParamPlmnList_paramPlmnId != needed_anrNrParamPlmnList_paramPlmnId 
                ||current_mncLength != needed_mncLength) {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3H: There is a misalignement in the list and it needs to be updated");
                    addRecommendation("UPDATE", anr, "Updating the anrNrParamPlmnList from: " + current_anrNrParamPlmnList[0] + " to: " + needed_anrNrParamPlmnList, "anrNrParamPlmnList", needed_anrNrParamPlmnList);
                    logger.info("---> INFO: Update is now complete");
                }

                else {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3I: The needed values are aligned with the current values in the anrNrParamPlmnList and no updates are needed");
                }

                }

                

                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 3I: Now we will check the parameter anrNrCoverageConfirmTimer");

                def current_anrNrCoverageConfirmTimer = anr.getParameterAsInteger("anrNrCoverageConfirmTimer");
                def needed_anrNrCoverageConfirmTimer = 2500;
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 3J: current_anrNrCoverageConfirmTimer is: " +current_anrNrCoverageConfirmTimer + " and the needed value is: " +needed_anrNrCoverageConfirmTimer);


                if(current_anrNrCoverageConfirmTimer != needed_anrNrCoverageConfirmTimer) {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3K: The current anrNrCoverageConfirmTimer does not equal the needed value and we will update the parameter");
                    addRecommendation("UPDATE", anr, "Updating the anrNrCoverageConfirmTimer from: " + current_anrNrCoverageConfirmTimer+ " to: " + needed_anrNrCoverageConfirmTimer, "anrNrCoverageConfirmTimer", needed_anrNrCoverageConfirmTimer);
                    logger.info("---> INFO: Update is now complete");
                }
                
                else {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3L: The needed values are aligned with the current values for the parameter anrNrCoverageConfirmTimer and no updates are needed");
                }
                
                def anrNrInactivityTimer = anr.getParameterAsInteger("anrNrInactivityTimer");
                def needed_anrNrInactivityTimer = 3;

                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 3M: Now we will check the parameter anrNrInactivityTimer");


                    if(anrNrInactivityTimer != needed_anrNrInactivityTimer) {
                        logger.info("**********************************************************************************************************************************************************************************************");
                        logger.info("---> INFO 3N: The anrNrInactivityTimer is not aligned with the needed value and will be updated");
                        addRecommendation("UPDATE", anr, "Updating the anrNrInactivityTimer from: " +anrNrInactivityTimer+ " to: " + needed_anrNrInactivityTimer, "anrNrInactivityTimer", needed_anrNrInactivityTimer)
                        logger.info("---> INFO: Update is now complete");
                    }

                else {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3O: The anrNrInactivityTimer is aligned with the needed value does not needed updating");
                }
                
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 3P: We will now check the actAutoNrNeighRemoval");
                current_actAutoNrNeighRemoval = anr.getParameterAsInteger("actAutoNrNeighRemoval");
                needed_actAutoNrNeighRemoval = 1;
                logger.info("---> INFO: In order for this parameter to be update the following conditions must be set to true");
                logger.info("---> INFO: actEndcAutoX2Step up is set to: " + actEndcAutoX2Setup_Flag);
                logger.info("---> INFO: actLteNrDualConnectivity is: " + current_actLteNrDualConnectivity);
                logger.info("---> INFO: The current actAutoNrNeightRemoval is: " + current_actAutoNrNeighRemoval + " and the needed value is: " + needed_actAutoNrNeighRemoval);

                if((current_actLteNrDualConnectivity != needed_actLteNrDualConnectivity) && (actEndcAutoX2Setup_Flag)) {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO: The actAutoNrNeighRemoval passes the condition for update on this cell ");
                    if(current_actAutoNrNeighRemoval != needed_actAutoNrNeighRemoval) {
                        logger.info("**********************************************************************************************************************************************************************************************");
                        logger.info("---> INFO 3Q: the actAutoNrNeighRemoval does not equal the needed value so we will update");
                        addRecommendation("UPDATE", anr, "Updating the actAutoNrNeighRemoval from: " + current_actAutoNrNeighRemoval + " to: " + needed_actAutoNrNeighRemoval, "actAutoNrNeighRemoval", needed_actAutoNrNeighRemoval);
                        logger.info("---> INFO: UPDATE IS NOW COMPLETE");
                    }
    
                    else {
                        logger.info("**********************************************************************************************************************************************************************************************");
                        logger.info("---> INFO 3R: the actAutoNrNeighRemoval equals the needed value");
                    }
                }

                else {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 3S: The actAutoNrNeighRemoval DOES NOT pass the condition for update on this cell");
                }

                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 3T: We will now check the anrNrNbrRelRemGuardTime");
                current_anrNrNbrRelRemGuardTime = anr.getParameterAsInteger("anrNrNbrRelRemGuardTime");
                needed_anrNrNbrRelRemGuardTime = 72;
                logger.info("---> INFO: In order for this parameter to be update it must meet the following condition");
                logger.info("---> The actUeBasedAnrNr_Updated is: " + actUeBasedAnrNr_Updated);
                logger.info("---> The current actLteNrDualConnectivity is: " + current_actLteNrDualConnectivity);
                logger.info("---> The current anrNrNbrRelRemGuardTime is: " + current_anrNrNbrRelRemGuardTime + " and the needed value is: " + needed_anrNrNbrRelRemGuardTime);

                if(actUeBasedAnrNr_Updated && (current_actLteNrDualConnectivity == needed_actLteNrDualConnectivity)) {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO: the anrNrNbrRelRemGuardTime passes the update condition");

                    if(current_anrNrNbrRelRemGuardTime != needed_anrNrNbrRelRemGuardTime) {
                        logger.info("**********************************************************************************************************************************************************************************************");
                        logger.info("---> INFO 3U: the anrNrNbrRelRemGuardTime does not equal needed value");
                        addRecommendation("UPDATE", anr, "Updating the anrNrNbrRelGuardTime from: " + current_anrNrNbrRelRemGuardTime + " to: " + needed_anrNrNbrRelRemGuardTime, "anrNrNbrRelRemGuardTime", needed_anrNrNbrRelRemGuardTime);
                        logger.info("---> INFO: UPDATE IS COMPLETE");
                    }

                    else {
                        logger.info("**********************************************************************************************************************************************************************************************");
                        logger.info("---> INFO 3V: the anrNrNbrRelRemGuardTime equals needed value");
                    }
                }

                else {
                        logger.info("**********************************************************************************************************************************************************************************************");
                        logger.info("---> INFO 3W: the anrNrNbrRelRemGuardTime does pass the needed condition from update");
                }
            }

            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO: Updates on ANR MO parameters are now complete");
            logger.info("---> INFO 4: Now we will check the nrdcdpr mo and the corresponding parameter");

        for(nrdcdpr in nrdcdprs) {
            def current_nrPciNbrRelAddHyst = nrdcdpr.getParameter("nrPciNbrRelAddHyst");
            def needed_nrPciNbrRelAddHyst = "0.998"

            if(current_nrPciNbrRelAddHyst != needed_nrPciNbrRelAddHyst) {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 4A: The current nrPciNbrRelAddHyst does not equal the needed the needed value and we will update");
                addRecommendation("UPDATE", nrdcdpr, "Updating the nrPciNbrRelAddHyst from: " + current_nrPciNbrRelAddHyst + "to: " + needed_nrPciNbrRelAddHyst, "nrPciNbrRelAddHyst", needed_nrPciNbrRelAddHyst);
                logger.info("---> INFO: UPDATE NOW COMPLETE");
            }

            else {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 4B: The needed and current values are aligned and no updates are needed");
            }

        }

        logger.info("**********************************************************************************************************************************************************************************************");
        logger.info("---> INFO: Updates on nrdcdpr MO parameters are now complete");
        logger.info("---> INFO 5: Now we will check the nrdcpr mo and the corresponding parameter");


        for(nrdcpr in nrdcprs) {
            def current_nrPciNbrRelAddHyst = nrdcdpr.getParameter("nrPciNbrRelAddHyst");
            def needed_nrPciNbrRelAddHyst = "0.998"

            if(current_nrPciNbrRelAddHyst != needed_nrPciNbrRelAddHyst) {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 5A: The current_nrPciNbrRelAddHyst does not equal to needed value");
                addRecommendation("UPDATE", nrdcpr, "Updating the nrPciNbrRelAddHyst from: " + current_nrPciNbrRelAddHyst + "to: " + needed_nrPciNbrRelAddHyst, "nrPciNbrRelAddHyst", needed_nrPciNbrRelAddHyst);
                logger.info("---> INFO: UPDATE NOW COMPLETE");
            }

            else {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 5B: The needed and current values are aligned and no updates are needed");
            }

        }
    }

    else {
        logger.info("**********************************************************************************************************************************************************************************************");
        logger.info("---> INFO: The actUeBasedAnrNr condition flag is set to: " + actUeBasedAnrNr_Updated + " therefore we are unable to update the parameters for this feature on this cell");
    }

}
logger.info(" ************** Total number of evaluated objects: MRBTS " + number_of_lnbts);
logger.info(" ************** End of " + cpr_name);