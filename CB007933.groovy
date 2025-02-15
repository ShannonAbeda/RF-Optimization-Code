/*
CPR NAME = N_5G_AWS_20/10_UPDATE
Developed by: Shannon Abeda
July 2024

Feature introduces the asymmetric bandwidth configuration between a 30 MHz cell bandwidth in the DL and a 10 MHz cell Bandwidth in the UL for FDD n66
IQ compression needed if required to support more than 1*20Mhz 4T4R NR/LTE cells per 1 CPRI 9.8G link (up to 4* 20Mhz NR/LTE 4T4R cells can be supported by 
1*CPRI 9.8G link with IQC+downsampling enabled).

Feature required to enable 3x30MHz + 2x20MHz on half ABIO. The refinement feature optimizes utilization of L1 processing resources for PRB handling and 
avoid peak throughput degradation seen with legacy PRB pooling (CB009158). Feature requires 24R1 SW Load. 
*/

cpr_name = "N_5G_AWS_20/10_UPDATE";
logger.info(" **** Beginning of " + cpr_name);
def number_of_nrtbs = 0;

for (mrbts in _context.getMos("MRBTS")) {
    //define Mos
    def nrbts = mrbts.getMos("NRBTS");
    def nrcells = nrbts.getMos("NRCELL");
    def nrcell_FDDs = nrcells.getMos("NRCELL_FDD");


    def mrbtsname  = mrbts.getParameter("name");
	def gNodeBID = mrbts.getParameterAsInteger("originalId");
	def nrbtsname = nrbts.getParameter("name");
	
	def siteID = nrcells.getPhysicalParameter("SITEID");
	def site_region = nrcells.getPhysicalParameter("TERRITORY");
	def site_zone = nrcells.getPhysicalParameter("ZONE");

    logger.info("**********************************************************************************************************************************************************************************************");
	logger.info("---> INFO: Here is the Evaluated mrbtsname: '"+mrbtsname +" and the gNodeBID: "+gNodeBID + " and the site_region, site_zone and nrbtsname: "+site_region + " ; "+ site_zone+ " ; "+nrbtsname);
	logger.info("**********************************************************************************************************************************************************************************************");

    if(nrcells.size() != 0) {
        for(nrcell in nrcells) {
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("--> We will now do verification on NRCELL parameters first");
            def current_pMax = nrcell.getParameterAsInteger("pMax");
            current_Nrcellindex = nrcell.getParameterAsInteger("originalId");
            def current_actShortBsrLimitAdaptation = nrcell.getParameterAsInteger("actShortBsrLimitAdaptation");
            def needed_actShortBsrLimitAdaptation = 1;
    
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO 1A: The current Nrcell Index is: " + current_Nrcellindex);
            
            //Applying pMax Values with Nrcell index greater than 2
            if(current_Nrcellindex >= 2) {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 1B: The index on this cell is greater than or equal to 2 therefore pMax values must be updated to 40")
                if(current_pMax != 40) {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 1C: Updating PMax for NRCELL 1")
                    addRecommendation("UPDATE", nrcell, "Updating Pmax from: " + current_pMax + " to: 40", "pMax", 40);
                }
                
                else {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 1D: No updates are needed");
                }
            }
    
            else {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 1E: The index on this cell is less than 2 therefore pMax values must be updated to 38")
                if(current_pMax != 38) {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 1F: Updating PMax for")
                    addRecommendation("UPDATE", nrcell, "Updating Pmax from: " + current_pMax + " to: 38", "pMax", 38);
                }
    
                else {
                    logger.info("**********************************************************************************************************************************************************************************************");
                    logger.info("---> INFO 1G: No updates are needed");
                }
            }
    
            if(current_actShortBsrLimitAdaptation != needed_actShortBsrLimitAdaptation) {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 1E: current_actShortBsrLimitAdaptation")
                addRecommendation("UPDATE", nrcell, "Updating actShortBsrLimitAdaptation from: " + current_actShortBsrLimitAdaptation + " to: " + needed_actShortBsrLimitAdaptation , "actShortBsrLimitAdaptation", needed_actShortBsrLimitAdaptation);
            }
    
            else {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 1F: current_actShortBsrLimitAdaptation is already set to: " + needed_actShortBsrLimitAdaptation);
            }
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO 1G: End of NRCELL UPDATES");
        } // END OF NRCELL
    }
    

    logger.info("**********************************************************************************************************************************************************************************************");
    logger.info("---> INFO 2A: Now we will check for NRCELL_FDD");
    if(nrcell_FDDs.size() != 0) {
        for(nrcell_FDD in nrcell_FDDs) {
            current_chBwDl = nrcell_FDD.getParameterAsInteger("chBwDl");
            needed_chBwDl = 20;
            current_nrarfcnDl = nrcell_FDD.getParameterAsInteger("nrarfcnDl");
            needed_nrarfcnDl = 436980;
    
            if(current_chBwDl != needed_chBwDl) {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 2B: The current chwBwDl does not equal to needed chwBwDl and we will update accordingly");
                addRecommendation("UPDATE", nrcell_FDD, "Updating chBwDl from: " + current_chBwDl + " to: " + needed_chBwDl, "chBwDl", needed_chBwDl);
            }
            
            else {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 2C: The current chwBwDl equals to needed chwBwDl");
            }
    
            if(current_nrarfcnDl != needed_nrarfcnDl) {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 2D: The current chwBwDl does not equal to needed chwBwDl and we will update accordingly");
                addRecommendation("UPDATE", nrcell_FDD, "Updating nrarfcnDl from: " + current_nrarfcnDl + " to: " + needed_nrarfcnDl, "nrarfcnDl", needed_nrarfcnDl);
            }
    
            else {
                logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("---> INFO 2E: The current nrarfcnDl equals to needed nrarfcnDl");
            }
            logger.info("**********************************************************************************************************************************************************************************************");
            logger.info("---> INFO 2G: End of nrcell_FDD UPDATES");
        }    
    }
}
logger.info(" ************** Total number of evaluated objects: NRTBS: " + number_of_nrtbs);
logger.info(" **** End of " + cpr_name); 