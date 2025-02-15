/*
CPR: CB008281 Uplink NR E-CID (RSRP, RSRQ)
Developed by: Shannon Abeda
V1 July 9th 2024

    Introduction of the following parameters:
    actUlNrEcIdPosMethod

    actUlNrEcIdPosMethod
    Conditons:
    NRCELL.celldeptype	SA (1) or SA+NSA (10) && Software >= 24R2	&& NRCELL.nrarfcn <= 2016667
    If NRCELL.actUlNrEcIdPosMethod is equal to TRUE, then NRBTS.nrEcidMeasSupervisionTimer shall be configured.
    If NRCELL.nrarfcn >= 2016667 (FR2), then NRCELL.actUlNrEcIdPosMethod shall be equal to FALSE.

    nrEcidMeasSupervisionTimer
    actUlNrEcIdTAdv
    Conditions:
    NRCELL.actUlNrEcIdPosMethod	TRUE
    NRCELL.actUlNrEcIdPosMethod	TRUE


*/

cpr_name = "N_5G_Uplink_NR_E-CID_Update";
logger.info(" **** Beginning of " + cpr_name);
def number_of_nrtbs = 0;

for (nrbts in _context.getMos("NRBTS")) {
    def mrbts = nrbts.getMos("MRBTS");
    def nrcells = nrbts.getMos("NRCELL");
    def nrcell_FDD = nrcells.getMos("NRCELL_FDD");
    def smodr_rs = nrbts.getMos("SMOD_R");
    def mrbtsname  = mrbts.getParameter("name");
	def gNodeBID = mrbts.getParameterAsInteger("originalId");
	def nrbtsname = nrbts.getParameter("name");
	
	def siteID = nrcells.getPhysicalParameter("SITEID");
	def site_region = nrcells.getPhysicalParameter("TERRITORY");
	def site_zone = nrcells.getPhysicalParameter("ZONE");


    //Software version
    versionRealString = nrbts.getParameter("version");						//Here, versionRealString should look like NRBTSCL5G21B_2105_200 or NRBTSCL22R1_2109_102
    if(versionRealString != null) {
	    versionString = versionRealString.split("_")[0];					//Here, versionString should look like NRBTSCL5G21B or NRBTSCL22R1
	    if(versionString.substring(versionString.size()-2, versionString.size()-1) == "R") {
			versionString = versionString.substring(versionString.size()-4); 	//Here, versionString should look like 22R1
	    }
	    else {
		    versionString = versionString.substring(versionString.size()-3); 	//Here, versionString should look like 21B
	    }
	    versionInt = Integer.parseInt(versionString.substring(0,2));		//Here, versionInt should look like 21 or 22
	    //logger.info(" ++++++++++ versionRealString = " + versionRealString);
	    //logger.info(" ++++++++++ versionString = " + versionString);
	    //logger.info(" ++++++++++ versionInt = " + versionInt); 
    }

    logger.info("**********************************************************************************************************************************************************************************************");
	logger.info("---> INFO: Here is the Evaluated mrbtsname: '"+mrbtsname +" and the gNodeBID: "+gNodeBID + " and the site_region, site_zone and nrbtsname: "+site_region + " ; "+ site_zone+ " ; "+nrbtsname);
	logger.info("**********************************************************************************************************************************************************************************************");

    //Conditional Parameters
    def cellDepTypeSA = 1;
    def cellDepTypeSA_NSA = 10;
    def actUlNrEcIdPosMethod_Flag = false;
    def alreadyActivated_Flag = false;
    def updateFlag = false;
    def nrEcidMeasSupervisionTimer_Update = false;
    def needed_nrarfcn = 2016667;

    //Parameters for updating NRCELLS
    def needed_actUlNrEcIdPosMethod = 1;
    def needed_actUlNrEcIdTAdv = 1;

    //Information
    def current_nrbts_distname = nrbts.getParameter("distName");

    //Parameters for updating NRBTS
    def current_nrEcidMeasSupervisionTimer = nrbts.getParameterAsInteger("nrEcidMeasSupervisionTimer");     //nrEcidMeasSupervisionTimer
    def needed_nrEcidMeasSupervisionTimer = 5000;

    //Condition for nrEcidMeasSupervisionTimer
    def current_timerRRCGuard = nrbts.getParameterAsInteger("timerRRCGuard");

    logger.info("---> INFO 1A: The Software on this cell is: " + versionRealString);
    
   
    logger.info("---> INFO 2: START OF NRCELLS UPDATES");
    for(nrcell in nrcells) {
        //Conditions for Parameters
        def current_distame = nrcell.getParameter("distName");
        def current_nrarfcn;
        def cellDepType = nrcell.getParameterAsInteger("cellDepType");
        def current_actUlNrEcIdPosMethod = nrcell.getParameterAsInteger("actUlNrEcIdPosMethod"); //actUlNrEcIdPosMethod
        def current_actUlNrEcIdTAdv = nrcell.getParameterAsInteger("actUlNrEcIdTAdv");
        def cellTechnology = nrcell.getParameterAsInteger("cellTechnology");

        if(cellTechnology == 0) {
            current_nrarfcn = nrcell_FDD.getParameterAsInteger("nrarfcnDl");
        }

        else {
            current_nrarfcn = nrcell.getParameterAsInteger("nrarfcn");
        }
        
        logger.info("INFO 2A: The current NRCELL is: " + current_distame);
        logger.info("We will update actUlNrEcIdPosMethod parameter however it must meet the following conditions: ");
        logger.info("The celltype needs to be set to either 1 or 10 and the current celltype is: " + cellDepType);
        logger.info("The software version needs to be >= 24R2 and the software version current is: " + versionString);
        logger.info("The cellTechnology is: ${cellTechnology}");
        logger.info("The nrafcn needs to be <= 2016667 and the current nrafcn is: " + current_nrarfcn);

        if(current_nrarfcn <= needed_nrarfcn) {
            logger.info("--> INFO 2B: CONDITION MET AS CURRENT_NRAFCN IS LESS THAN 2016667 NRCELL.actUlNrEcIdPosMethod shall be equal to TRUE")
            if((cellDepType == cellDepTypeSA || cellDepType == cellDepTypeSA_NSA) && (versionString >= "24R2")) {
                logger.info("---> INFO 2C: The software on this cell  condition are met for requirements for update");
                if(current_actUlNrEcIdPosMethod != needed_actUlNrEcIdPosMethod) {
                    logger.info("---> INFO 2D: The current_actUlNrEcIdPosMethod does not equal the required update") 
                    addRecommendation("UPDATE", nrcell, "Updating the current_actUlNrEcIdPosMethod from: " + current_actUlNrEcIdPosMethod + " to: " + needed_actUlNrEcIdPosMethod, "actUlNrEcIdPosMethod", needed_actUlNrEcIdPosMethod);
                    logger.info("---> UPDATE COMPLETE");
                    actUlNrEcIdPosMethod_Flag = true;    //Condition flag being updated
                    logger.info("---> INFO 2E: actUlNrEcIdPosMethod_Flag is now set to: " + actUlNrEcIdPosMethod_Flag);
                    updateFlag = true;
                }
                    
                else {
                    logger.info("---> INFO 2F: Update is not required as the current values matches the needed value");
                    actUlNrEcIdPosMethod_Flag = true; //conditions is being set to false
                                                // Prevents the actUlNrEcIdTAdv from updating as the flag is not being reset
                    alreadyActivated_Flag = true;
                }
            }

            else {
                logger.info("---> INFO 2G: The conditions are not satisify for update on this cell, we will not ");
                actUlNrEcIdPosMethod_Flag = false;
            }
        }

        else {
            logger.info("---> INFO 2H: Update is not allowed as condition is not met");
            actUlNrEcIdPosMethod_Flag = false;
        }
        logger.info("---> INFO 2I: Now we will update actUlNrEcIdTAdv");
        logger.info("---> INFO: In order for this parameter to be update it must meet the following conditions: ");
        logger.info("---> The actUlNrEcIdPosMethod_Flag must be set to true and it is currently set to: " + actUlNrEcIdPosMethod_Flag);
        logger.info("---> Or the alreadyActivated_Flag must be set to true and it is currently set to: " + alreadyActivated_Flag);
    
        if(actUlNrEcIdPosMethod_Flag || alreadyActivated_Flag) {
            if(current_actUlNrEcIdTAdv != needed_actUlNrEcIdTAdv) {
                logger.info("---> INFO 2J: The current_actUlNrEcIdPosMethod does not equal the required update");
                addRecommendation("UPDATE", nrcell, "Updating the current_actUlNrEcIdTAdv from: " + current_actUlNrEcIdTAdv + " to: " + needed_actUlNrEcIdTAdv, "actUlNrEcIdTAdv", needed_actUlNrEcIdTAdv);
                logger.info("---> UPDATE COMPLETE");
            }
            else {
                logger.info("---> INFO 2K: Update is not required as the current values matches the needed value");
            }
        }
    
        else {
            logger.info("---> INFO 2L: Update is not allowed as the conditions for update are not met");
        }
    
        logger.info("---> INFO 2M: END OF NRCELL UPDATES");
    } // end of nrcell


    logger.info("---> INFO 1B: Now we will update the NRBTS Parameters");
    logger.info("This current NRBTS cell is: ${current_nrbts_distname} ");
    logger.info("In order for the current_nrEcidMeasSupervisionTimer to be updated the current_timerRRCGuard must be less that needed_nrEcidMeasSupervisionTimer");
    logger.info("In addition the actUlNrEcIdPosMethod_Flag must be set to true and it is currently set to: ${actUlNrEcIdPosMethod_Flag}");
    logger.info(" OR the updateFlag/alreadyActivated_Flag must be set to true and the updateFlagis currently set to: ${updateFlag} & the alreadyActivated_Flag is set to: ${alreadyActivated_Flag}");
    logger.info("The current_nrEcidMeasSupervisionTimer is ${current_nrEcidMeasSupervisionTimer} and the needed value is: ${needed_nrEcidMeasSupervisionTimer}");
    logger.info("The current_timerRRCGuard is: ${current_timerRRCGuard}");
    logger.info("The actUlNrEcIdPosMethod_Flag is set to: ${actUlNrEcIdPosMethod_Flag}");

    if(actUlNrEcIdPosMethod_Flag || alreadyActivated_Flag || updateFlag) {
        logger.info("The flag for this update should set to true");
        nrEcidMeasSupervisionTimer_Update = true;
    }

    
    if(nrEcidMeasSupervisionTimer_Update) {
        logger.info("Condition met for updating");
        if(current_nrEcidMeasSupervisionTimer != needed_nrEcidMeasSupervisionTimer) {
            logger.info("---> INFO 1C: As conditional requirement for actUlNrEcIdPosMethod being activated, nrEcidMeasSupervisionTimer must be configured per parameter relationship");
            addRecommendation("UPDATE", nrbts, "Configuring nrEcidMeasSupervisionTimer as required by parameter relationship", "nrEcidMeasSupervisionTimer", needed_nrEcidMeasSupervisionTimer);
            logger.info("---> UPDATE COMPLETE");
        }

        else {
            logger.info("The current_nrEcidMeasSupervisionTimer is equal the to the needed_nrEcidMeasSupervisionTimer");
        }
    }
    logger.info("---> INFO 1D: END OF NRBTS UPDATES");
}
logger.info(" ************** Total number of evaluated objects: NRTBS: " + number_of_nrtbs);
logger.info(" **** End of " + cpr_name);