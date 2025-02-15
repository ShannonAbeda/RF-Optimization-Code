/******************************************************************************************************************************************************************************
    Version 1:
 *      * This CPR is to restrict N78 Cells in the protection zone. The rule block any NETACT user to run any changes on N78 cells in the protection zone on any of these parameters NRCELL-pMax","NRCELL-beamSet","NRCELL-ssPbchBlockPower
 *
 *  Version 2: * Enhance the CPR
 *      ? For each MRBTS:
 *      ?       - find all N78 NRCELLS
 *      ?       - if at least one N78 cell is found:
 *      ?           - if the current preventParamModifTargetDN and le list of N78 Cells found are not the same
 *      ?               => UPDATE the preventParamModifTargetDN with the N78 cells found
 *      ?           - if the preventParamModifList is different from ["NRCELL-pMax","NRCELL-beamSet","NRCELL-ssPbchBlockPower"]
 *      ?               => UPDATE the preventParamModifList
 *      ?       - if no N78 cells ahve been found then don't do anything
*******************************************************************************************************************************************************************************/

def cpr_name = "N_N78_FEATCADM_Restriction";
logger.info("Beginning – of CPR: "+ cpr_name );
def number_of_nrbts = 0;



for (nrbts in _context.getMos("NRBTS")){
    number_of_nrbts ++;

    def isUpdated_preventParamModifTargetDN     = false
    def isUpdated_preventParamModifList         = false

    def gNodeBID                        = nrbts.getParameterAsInteger("originalId");             fvvff      C 
    def mrbts                           = nrbts.getMos("MRBTS");
    def mrbtsname                       = mrbts.getParameter("name");

    /* exposing the MO  FEATCADM and the parameter assisiated to it */
    def FeatCadm                        = mrbts.getMos("FEATCADM");
    def current_prevParamModTargetDN    = FeatCadm.getListEnumerationParameter("preventParamModifTargetDN");
    def current_prevParamModList        = FeatCadm.getListEnumerationParameter("preventParamModifList");

    logger.info("_"*200);
    logger.info("---> INFO: Evaluating " + mrbtsname + " an the gNodeBID is: " + gNodeBID);


    logger.info("INFO 1A:  N78 Restricted current NRcells from preventParamModifTargetDN size is: " + current_prevParamModTargetDN.size() + " current_prevParamModTargetDN: " + current_prevParamModTargetDN );
    logger.info("INFO 1B:  N78 Restricted current parameters from preventParamModifList size is: "+ current_prevParamModList.size() + " current_prevParamModList: " + current_prevParamModList );

    def nrcells = nrbts.getMos("NRCELL");
    def needed_prevParamModList = [ "NRCELL-pMax","NRCELL-beamSet","NRCELL-ssPbchBlockPower"]
    def freqBandIndicatorN78List = [];
    def needed_prevParamModTargetDN = [];
    def nrcell_cellname = "";
    def nrcell_distName = "";
    def freqBandIndicatorNR = "";
    def NR_DistName_format = ""

    if (nrcells.size()>0){      // * here onwards looping through each cell of the MRBTS
        for (eachnrcell in nrcells){
            nrcell_cellname                 = eachnrcell.getParameter("cellName");
            nrcell_distName                 = eachnrcell.getParameter("distName");
            NR_DistName_format              = nrcell_distName.substring(10);
            freqBandIndicatorNR             = eachnrcell.getParameterAsInteger("freqBandIndicatorNR");

            logger.info("*"*100);
            logger.info("INFO 2:  The nrcell_cellname is: " + nrcell_cellname + " and the freqBandIndicatorNR:" + freqBandIndicatorNR);
            logger.info("INFO 3:  The freqBandIndicatorNR is: " + freqBandIndicatorNR);

            if (freqBandIndicatorNR!=null){
                if (freqBandIndicatorNR == 78) {
                    logger.info("INFO 4: N78 info nrcell_distName needed: '" + NR_DistName_format + "' and the nrcell_cellname is: '" + nrcell_cellname + "'.")
                    logger.info("INFO 4: The freqBandIndicatorNR is: '" + freqBandIndicatorNR);

                    freqBandIndicatorN78List.add(freqBandIndicatorNR);
                    needed_prevParamModTargetDN.add(NR_DistName_format);

                    logger.info("INFO 5: N78 List of Dis name List is: " + needed_prevParamModTargetDN)
                    logger.info("INFO 5: N78 Bandindicator List is: " + freqBandIndicatorN78List);
                }
                else{
                    logger.info("INFO 6: This cell is not N78 !! nrcell_distName: '" + NR_DistName_format + "' and the nrcell_cellname is: '" + nrcell_cellname + "' and the freqBandIndicatorNR: '" + freqBandIndicatorNR);
                }
            }
        }

        logger.info("INFO 7A:  N78 Restricted current NRcells from preventParamModifTargetDN size is: "+ current_prevParamModTargetDN.size() + " current_prevParamModTargetDN: " + current_prevParamModTargetDN );
        logger.info("INFO 7B:  N78 Restricted Needed NRcells from preventParamModifTargetDN size is: "+ needed_prevParamModTargetDN.size() + " needed_prevParamModTargetDN: " + needed_prevParamModTargetDN);


        logger.info("INFO 8A:  N78 Restricted current Parameters from preventParamModifList size is: "+ current_prevParamModList.size() + " current_prevParamModList: " + current_prevParamModList );

        logger.info("INFO 8B:  N78 Restricted needed Parameters from preventParamModifList size is: " + needed_prevParamModList.size() + " if N78 present in the site, -needed_prevParamModList: " + needed_prevParamModList );

        logger.info("==> INFO: N78 cells found for MRBTS " + mrbtsname + " : " + needed_prevParamModTargetDN);

        // * Filtering out the sites that doesnt have N78 and applying the filter only on sites that have N78
        if (freqBandIndicatorN78List.size()>0) {

            if(current_prevParamModTargetDN.size()!=needed_prevParamModTargetDN.size()){                            // ? Current and Needed Target lists sizes are differents
                logger.info("INFO 9A:UPDATE  featcadm.prevParamModTargetDN, current and needed lists have different sizes" );
                isUpdated_preventParamModifTargetDN = true
                def new_prevParamModTargetDN = createListEnum();
                for(i = 0; i < needed_prevParamModTargetDN.size(); i++){
                    new_prevParamModTargetDN.addEntry(needed_prevParamModTargetDN[i])
                }
                addRecommendation("UPDATE", FeatCadm, "Update featcadm.prevParamModTargetDN from  " + current_prevParamModTargetDN +" to " + new_prevParamModTargetDN ,"preventParamModifTargetDN",new_prevParamModTargetDN);

            } else {                                                                                             // ? Current and Needed Target lists sizes are the same
                for (i = 0; i < needed_prevParamModTargetDN.size(); i++) {
                    if ( ! needed_prevParamModTargetDN.contains(current_prevParamModTargetDN.getItem(i))) {         // ? Current and Needed Target lists are differents
                        logger.info("INFO 9B: UPDATE  featcadm.prevParamModTargetDN, current and needed lists are different" );
                        isUpdated_preventParamModifTargetDN = true
                        def new_prevParamModTargetDN = createListEnum();
                        for(i = 0; i < needed_prevParamModTargetDN.size(); i++){
                            new_prevParamModTargetDN.addEntry(needed_prevParamModTargetDN[i])
                        }
                        addRecommendation("UPDATE", FeatCadm, "Update featcadm.prevParamModTargetDN from  " + current_prevParamModTargetDN +" to " + new_prevParamModTargetDN ,"preventParamModifTargetDN",new_prevParamModTargetDN);
                    }
                }
            }

            if(current_prevParamModList.size()!= needed_prevParamModList.size()){                                   // ? Current and Needed parameter lists sizes are differents
                logger.info("INFO 10A: UPDATE  featcadm.preventParamModifList, current and needed lists have different sizes" );
                isUpdated_preventParamModifList = true
                def new_prevParamModList = createListEnum();
                for(i = 0; i < needed_prevParamModList.size(); i++){
                    new_prevParamModList.addEntry(needed_prevParamModList[i])
                }
                addRecommendation("UPDATE", FeatCadm, "Update featcadm.preventParamModifList from  " + current_prevParamModList + " to " + new_prevParamModList ,"preventParamModifList",new_prevParamModList);

            } else {                                                                                                // ? Current and Needed parameter lists sizes are the same
                for (i = 0; i < needed_prevParamModList.size(); i++){
                    if ( ! needed_prevParamModList.contains(current_prevParamModList.getItem(i))) {                 // ? Current and Needed parameter lists lists are differents
                        logger.info("INFO 10B: UPDATE  featcadm.preventParamModifList, current and needed lists are different" );
                        isUpdated_preventParamModifList = true
                        def new_prevParamModList = createListEnum();
                        for(i = 0; i < needed_prevParamModList.size(); i++){
                            new_prevParamModList.addEntry(needed_prevParamModList[i])
                        }
                        addRecommendation("UPDATE", FeatCadm, "Update featcadm.preventParamModifList from  " + current_prevParamModList + " to " + new_prevParamModList ,"preventParamModifList",new_prevParamModList);

                    }
                }
            }

            if (!isUpdated_preventParamModifTargetDN){
                logger.info("INFO 11A: preventParamModifTargetDN is correct, no update needed.")
            }

            if (!isUpdated_preventParamModifList){
                logger.info("INFO 11B: preventParamModifList is correct, no update needed.")
            }

        } else {
            logger.info("INFO 12: No N78 cells found in " + mrbtsname);
        }
    }
}

logger.info("Total number of evaluated objects: NRBTS " + number_of_nrbts);
logger.info("---> INFO: End of the CPR: "+ cpr_name);