/*
CPR NAME = 5GC001085
Version 1
Developed by Shannon Abeda
Date: Feb 12, 2023

    The 5GC001085: ANR for SA: Intra-NR Intra-frequency UE Based feature implements the automatic neighbor relation (ANR) mechanism for automatically discover and handling the NR neighbor cells on the same carrier frequency (intra-frequency) configured in standalone option 2 architecture. The feature provides:

    An intra-gNB and inter-gNB (on existing and non-existing Xn link) neighbor relations support of the cells on the same carrier frequency
    An automatic creation of neighbor gNB and Xn link managed objects in the source gNB
    An automatic mechanism to discover new intra-frequency NR neighbor cells.
    An automatic creation of the neighbor relations between the newly detected neighbor cells.
    An identification of suitable NR-CGI value for the unknown PCI based on UE measurements.
    The ability to request to ignore unknown NR-PCI measurements below defined reference signal received power (RSRP) threshold thus preventing adding "weaker" neighbors.
    An ANR neighbor removal function to automatically delete rarely used gNB neighbor relations.

    The following parameters were updated:
        consecHoFailThres
        actSaUeBasedAnrNr
        actConflictConfiguration
        actEnhanceIntraNrIntraFreqAnr
        actAutoNrNbRemoval
        anrThresholdSelector
        anrThresRsrpNbCell
        cellDepMode
        consecRecheckForNrNbRemoval
        idleTimeForNrNbRemoval
        maxNumAnrNrrelAllowed

        Current restraints on parameters are the following:
        
        NRBTS.actSaUeBasedAnrNr  == 1 for these parameters
        actEnhanceIntraNrIntraFreqAnr
        actAutoNrNbRemoval
        anrThresholdSelector
        anrThresRsrpNbCell
        cellDepMode
        consecRecheckForNrNbRemoval
        idleTimeForNrNbRemoval
        maxNumAnrNrrelAllowed

        actSaUeBasedAnrNr
        NRBTS.actGeneralConnectivityBronze = 1 && NRBTS.gNbCuType == SA (1) or SA+NSA (10) && NRBTS.actIntraFreqHoSA == 1

        actEnhanceIntraNrIntraFreqAnr
        NRBTS.actSaUeBasedAnrNr == 1 || NRBTS.actNsaUeBasedAnrNr == 1    


*/

cpr_name = "5GC001085";
//logger.info(" **** Beginning of " + cpr_name);
//logger.info("******** THIS CPR WILL ACTIVATE AUTOMATIC NEIGHBOR RELATION (ANR) ********************************************* ");

def number_of_nrbts = 0;

for (nrbts in _context.getMos("NRBTS")) {
    number_of_nrbts++;
   
    def nrcells = nrbts.getMos("NRCELL");
    def mrbts = nrbts.getMos("MRBTS");
	def mrbtsname  = mrbts.getParameter("name");
	def gNodeBID = mrbts.getParameterAsInteger("originalId");
	def nrbtsname = nrbts.getParameter("name");
    
    def siteID = nrcells.getPhysicalParameter("SITEID");
	def site_region = nrcells.getPhysicalParameter("TERRITORY");
	def site_zone = nrcells.getPhysicalParameter("ZONE");
	
	def nrbtsversion = nrbts.getParameter("version");
    

    def nranrs = nrbts.getMos("NRANR");
    def nranrprs = nrbts.getMos("NRANRPR");
   

    //logger.info("**********************************************************************************************************************************************************************************************");
	//logger.info("---> INFO: Here is the Evaluated mrbtsname: '"+mrbtsname +" and the gNodeBID: "+gNodeBID + " and the site_region, site_zone and nrbtsname: "+site_region + " ; "+ site_zone+ " ; "+nrbtsname);
	//logger.info("**********************************************************************************************************************************************************************************************");

    def current_actSaUeBasedAnrNr = nrbts.getParameterAsInteger("actSaUeBasedAnrNr");
    def current_actConflictConfiguration = nrbts.getParameterAsInteger("actConflictConfiguration");
    def current_actEnhanceIntraNrIntraFreqAnr = nrbts.getParameterAsInteger("actEnhanceIntraNrIntraFreqAnr");
    def current_actGeneralConnectivityBronze = nrbts.getParameterAsInteger("actGeneralConnectivityBronze");
    def current_gNbCuType = nrbts.getParameterAsInteger("gNbCuType");
    def current_actIntraFreqHoSA = nrbts.getParameterAsInteger("actIntraFreqHoSA");
    def current_actNsaUeBasedAnrNr = nrbts.getParameterAsInteger("actNsaUeBasedAnrNr");
    
    
    //Needed values for parameters
    def neededBoolValue = 1;
    def needed_consecHoFailThresValue = 0;
    def needed_anrThresholdSelector = 0;
    def needed_anrThresRsrpNbCell = -115;
    def needed_cellDepMode = 10;
    def needed_consecRecheckForNrNbRemoval = 5;
    def needed_idleTimeForNrNbRemoval = 72;
    def needed_maxNumAnrNrrelAllowed = 256;
    def needed_actAutoNrNbRemoval = 1;
    def needed_actAutoEutraNbRemoval = 0;
    def gNbCuType_SA_NSA = 10;
    def gNbCuType_SA = 1;
    def conditionFlag = false;
    def activationFlag = false;
    def actEnhanceIntraNrIntraFreqAnr_Condition_Flag = false;


    //Conditions printing
    /*logger.info("**********************************************************************************************************************************************************************************************");
    logger.info("---> INFO: STEP 1 WE WILL UPDATE THE nrbts PARAMETERS FIRST ");
    logger.info("**********************************************************************************************************************************************************************************************");
    logger.info("---> INFO: In order to update the actSaUeBasedAnrNr it must meet the following parameter conditions: ");
    logger.info("---> INFO: The actGeneralConnectivityBronze must be set to:  " + neededBoolValue);
    logger.info("---> INFO: The current_gNbCuType must be set to: " + gNbCuType_SA_NSA + " or: " + gNbCuType_SA);
    logger.info("---> INFO: The current_actIntraFreqHoSA must be set to: " + neededBoolValue);
    logger.info("**********************************************************************************************************************************************************************************************");
    logger.info("---> INFO: The current_gNbCuType is: " + current_gNbCuType);
    logger.info("---> INFO: The current_actGeneralConnectivityBronze is: " + current_actGeneralConnectivityBronze);
    logger.info("---> INFO: The current_actIntraFreqHoSA is: " + current_actIntraFreqHoSA);
    logger.info("---> INFO: Now we will check if it meets the above conditions");
    logger.info("**********************************************************************************************************************************************************************************************");
    */
    if((current_actGeneralConnectivityBronze == neededBoolValue) && (current_gNbCuType == gNbCuType_SA_NSA || current_gNbCuType == gNbCuType_SA) && (current_actIntraFreqHoSA == neededBoolValue)) {
        conditionFlag = true
        //logger.info("**********************************************************************************************************************************************************************************************");
        //logger.info("---> INFO 1A: The conditionFlag is set to: " + conditionFlag + " and we will update actSaUeBasedAnrNr parameters if needed");
        //logger.info("**********************************************************************************************************************************************************************************************");
    }

    else {
        //logger.info("**********************************************************************************************************************************************************************************************");
        //logger.info("---> INFO 1B: The conditionFlag is set to: " + conditionFlag + " and we will NOT update the actSaUeBasedAnrNr parameters");
        //logger.info("**********************************************************************************************************************************************************************************************");
    }
    
   

    //Conditions:  NRBTS.actGeneralConnectivityBronze = 1 && NRBTS.gNbCuType == SA (1) or SA+NSA (10) && NRBTS.actIntraFreqHoSA == 1
    if(conditionFlag) {
            //logger.info("**********************************************************************************************************************************************************************************************");
            //logger.info("---> INFO 1C: current_actSaUeBasedAnrNr has met the conditions to be updated");
            //logger.info("---> INFO: The current_actSaUeBasedAnrNr is: " + current_actSaUeBasedAnrNr + " and the needed value is: " + neededBoolValue);
            //logger.info("**********************************************************************************************************************************************************************************************");
            
            if(current_actSaUeBasedAnrNr != neededBoolValue) {
                //logger.info("**********************************************************************************************************************************************************************************************");
                //logger.info("---> INFO 1D: current_actSaUeBasedAnrNr does not equal the needed value and will be change to: " + neededBoolValue);
                addRecommendation("UPDATE", nrbts, "Activating actSaUeBasedAnrNr: " + current_actSaUeBasedAnrNr + "to: " + neededBoolValue, "actSaUeBasedAnrNr", neededBoolValue);
                activationFlag = true;
                //logger.info("---> INFO: Update now completed and the activationFeatureFlag has been set to: " + activationFlag);
                //logger.info("**********************************************************************************************************************************************************************************************");
                
            }

            else {
                //logger.info("**********************************************************************************************************************************************************************************************");
                //logger.info("---> INFO 1E: current_actSaUeBasedAnrNr is already to set to: " + neededBoolValue + " and no updates are needed");
                //logger.info("**********************************************************************************************************************************************************************************************");
            }
        }   
    
    else {
        //logger.info("**********************************************************************************************************************************************************************************************");
        //logger.info("---> INFO 1F: current_actSaUeBasedAnrNr has NOT met the conditions to be updated on this cell as the flag for this cell is set to: " + conditionFlag);
        //logger.info("**********************************************************************************************************************************************************************************************");

    }


    //logger.info("---> INFO 1G: Now we will updated actEnhanceIntraNrIntraFreqAnr");
    //  NRBTS.actSaUeBasedAnrNr OR NRBTS.actNsaUeBasedAnrNr
    if(activationFlag || current_actNsaUeBasedAnrNr == true) {
        actEnhanceIntraNrIntraFreqAnr_Condition_Flag = true;
        //logger.info("**********************************************************************************************************************************************************************************************");
        //logger.info("---> INFO 1H: The actEnhanceIntraNrIntraFreqAnr_Condition_Flag is set to: " + actEnhanceIntraNrIntraFreqAnr_Condition_Flag + " and we will now update the parameters as needed");
        //logger.info("**********************************************************************************************************************************************************************************************");
    }

    if(actEnhanceIntraNrIntraFreqAnr_Condition_Flag) {
        //logger.info("**********************************************************************************************************************************************************************************************");
        //logger.info("---> INFO: The current_actEnhanceIntraNrIntraFreqAnr is: " + current_actEnhanceIntraNrIntraFreqAnr + " and the needed value is: " + neededBoolValue);
        //logger.info("**********************************************************************************************************************************************************************************************");
        if(current_actEnhanceIntraNrIntraFreqAnr != neededBoolValue) {
            //logger.info("**********************************************************************************************************************************************************************************************");
            //logger.info("---> INFO 1I: current_actEnhanceIntraNrIntraFreqAnr does not equal the needed value and will be change to: " + neededBoolValue);
             addRecommendation("UPDATE", nrbts, "Activating actEnhanceIntraNrIntraFreqAnr: " + current_actEnhanceIntraNrIntraFreqAnr + "to: " + neededBoolValue, "actEnhanceIntraNrIntraFreqAnr", neededBoolValue);
            //logger.info("---> INFO: Update now completed");
            //logger.info("**********************************************************************************************************************************************************************************************");
        }
    
        else {
            //logger.info("**********************************************************************************************************************************************************************************************");
            //logger.info("---> INFO 1J: current_actEnhanceIntraNrIntraFreqAnr is already to set to: " + neededBoolValue);
            //logger.info("**********************************************************************************************************************************************************************************************");
        }
    }

    else {
        //logger.info("**********************************************************************************************************************************************************************************************");
        //logger.info("---> INFO  1K: The actEnhanceIntraNrIntraFreqAnr_Condition_Flag is set to: " + actEnhanceIntraNrIntraFreqAnr_Condition_Flag + " and we will NOT update the parameter");
        //logger.info("**********************************************************************************************************************************************************************************************");
    }


    
    
    //logger.info("**********************************************************************************************************************************************************************************************");
    //logger.info("---> INFO 1L: We will now check the parameter actConflictConfiguration");
    //logger.info("---> INFO: The current_actConflictConfiguration is: " + current_actConflictConfiguration + "and the needed value is: " +neededBoolValue);
    //logger.info("**********************************************************************************************************************************************************************************************");


    if(current_actConflictConfiguration != neededBoolValue) {
        //logger.info("**********************************************************************************************************************************************************************************************");
        //logger.info("---> INFO 1M: current_actConflictConfiguration does not equal the needed value and will be change to: " + neededBoolValue);
        addRecommendation("UPDATE", nrbts, "Activating actConflictConfiguration: " + current_actConflictConfiguration + "to: " + neededBoolValue, "actConflictConfiguration", neededBoolValue);
        //logger.info("---> INFO: Update now completed");
        //logger.info("**********************************************************************************************************************************************************************************************");
    }

    else {
        //logger.info("**********************************************************************************************************************************************************************************************");
        //logger.info("---> INFO 1O: current_actConflictConfiguration is: " + current_actConflictConfiguration + " and does not need to be updated to the: " + neededBoolValue);
        //logger.info("**********************************************************************************************************************************************************************************************");
    }


    //logger.info("**********************************************************************************************************************************************************************************************");
    //logger.info("---> INFO 1P: The NRBTS parameter updates are now complete for this cell");
    //logger.info("**********************************************************************************************************************************************************************************************");


    //logger.info("**********************************************************************************************************************************************************************************************");
    //logger.info("---> INFO: STEP 2 Now will now update the MO NRANR");
    //logger.info("---> INFO: The activation flag is set to: " + activationFlag);
    //logger.info("**********************************************************************************************************************************************************************************************");
    
    if(activationFlag){
        if(nranrs.size() > 0) {
            for(nranr in nranrs) {
                def current_consecHoFailThres = nranr.getParameterAsInteger("consecHoFailThres");
                def current_actAutoNrNbRemoval = nranr.getParameterAsInteger("actAutoNrNbRemoval");
                def current_actSaUeBasedAnrEutra = nranr.getParameterAsInteger("actSaUeBasedAnrEutra");
                def current_actAutoEutraNbRemoval = nranr.getParameterAsInteger("actAutoEutraNbRemoval");

                //logger.info("**********************************************************************************************************************************************************************************************");
                //logger.info("---> INFO: Netact validation error fixiing");

                if(current_actSaUeBasedAnrEutra == 0) {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO: actSaUeBasedAnrEutra is set to false ");

                    if(current_actAutoEutraNbRemoval != 0) {
                        //logger.info("**********************************************************************************************************************************************************************************************");
                        //logger.info("---> INFO: current_actAutoEutraNbRemoval is NOT set to false so we will update");
                        addRecommendation("UPDATE", nranr, "Updating actAutoEutraNbRemoval to set to false", "actAutoEutraNbRemoval", 0);
                    }
                }

                //logger.info("**********************************************************************************************************************************************************************************************");
                //logger.info("---> INFO 2A: The current_consecHoFailThres value is: " + current_consecHoFailThres + " and the needed value is: " +needed_consecHoFailThresValue);
                //logger.info("**********************************************************************************************************************************************************************************************");
        
                if(current_consecHoFailThres != needed_consecHoFailThresValue) {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 2B: The current_consecHoFailThres does equal to the needed value therefore must be updated");
                    addRecommendation("UPDATE", nranr, "UPDATING current_consecHoFailThres from: " + current_consecHoFailThres + " to: " + needed_consecHoFailThresValue, "consecHoFailThres", needed_consecHoFailThresValue);
                    //logger.info("---> INFO: Update now completed");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }
        
                else {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 2C: The current_consecHoFailThres is equal the needed_consecHoFailThresValue and does not needed to be updated");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }

                //logger.info("**********************************************************************************************************************************************************************************************");
                //logger.info("---> INFO 2D: We will now check the parameter actAutoNrNbRemoval");
                //logger.info("---> INFO: The current_actAutoNrNbRemoval is: " + current_actConflictConfiguration + "and the needed value is: " + needed_actAutoNrNbRemoval);
                //logger.info("**********************************************************************************************************************************************************************************************");

                if(current_actAutoNrNbRemoval != needed_actAutoNrNbRemoval) {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 2E: The current_actAutoNrNbRemoval does equal to the needed value therefore must be updated");
                    addRecommendation("UPDATE", nranr, "UPDATING current_actAutoNrNbRemoval from: " + current_actAutoNrNbRemoval + " to: " + needed_actAutoNrNbRemoval, "actAutoNrNbRemoval", needed_actAutoNrNbRemoval);
                    //logger.info("---> INFO: Update now completed");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }

                else {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 2F: The current_actAutoNrNbRemoval is equal the needed_actAutoNrNbRemoval and does not needed to be updated");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }
            }
        }


        //MO Creation
        else {
            //logger.info("**********************************************************************************************************************************************************************************************");
            //logger.info("---> INFO 2G: This NRANR MO does not exist and must be created");
            //logger.info("**********************************************************************************************************************************************************************************************");
            
            def nranr = buildMo("NRANR", nrbts,1);	
            
            nranr.setParameterValue("defaults", "Default_5G");
            nranr.setParameterValue("actAutoNrNbRemoval", needed_actAutoNrNbRemoval);
            nranr.setParameterValue("consecHoFailThres", needed_consecHoFailThresValue);
            nranr.setParameterValue("actAutoEutraNbRemoval", needed_actAutoEutraNbRemoval);
            //logger.info("**********************************************************************************************************************************************************************************************");
            addRecommendation("CREATE", nranr," Creating nranrpr");
            //logger.info("----> INFO: nranr MO has been created");
            //logger.info("----> INFO: Finish creating nranr MO");

        }
    }

    else {
        //logger.info("**********************************************************************************************************************************************************************************************");
        //logger.info("---> INFO: The activation flag has not been set to true and therefore we will not update the NRANR MO");
        //logger.info("**********************************************************************************************************************************************************************************************");
    }


    //logger.info("**********************************************************************************************************************************************************************************************");
    //logger.info("---> INFO: STEP 3 Now will continue to update the MO NRANRPR");
    //logger.info("**********************************************************************************************************************************************************************************************");

    //individual update each paramter within the mo if it exist
    if(nranrprs.size() != 0) {
        for(nranrpr in nranrprs) {
            def current_anrThresholdSelector = nranrpr.getParameterAsInteger("anrThresholdSelector"); // needed value is 1
            def current_anrThresRsrpNbCell = nranrpr.getParameterAsInteger("anrThresRsrpNbCell"); // needed value is 0
            def current_cellDepMode = nranrpr.getParameterAsInteger("cellDepMode"); // needed value is 10
            def current_consecRecheckForNrNbRemoval = nranrpr.getParameterAsInteger("consecRecheckForNrNbRemoval"); //needed value is 5
            def current_idleTimeForNrNbRemoval = nranrpr.getParameterAsInteger("idleTimeForNrNbRemoval"); // needed value is 72
            def current_maxNumAnrNrrelAllowed = nranrpr.getParameterAsInteger("maxNumAnrNrrelAllowed"); //needed value is 256

            //logger.info("**********************************************************************************************************************************************************************************************");
            //logger.info("---> INFO 3A : These are the current values on this cell: ");
            //logger.info("---> INFO: The current_anrThresholdSelector is: " + current_anrThresholdSelector + "and the needed value is: " + needed_anrThresholdSelector);
            //logger.info("---> INFO: The current_anrThresRsrpNbCell is: " + current_anrThresRsrpNbCell + "and the needed value is: " + needed_anrThresRsrpNbCell);
            //logger.info("---> INFO: The current_cellDepMode is: " + current_cellDepMode + "and the needed value is: " + needed_cellDepMode);
            //logger.info("---> INFO: The current_consecRecheckForNrNbRemoval is: " + current_consecRecheckForNrNbRemoval + "and the needed value is: " + needed_consecRecheckForNrNbRemoval);
            //logger.info("---> INFO: The current_idleTimeForNrNbRemoval is: " + current_idleTimeForNrNbRemoval + "and the needed value is: " + needed_idleTimeForNrNbRemoval);
            //logger.info("---> INFO: The current_maxNumAnrNrrelAllowed is: " + current_maxNumAnrNrrelAllowed + "and the needed value is: " + needed_maxNumAnrNrrelAllowed);
            //logger.info("**********************************************************************************************************************************************************************************************");

            //Condition NRBTS.actSaUeBasedAnrNr = 1
            if(current_actSaUeBasedAnrNr == neededBoolValue) {
                //logger.info("**********************************************************************************************************************************************************************************************")
                //logger.info("---> INFO 3C: current_actSaUeBasedAnrNr is set to: " + neededBoolValue + " and meets the needed conditions");
                //logger.info("**********************************************************************************************************************************************************************************************");

                if(current_anrThresholdSelector != needed_anrThresholdSelector) {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3D: The current_anrThresholdSelector does equal to the needed value therefore must be updated");
                    addRecommendation("UPDATE", nranrpr, "UPDATING current_anrThresholdSelector from: " + current_anrThresholdSelector + " to: " + needed_anrThresholdSelector, "anrThresholdSelector", needed_anrThresholdSelector);
                    //logger.info("---> INFO: Update complete");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }

                else {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3E: The current_anrThresholdSelector is equal the needed_anrThresholdSelector and does not needed to be updated");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }

    
                if(current_anrThresRsrpNbCell != needed_anrThresRsrpNbCell) {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3F: The current_anrThresRsrpNbCell does equal to the needed value and therefore must be updated");
                    addRecommendation("UPDATE", nranrpr, "UPDATING current_anrThresRsrpNbCell from: " + current_anrThresRsrpNbCell + " to: " + needed_anrThresRsrpNbCell, "anrThresRsrpNbCell", needed_anrThresRsrpNbCell);
                    //logger.info("---> INFO: Updates are complete");
                    //logger.info("**********************************************************************************************************************************************************************************************");

                }

                else {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3G: The current_anrThresRsrpNbcell equals the needed value and no updates are needed");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }

                if(current_cellDepMode != needed_cellDepMode) {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3H: The current_cellDepMode does equal to the needed value and therefore must be updated");
                    addRecommendation("UPDATE", nranrpr, "UPDATING current_cellDepMode from: " + current_cellDepMode + " to: " + needed_cellDepMode, "cellDepMode", needed_cellDepMode);
                    //logger.info("---> INFO: Updates are complete");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }

                else {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3I: The current_cellDepMode equals the needed value and no updates are needed");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }

                if(current_consecRecheckForNrNbRemoval != needed_consecRecheckForNrNbRemoval) {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3J: The current_consecRecheckForNrNbRemoval does equal to the needed value and therefore must be updated");
                    addRecommendation("UPDATE", nranrpr, "UPDATING current_consecRecheckForNrNbRemoval from: " + current_consecRecheckForNrNbRemoval + " to: " + needed_consecRecheckForNrNbRemoval, "consecRecheckForNrNbRemoval", needed_consecRecheckForNrNbRemoval);
                    //logger.info("---> INFO: Updates are complete");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }

                else {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3K: The current_consecRecheckForNrNbRemoval equals the needed value and no updates are needed");
                    //logger.info("**********************************************************************************************************************************************************************************************");

                }

                if(current_idleTimeForNrNbRemoval != needed_idleTimeForNrNbRemoval) {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3L: The current_idleTimeForNrNbRemoval does equal to the needed value and therefore must be updated");
                    addRecommendation("UPDATE", nranrpr, "UPDATING current_idleTimeForNrNbRemoval from: " + current_idleTimeForNrNbRemoval + " to: " + needed_idleTimeForNrNbRemoval, "idleTimeForNrNbRemoval", needed_idleTimeForNrNbRemoval);
                    //logger.info("---> INFO: Updates are complete");
                    //logger.info("**********************************************************************************************************************************************************************************************");

                }

                else {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3M: The current_idleTimeForNrNbRemoval equals the needed value and no updates are needed");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }

                if(current_maxNumAnrNrrelAllowed != needed_maxNumAnrNrrelAllowed) {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3N: The current_maxNumAnrNrrelAllowed does equal to the needed value and therefore must be updated");
                    addRecommendation("UPDATE", nranrpr, "UPDATING current_maxNumAnrNrrelAllowed from: " + current_maxNumAnrNrrelAllowed + " to: " + needed_maxNumAnrNrrelAllowed, "maxNumAnrNrrelAllowed", needed_maxNumAnrNrrelAllowed);
                    //logger.info("---> INFO: Updates are complete");
                    //logger.info("**********************************************************************************************************************************************************************************************");

                }

                else {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO 3O: The current_maxNumAnrNrrelAllowed equals the needed value and no updates are needed");
                    //logger.info("**********************************************************************************************************************************************************************************************");
                }

            }

            else {
                //logger.info("**********************************************************************************************************************************************************************************************");
                //logger.info("---> INFO 3P: Updates on NRANRPR MO cannot be completed as they do not meet the critrea as the current_actSaUeBasedAnrNr is set to: " + current_actSaUeBasedAnrNr);
                //logger.info("**********************************************************************************************************************************************************************************************");
            }

        }

    }
    

    //MO CREATION
    else {
        //logger.info("**********************************************************************************************************************************************************************************************");
        //logger.info("---> INFO 3Q: The MO NRANR does not exist and must be built");
        //logger.info("**********************************************************************************************************************************************************************************************");
        
        def nranrpr = buildMo("NRANRPR", nrbts,1);	
		
        nranrpr.setParameterValue("defaults", "Default_5G");
        nranrpr.setParameterValue("anrThresholdSelector", needed_anrThresholdSelector);
        nranrpr.setParameterValue("anrThresRsrpNbCell", needed_anrThresRsrpNbCell);
        nranrpr.setParameterValue("cellDepMode", needed_cellDepMode);
        nranrpr.setParameterValue("consecRecheckForNrNbRemoval", needed_consecRecheckForNrNbRemoval);
        nranrpr.setParameterValue("idleTimeForNrNbRemoval", needed_idleTimeForNrNbRemoval);
        nranrpr.setParameterValue("maxNumAnrNrrelAllowed",  needed_maxNumAnrNrrelAllowed);
        //logger.info("**********************************************************************************************************************************************************************************************");
		addRecommendation("CREATE", nranrpr," Creating nranrpr");
        //logger.info("----> INFO: nranrpr MO has been created");
        //logger.info("----> INFO: Finish creating nranrpr MO");
        //logger.info("**********************************************************************************************************************************************************************************************");
    }   
}
//logger.info("**********************************************************************************************************************************************************************************************");
//logger.info("--> INFO : FEATURE UPDATES ARE COMPLETED");
//logger.info(" ************** Total number of evaluated objects: NRBTS " + number_of_nrbts);
//logger.info(" ************** End of " + cpr_name);
//logger.info("**********************************************************************************************************************************************************************************************");