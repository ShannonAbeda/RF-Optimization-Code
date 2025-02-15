/*****************************************************************************************************************************************************************************************
 * ! This CPR manages the NRREL.sCellAdditionMethod parameter which controls the PCELL-SCELL combo and it manages the NRCAIMP/NRCAIMS objects for feature CB007516 NRCA aware IMLB,
 * *    - The list of PCELL-SCELL combo are user defined and it's managed by the array called "pcell_scell_bandComboList", which rely on NR BAND.
 * *        - This has a direct impact on NRREL.sCellAdditionMethod parameter and on the NRCAIMP/NRCAIMS objects
 * *    - IMPORTANT #1 : All the PCELL-SCELL combo must be in the same sector.
 * *    - IMPORTANT #2 : The CPR is very sensitive to cellName because it extracts the sectorID from the cellName. So if it detects that a cellName is not valid, it will stop the current gNB evaluation and it will store the invalid cellName in this array "wrongCellName".
 * *    - IMPORTANT #3 : The CPR will deactivate a PCELL-SCELL combo if it's not needed since the number of configured PCELL allowed and the number of configured SCELL allowed is very limited, so it needs to be controled otherwise it will create Netact error.
 * *    - IMPORTANT #4 : In the code, a gNB is considered "NR CA" if at least one valid PCELL-SCELL combo was found.
 * *    - IMPORTANT #5 : The CPR only verify ABIO baseband.
 * *    - IMPORTANT #6 : The CPR only verify >=22R1 software.
 * *    - IMPORTANT #6 : The CPR only verify gNB with gNbCuType == SA or gNbCuType == SA+NSA.
 *
 * * This CPR also has the possibility to activate/deactivate NR CA for NSA users.
 * ?    - It's user defined and it's managed by the variable called "NRCA_FOR_NSA_USERS_IS_ALLOWED"
 *
 * * If NR CA is needed, this CPR also manages other CA parameters, such as :
 * ?    - NRBTS.maxNumScellsDl:
 * ?        - The paramater is user defined and it's managed by the variable called "MAXNUMSCELLSDL"
 * ?    - NRBTS.actMeasBasedSCellAddition = 1
 * ?    - NRBTS.actMeasBasedSCellAdditionNsa:
 * ?        - This parameter will be 1 is NR CA is allowed for NSA users
 * ?        - This parameter will be 0 is NR CA is not allowed for NSA users
 * ?    - NRDU.actInterNumerologyCa = 1
 * ?    - NRDU.actConnectedModeMobilityCA = 1
 * ?    - NRDU.flexibleCAMode = 2
 * ?    - NRDU.carrierAggregationSupportList (complex listenumeration):
 * ?        - This is where it allows NRCA for SA and/or NSA users
 * ?        - This is where it allows different type of NRCA (FDD-FDD, FDD-TDD, TDD-TDD, TDD-FDD)
 * ?    - NRCELL.reqMinNumPuschPrb:
 * ?        - It updates this parameter only in a specific situation where CA is not activated (NRDU.actInterNumerologyCa=0) and it will be activated (NRDU.actInterNumerologyCa=1). Otherwize, it's managed by simple rule.
 * ?    - NRCELL.a1MeasSCellAddEnabled = 1
 * ?    - NRCELL.a2MeasSCellRelEnabled = 1
 *
 *
 *
 * ! PROBLEM #1 : if CA is not activated at all and it needs to be activated(NRDU.actInterNumerologyCa from 0 to 1), we need to change NRCELL.reqMinNumPuschPrb which is a complex parameter, and it's already managed by a complex simple rule
 * * SOLUTION : This CPR is updating NRCELL.reqMinNumPuschPrb only if CA is needed and only if it's higher than the maxmimum value allowed for CA gNB
 * ? POSSIBLE PROBLEM #1.5 : this CPR should not be in the same CC task as the NRCELL.reqMinNumPuschPrb simple rule to avoid conflict
 *
 *
 * * NOTE #1:   The CPR consider that N78 with the highest BW is considered N78-F1. If both N78 carrier have same BW, the one with the lowest ssbFreq is considered N78-F1.
 * *            But this method only work in GTA where we have N78 60MHz and N78 20MHz. What happened if we have an area with N78 50MHz and N78 40MHz ?
 * *            Wouldn't we want to have the N78 40MHz to have NRCA also ?
 * *            It would be easier to do NRCA on any N78 if the BW is higher than a minimum BW requirement, like that:
 * *            If cell is N78 and BW is higher than 20MHz, we allow NR CA on it
 * *            By doing that, we'll avoid having to differenciate both N78 as N78-F1 / N78-F2, which will simplify a lot the logic.
 *
 *
 * * VERSION 2 :
 * ?    - CPR now support only software >= 22R2
 * ?    - Adding more option to NRDU.carrierAggregationSupportList to support 3CC
 * ?        - now support the values 600 and 700
 *
 * * Version 3:
 *
 * * VERSION 4: To Support 4CC
 * ?    - Updating Below parameter:
 * ?        - NRBTS.maxNumScellsDl=3
 * ?        - NRCELL.maxNumScellsDlFr1=3; //4CC parameter
 * ?    - Added a condition to remove actK1OptForDPA from version 23R1 onwards
 *
 *
 * * VERSION 5: To Support N25 MultiBand
 *
 * * Version 6: Corrected
 * ?    - Object identification id of object class NRREL : minvalue: 1 maxvalue: 256 step: 1
 * ?    - NRRELMAXSIZE = 256
 * ?    - def new_nrrel = buildMo("NRREL", nrcell, "1-"+(NRRELMAXSIZE-1));
 *
 *
 * * Version 7:
 * ?    - carrierAggregationSupportList condition modification to support if the site have only one TDD frequency
 * ?        - CA_FR1TDD_SA = "400";
 * ?        - CA_FR1TDD_NSA = "401";
 * ?        - CA_FR1TDD_ANY_SA = "700";
 * ?    - Added NRREL.sCellOperationalMode =dlOnly if NRREL.sCellAdditionMethod = COVBASEDA1 added NRCELL.actK1Opt = 1
 *
 * * Version 8:
 * ?    - if all N25 cells are locked, NRCA is configured normally except we don't create any NRCAIMP and NRCAIMS that imply N25
 *
 *
 ****************************************************************************************************************************************************************************************/





//                                  PCELL        SCELL(s)
def pcell_scell_bandComboList = [   ["71",      ["66","78-F1"]                  ],
                                    ["5",       ["66","78-F1"]                  ],
                                    ["25",      ["71","5","66","78-F1","78-F2"] ],
                                    ["66",      ["71","5","25","78-F1","78-F2"] ],
                                    ["78-F1",   ["71","5","25","66","78-F2"]    ]
                                ];

def NRCA_FOR_NSA_USERS_IS_ALLOWED = true;
def MAXNUMSCELLSDL                = "3";
def MAXNUMSCELLSDLFR1             = "3";

// ? possible choice : CELLNAME, TOPOLOGY_SECTORNAME. by default, it'll use CELLNAME.
def SECTORID_METHOD = "CELLNAME";

// ? these values were extracted from the 22R1 CM documentation for the parameter NRCELL.reqMinNumPuschPrb
def mapping_cellTechnology_bw_scs           =   ["0_5_15", "0_10_15", "0_15_15", "0_20_15", "0_30_15", "1_20_30", "1_30_30", "1_40_30", "1_50_30", "1_60_30", "1_70_30", "1_80_30", "1_90_30", "1_100_30"   ];
def mapping_max_reqMinNumPuschPrb_withCA    =   [15,       42,        69,        96,        150,        41,         68,         90,     113,        142,        169,        197,      225,          253     ];

// ? these values were extracted from the 22R1 CM documentation for the parameter NRREL.sCellAdditionMethod
def MAX_NUMBER_OF_FDDtoFDDcombo_HAVING_SAME_PCELL = 2;
def MAX_NUMBER_OF_FDDtoFDDcombo_HAVING_SAME_SCELL = 2;
def MAX_NUMBER_OF_FDDtoTDDcombo_HAVING_SAME_PCELL = 2;
def MAX_NUMBER_OF_FDDtoTDDcombo_HAVING_SAME_SCELL = 3;
def MAX_NUMBER_OF_TDDtoTDDcombo_HAVING_SAME_PCELL = 1;
def MAX_NUMBER_OF_TDDtoTDDcombo_HAVING_SAME_SCELL = 1;
def MAX_NUMBER_OF_TDDtoFDDcombo_HAVING_SAME_PCELL = 3;
def MAX_NUMBER_OF_TDDtoFDDcombo_HAVING_SAME_SCELL = 2;






def cpr_name = "N_5G_CA_Manage";
logger.info(" **** Beginning of " + cpr_name);
logger.info(" **** The pcell_scell_bandComboList is " + pcell_scell_bandComboList);

def number_of_nrbts                     = 0     ;
def NRRELMAXSIZE                        = 256   ;
def wrongCellName                       = []    ;
def listofcellwithtoomanyNRREL          = []    ;
def listofunconfiguredscelltoomanyNRREL = []    ;
def UNLOCKED                            = "2"   ;
def FALSE                               = "0"   ;
def TRUE                                = "1"   ;
def LONGPUCCH                           = "1"   ;
def ON                                  = "1"   ;
def OFF                                 = "0"   ;
def FDD                                 = "0"   ;
def TDD                                 = "1"   ;
def FDD_FDD                             = "0_0" ;
def FDD_TDD                             = "0_1" ;
def TDD_TDD                             = "1_1" ;
def TDD_FDD                             = "1_0" ;
def NSA                                 = "0"   ;
def SA                                  = "1"   ;
def SAplusNSA                           = "10"  ;
def BLIND                               = "0"   ;
def COVBASEDA1                          = "1"   ;
def BASICCAMODE                         = "0"   ;
def MIXEDCAMODE                         = "1"   ;
def CONFIGURATIONINDEPENDENT            = "2"   ;
def ACCEPTUNKNOWNBANDCOMBINATIONS       = "1"   ;
def NONE                                = "0"   ;
def CA_FDD_FDD                          = "3"   ;
def CA_FDD_FR1TDD_SA                    = "200" ;
def CA_FDD_FR1TDD_NSA                   = "201" ;
def CA_FR1TDD_SA                        = "400" ;
def CA_FR1TDD_NSA                       = "401" ;
def CA_FR1TDD_FDD_SA                    = "500" ;
def CA_FR1TDD_FDD_NSA                   = "501" ;
def CA_FDD_ANY_SA                       = "600" ;
def CA_FR1TDD_ANY_SA                    = "700" ;
def NONCAAWARE                          = "0"   ;
def CAAWARE                             = "1"   ;
def cellwithunknownSSBFreq              = []    ;

def pcell_bandList = [];
for(i = 0; i < pcell_scell_bandComboList.size(); i++) {
    pcell_bandList.add(pcell_scell_bandComboList[i][0]);
}

def OK_TO_START = true;
if(mapping_cellTechnology_bw_scs.size() != mapping_max_reqMinNumPuschPrb_withCA.size()) {
    OK_TO_START = false;
}

if(OK_TO_START) {
    for(nrbts in _context.getMos("NRBTS")) {
        number_of_nrbts++;
        def CA_IS_NEEDED_ON_GNB = false;
        def nrdu                = nrbts.getMos("NRDU");
        def nrcells             = nrbts.getMos("NRCELL");
        def bbmodrs             = nrbts.getMos("BBMOD_R");
        def gNbCuType           = nrbts.getParameter("gNbCuType");
        def nrbtsDistname       = nrbts.getParameter("distName").substring(10);
        def Integer versionInt  = 0;

        // ? Version Parsing
        def versionRealString   = nrbts.getParameter("version");                        // * Here, versionRealString should look like NRBTSCL5G21B_2105_200 or NRBTSCL22R1_2109_102
        if(versionRealString != null) {
            versionString = versionRealString.split("_")[0];                            // * Here, versionString should look like NRBTSCL5G21B or NRBTSCL22R1
            if(versionString.substring(versionString.size()-2, versionString.size()-1) == "R") {
                versionString = versionString.substring(versionString.size()-4);        // * Here, versionString should look like 22R1
            }
            else {
                versionString = versionString.substring(versionString.size()-3);        // * Here, versionString should look like 21B
            }
            versionInt = Integer.parseInt(versionString.substring(0,2));                // * Here, versionInt should look like 21 or 22
        }

        // ? Loop through all the BBMOD_R to get the all the productName
        productNameList = [];
        for(bbmodr in bbmodrs) {
            productNameList.add(bbmodr.getParameter("productName"));
        }

        logger.info(" ********** We are evaluating this gNB " + nrbtsDistname + " on this 5G version : " + versionRealString + " with " + nrcells.size() + " NRCELL(s) and " + nrdu.size() + " NRDU(s) and here is the list of BBMOD productName : " + productNameList);

        if(nrcells.size() != 0 && nrdu.size() == 1) {
            if(versionInt >= 22 && versionString != "22R1") {  // ! Starting software is 22R2
                if(gNbCuType == SA || gNbCuType == SAplusNSA) {
                    if(productNameList.contains("ABIO AirScale Capacity")) {
                        /*****************************************************************************************************************/
                        /************ STEP#1a Loop through all the NRCELLs to build reference tables to help with CA decision ************/
                        /*****************************************************************************************************************/
                        logger.info(" ************* STEP#1a Loop through all the NRCELLs to build reference tables to help with CA decision");
                        def cellReferenceTable_distName                         = [];
                        def cellReferenceTable_lcrId                            = [];
                        def cellReferenceTable_gNBID_lcrId                      = [];
                        def cellReferenceTable_status                           = [];
                        def cellReferenceTable_cellTechnology                   = [];
                        def cellReferenceTable_cellDepType                      = [];
                        def cellReferenceTable_freqBandIndicatorNR              = [];
                        def cellReferenceTable_actBeamforming                   = [];
                        def cellReferenceTable_ssbFrequency                     = [];
                        def cellReferenceTable_nrarfcn                          = [];
                        def cellReferenceTable_gscn                             = [];
                        def cellReferenceTable_bw                               = [];
                        def cellReferenceTable_scs                              = [];
                        def cellReferenceTable_cellTechnology_bw_scs            = [];
                        def cellReferenceTable_max_reqMinNumPuschPrb_withCA     = [];
                        def cellReferenceTable_sectorID                         = [];
                        def cellReferenceTable_sectorIDint_fromcellName         = [];
                        def cellReferenceTable_sectorIDstr_fromtopology         = [];
                        def cellReferenceTable_pucchModeSelect                  = [];
                        def cellReferenceTable_actK1OptForDPA                   = [];
                        def cellReferenceTable_actK1Opt                         = [];
                        def cellReferenceTable_csiReportPeriodicity             = [];
                        def cellReferenceTable_csirsTrackingPeriod              = [];
                        def cellReferenceTable_numberoftime_FDDtoFDDcombo_pcell = [];
                        def cellReferenceTable_numberoftime_FDDtoFDDcombo_scell = [];
                        def cellReferenceTable_numberoftime_FDDtoTDDcombo_pcell = [];
                        def cellReferenceTable_numberoftime_FDDtoTDDcombo_scell = [];
                        def cellReferenceTable_numberoftime_TDDtoTDDcombo_pcell = [];
                        def cellReferenceTable_numberoftime_TDDtoTDDcombo_scell = [];
                        def cellReferenceTable_numberoftime_TDDtoFDDcombo_pcell = [];
                        def cellReferenceTable_numberoftime_TDDtoFDDcombo_scell = [];
                        def cellReferenceTable_listofpcell                      = [];
                        def cellReferenceTable_wttpind                          = [];
                        def cellReferenceTable_cellBarred                       = []
                        def N78_ssbFreq_list                                    = [];
                        def N78_bw_list                                         = [];
                        def UNKNOWN_SSBFREQ                                     = false;


                        for(nrcell in nrcells) {
                            if(nrcell.getParameter("cellTechnology") == FDD) {
                                nrarfcn_integer = nrcell.getMos("NRCELL_FDD").getParameterAsInteger("nrarfcnDl");
                                bw_integer      = nrcell.getMos("NRCELL_FDD").getParameterAsInteger("chBwDl");
                            } else {
                                nrarfcn_integer = nrcell.getParameterAsInteger("nrarfcn");
                                bw_integer      = nrcell.getParameterAsInteger("chBw");
                            }
                            gscn_integer = nrcell.getParameterAsInteger("gscnOrSsPbchArfcn");
                            arfcnSsbPbch = nrcell.getParameter("arfcnSsbPbch");
                            logger.info("We are analysing the NRCELL cellName : " + nrcell.getParameter("cellName") + " with the arfcnSsbPbch : " + arfcnSsbPbch + " with the gscn_integer : " + gscn_integer +  " with the nrarfcn_integer : " + nrarfcn_integer);

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
                                        } else {
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
                                    } else {
                                        ssbfreq_integer = Integer.parseInt(arfcnSsbPbch);
                                        logger.info("SSBFREQ with parameter : ssbfreq_integer = " + arfcnSsbPbch);
                                    }
                                    cellReferenceTable_ssbFrequency             .add(ssbfreq_integer.toString());
                                    cellReferenceTable_distName                 .add(nrcell.getParameter("distName").substring(10));
                                    cellReferenceTable_lcrId                    .add(nrcell.getParameter("lcrId"));
                                    cellReferenceTable_gNBID_lcrId              .add(nrbts.getParameter("originalId")+"_"+nrcell.getParameter("lcrId"));
                                    cellReferenceTable_cellTechnology           .add(nrcell.getParameter("cellTechnology"));
                                    cellReferenceTable_cellDepType              .add(nrcell.getParameter("cellDepType"));
                                    cellReferenceTable_freqBandIndicatorNR      .add(nrcell.getParameter("freqBandIndicatorNR"));
                                    cellReferenceTable_scs                      .add(nrcell.getParameter("ssbScs"));
                                    cellReferenceTable_nrarfcn                  .add(nrarfcn_integer.toString());
                                    cellReferenceTable_bw                       .add(bw_integer.toString());
                                    cellReferenceTable_gscn                     .add(gscn_integer.toString());
                                    cellReferenceTable_cellBarred               .add(nrcell.getParameterAsInteger("cellBarred"));
                                    cellReferenceTable_cellTechnology_bw_scs    .add(temp_cellTechnology_bw_scs);

                                    temp_cellTechnology_bw_scs                  = nrcell.getParameter("cellTechnology")+"_"+bw_integer.toString()+"_"+nrcell.getParameter("ssbScs");
                                    if(mapping_cellTechnology_bw_scs.contains(temp_cellTechnology_bw_scs)) {
                                        cellReferenceTable_max_reqMinNumPuschPrb_withCA.add(mapping_max_reqMinNumPuschPrb_withCA[mapping_cellTechnology_bw_scs.indexOf(temp_cellTechnology_bw_scs)]);
                                    }

                                    cellReferenceTable_actBeamforming           .add(nrcell.getParameter("actBeamforming"));
                                    cellReferenceTable_pucchModeSelect          .add(nrcell.getParameter("pucchModeSelect"));
                                    cellReferenceTable_actK1OptForDPA           .add(nrcell.getParameter("actK1OptForDPA"));
                                    cellReferenceTable_actK1Opt                 .add(nrcell.getParameter("actK1Opt"));
                                    cellReferenceTable_csiReportPeriodicity     .add(nrcell.getListParameter("periodicCsiAcquisition")[0]["csiReportPeriodicity"]);
                                    cellReferenceTable_csirsTrackingPeriod      .add(nrcell.getListParameter("csirsForTracking")[0]["csirsTrackingPeriod"]);

                                    cellName = nrcell.getParameter("cellName");
                                    if(cellName.size() >= 34) {
                                        if(cellName.split("-").size() >= 6) {
                                            cellReferenceTable_sectorIDint_fromcellName.add(Integer.parseInt(cellName.split("-")[5].split("_")[0]));
                                        } else {
                                            logger.info(" ************* WARNING : The cellName has less than 5 '-' symbol.. something is wrong with the cellName " + cellName);
                                            wrongCellName.add(cellName);
                                        }
                                    } else {
                                        logger.info(" ************* WARNING : The cellName length is lower than 34 characters which is not standard.. something is wrong with the cellName " + cellName);
                                        wrongCellName.add(cellName);
                                    }

                                    if(nrcell.getPhysicalParameter("SECTOR_NAME") != null) {
                                        cellReferenceTable_sectorIDstr_fromtopology.add(nrcell.getPhysicalParameter("SECTOR_NAME"));
                                    }

                                    if(nrcell.getParameter("administrativeState") == UNLOCKED) {
                                        cellReferenceTable_status.add(ON);
                                    } else {
                                        cellReferenceTable_status.add(OFF);
                                    }
                                    cellReferenceTable_numberoftime_FDDtoFDDcombo_pcell.add(0);
                                    cellReferenceTable_numberoftime_FDDtoFDDcombo_scell.add(0);
                                    cellReferenceTable_numberoftime_FDDtoTDDcombo_pcell.add(0);
                                    cellReferenceTable_numberoftime_FDDtoTDDcombo_scell.add(0);
                                    cellReferenceTable_numberoftime_TDDtoTDDcombo_pcell.add(0);
                                    cellReferenceTable_numberoftime_TDDtoTDDcombo_scell.add(0);
                                    cellReferenceTable_numberoftime_TDDtoFDDcombo_pcell.add(0);
                                    cellReferenceTable_numberoftime_TDDtoFDDcombo_scell.add(0);
                                    cellReferenceTable_listofpcell.add([]);

                                    if(nrcell.getPhysicalParameter("WTTP_IND") == null) {
                                        cellReferenceTable_wttpind.add("NO");
                                    } else {
                                        cellReferenceTable_wttpind.add(nrcell.getPhysicalParameter("WTTP_IND"));
                                    }

                                    // ? Build list of unique N78 ssbFreq
                                    if(nrcell.getParameter("freqBandIndicatorNR") == "78") {
                                        if(!N78_ssbFreq_list.contains(ssbfreq_integer)) {
                                            N78_ssbFreq_list.add(ssbfreq_integer);
                                            N78_bw_list.add(bw_integer);
                                        }
                                    }
                            } else {
                                logger.info("ERROR : This nrcell we can't query the SSBFREQ: " + nrcell.getParameter("distName").substring(10));
                                UNKNOWN_SSBFREQ = true;
                                cellwithunknownSSBFreq.add(nrcell.getParameter("distName").substring(10));
                            }
                        }

                        if(!UNKNOWN_SSBFREQ) {
                            // * N78 differenciation : N78-F1 is the highest BW. If both N78 have similar BW, N78-F1 will be the lowest ssbFreq
                            logger.info(" ************* BEFORE : cellReferenceTable_freqBandIndicatorNR : " + cellReferenceTable_freqBandIndicatorNR);
                            if(N78_ssbFreq_list.size() != 0) {
                                if(N78_ssbFreq_list.size() == 1) {                              // ? If only one N78 carrier
                                    N78_F1_ssbFreq = N78_ssbFreq_list[0].toString();
                                } else {                                                        // ? More than 1 N78 carrier
                                    if(N78_bw_list[0] > N78_bw_list[1]) {                           // ? If the first carrier has the highest BW
                                        N78_F1_ssbFreq = N78_ssbFreq_list[0].toString();
                                    } else if(N78_bw_list[1] > N78_bw_list[0]) {                    // ? If the second carrier has the highest BW
                                        N78_F1_ssbFreq = N78_ssbFreq_list[1].toString();
                                    } else {                                                        // ? If the both carriers have similar BW
                                        if(N78_ssbFreq_list[0] < N78_ssbFreq_list[1]) {                 // ? If the first carrier has the lowest ssbFreq
                                            N78_F1_ssbFreq = N78_ssbFreq_list[0].toString();
                                        } else {                                                        // ? If the second carrier has the lowest ssbFreq
                                            N78_F1_ssbFreq = N78_ssbFreq_list[1].toString();
                                        }
                                    }
                                }

                                logger.info(" *** N78_ssbFreq_list is : " + N78_ssbFreq_list);
                                logger.info(" *** N78_bw_list is : " + N78_bw_list);
                                logger.info(" *** N78_F1_ssbFreq is : " + N78_F1_ssbFreq);
                                for(i = 0 ; i < cellReferenceTable_gNBID_lcrId .size(); i++) {
                                    if(cellReferenceTable_freqBandIndicatorNR[i] == "78") {
                                        if(cellReferenceTable_ssbFrequency[i] == N78_F1_ssbFreq) {
                                            cellReferenceTable_freqBandIndicatorNR[i] = "78-F1";
                                        } else {
                                            cellReferenceTable_freqBandIndicatorNR[i] = "78-F2";
                                        }
                                    }
                                }
                            }
                            logger.info(" ************* AFTER : cellReferenceTable_freqBandIndicatorNR : " + cellReferenceTable_freqBandIndicatorNR);

                            if(SECTORID_METHOD == "CELLNAME") {
                                cellReferenceTable_sectorID = cellReferenceTable_sectorIDint_fromcellName;
                                logger.info(" ************* THE CPR IS USING THE 'CELLNAME' (USER DEFINED) TO QUERY THE SECTORID.");
                            } else if(SECTORID_METHOD == "TOPOLOGY_SECTORNAME") {
                                cellReferenceTable_sectorID = cellReferenceTable_sectorIDstr_fromtopology;
                                logger.info(" ************* THE CPR IS USING THE 'TOPOLOGY_SECTORNAME' (USER DEFINED) TO QUERY THE SECTORID.");
                            } else {
                                cellReferenceTable_sectorID = cellReferenceTable_sectorIDint_fromcellName;
                                logger.info(" ************* THE CPR IS USING THE 'CELLNAME' (DEFAULT) TO QUERY THE SECTORID.");
                            }

                            logger.info(" ************* cellReferenceTable_lcrId                            : " + cellReferenceTable_lcrId                          );
                            logger.info(" ************* cellReferenceTable_gNBID_lcrId                      : " + cellReferenceTable_gNBID_lcrId                    );
                            logger.info(" ************* cellReferenceTable_distName                         : " + cellReferenceTable_distName                       );
                            logger.info(" ************* cellReferenceTable_status                           : " + cellReferenceTable_status                         );
                            logger.info(" ************* cellReferenceTable_cellBarred                       : " + cellReferenceTable_cellBarred                     );
                            logger.info(" ************* cellReferenceTable_cellTechnology                   : " + cellReferenceTable_cellTechnology                 );
                            logger.info(" ************* cellReferenceTable_cellDepType                      : " + cellReferenceTable_cellDepType                    );
                            logger.info(" ************* cellReferenceTable_freqBandIndicatorNR              : " + cellReferenceTable_freqBandIndicatorNR            );
                            logger.info(" ************* cellReferenceTable_ssbFrequency                     : " + cellReferenceTable_ssbFrequency                   );
                            logger.info(" ************* cellReferenceTable_nrarfcn                          : " + cellReferenceTable_nrarfcn                        );
                            logger.info(" ************* cellReferenceTable_gscn                             : " + cellReferenceTable_gscn                           );
                            logger.info(" ************* cellReferenceTable_bw                               : " + cellReferenceTable_bw                             );
                            logger.info(" ************* cellReferenceTable_scs                              : " + cellReferenceTable_scs                            );
                            logger.info(" ************* cellReferenceTable_wttpind                          : " + cellReferenceTable_wttpind                        );
                            logger.info(" ************* cellReferenceTable_sectorIDint_fromcellName         : " + cellReferenceTable_sectorIDint_fromcellName       );
                            logger.info(" ************* cellReferenceTable_sectorIDstr_fromtopology         : " + cellReferenceTable_sectorIDstr_fromtopology       );
                            logger.info(" ************* cellReferenceTable_sectorID                         : " + cellReferenceTable_sectorID                       );
                            logger.info(" ************* cellReferenceTable_actBeamforming                   : " + cellReferenceTable_actBeamforming                 );
                            logger.info(" ************* cellReferenceTable_pucchModeSelect                  : " + cellReferenceTable_pucchModeSelect                );
                            logger.info(" ************* cellReferenceTable_actK1OptForDPA                   : " + cellReferenceTable_actK1OptForDPA                 );
                            logger.info(" ************* cellReferenceTable_csiReportPeriodicity             : " + cellReferenceTable_csiReportPeriodicity           );
                            logger.info(" ************* cellReferenceTable_csirsTrackingPeriod              : " + cellReferenceTable_csirsTrackingPeriod            );
                            logger.info(" ************* cellReferenceTable_cellTechnology_bw_scs            : " + cellReferenceTable_cellTechnology_bw_scs          );
                            logger.info(" ************* cellReferenceTable_max_reqMinNumPuschPrb_withCA     : " + cellReferenceTable_max_reqMinNumPuschPrb_withCA   );

                            if(cellReferenceTable_gNBID_lcrId.size() == cellReferenceTable_max_reqMinNumPuschPrb_withCA.size()) {
                                logger.info(" ************* The size of 'cellReferenceTable_max_reqMinNumPuschPrb_withCA' is OK");
                                if(cellReferenceTable_gNBID_lcrId.size() == cellReferenceTable_sectorID.size()) {
                                    logger.info(" ************* The size of 'cellReferenceTable_sectorID' is OK");

                                    // ? If there is one N25 cell that is ON AIR, we create NRCAIMP and NRCAIMS
                                    flagN25NRCAIMPandNRCAIMS = false;
                                    for(i = 0 ; i < cellReferenceTable_gNBID_lcrId .size(); i++) {
                                        if(cellReferenceTable_freqBandIndicatorNR[i] == "25" && cellReferenceTable_status[i] == "1") {
                                            flagN25NRCAIMPandNRCAIMS = true
                                            break;
                                        }
                                    }
                                    logger.info(" Special_11 ******** N25 NRCAIMP NRCAIMS Flag status: " + flagN25NRCAIMPandNRCAIMS);
                                    /************************************************************************************************************************************************/
                                    /************ STEP#1b Loop through all the cellReferenceTable and for every valid pcell, create a list of valid scells (gNBID_lcrId) ************/
                                    /************************************************************************************************************************************************/
                                    logger.info(" ************* STEP#1b Loop through all the cellReferenceTable and for every valid pcell, create a list of valid scells (gNBID_lcrId)");
                                    cellReferenceTable_listofscell                                  = [];
                                    cellReferenceTable_pscellscell_cellTechnology_uniquecombination = [];
                                    evaluated_pcell_scell_ssbFrequencyCombo                         = [];
                                    neededNRCAIMPssbFreqList                                        = [];
                                    neededNRCAIMSssbFreqList                                        = [];

                                    // ? First loop in the cellReferenceTable (INFO1) to find a potential PCELL
                                    logger.info(" ******************* INFO1 : Here is the list of allowed pcellBands : " + pcell_bandList);
                                    for(i = 0 ; i < cellReferenceTable_gNBID_lcrId .size(); i++) {
                                        tempScellList = [];
                                        logger.info(" ********************* INFO1 : Evaluating to see if this cell:" + cellReferenceTable_gNBID_lcrId[i] + " is a potential pcell");
                                        // ! PCELL and SCELL general constraints
                                        // ? Verify if the band is allowed to be a PCELL (User defined table)
                                        if(pcell_bandList.contains(cellReferenceTable_freqBandIndicatorNR[i])) {
                                            logger.info(" ***************************** INFO1 : GOOD ! This band (value=" + cellReferenceTable_freqBandIndicatorNR[i] + ") is an allowed pcellBand (value=" + pcell_bandList + ")");

                                            // ? Verify if PCELL has LONGPUCCH activated
                                            if(cellReferenceTable_pucchModeSelect[i] == LONGPUCCH) {
                                                scell_bandList = pcell_scell_bandComboList[pcell_bandList.indexOf(cellReferenceTable_freqBandIndicatorNR[i])][1];
                                                logger.info(" ***************************** INFO1 : PERFECT ! This PCELL has LONGPUCCH (value=" + cellReferenceTable_pucchModeSelect[i] + "). Here are its allowed scell_bandList : " + scell_bandList);

                                                // ? Second loop in the cellReferenceTable (INFO2) to find a potential SCELL
                                                for(j = 0 ; j < cellReferenceTable_gNBID_lcrId .size(); j++) {
                                                    logger.info(" ***************************** INFO2 : Evaluating to see if this cell:" + cellReferenceTable_gNBID_lcrId[j] + " is a potential scell");

                                                    // ? Verify if the band is allowed to be a SCELL
                                                    if(scell_bandList.contains(cellReferenceTable_freqBandIndicatorNR[j])) {
                                                        logger.info(" ************************************* INFO2 : GOOD ! This band (value=" + cellReferenceTable_freqBandIndicatorNR[j] + ") is an allowed scellBand (value=" + scell_bandList + ")");

                                                        // ? Verify if the SCELL nrarfcn is not similar from the PCELL nrarfcn
                                                        if(cellReferenceTable_nrarfcn[j] != cellReferenceTable_nrarfcn[i]) {
                                                            logger.info(" ************************************* INFO2 : GOOD ! The SCELL nrarfcn (value=" + cellReferenceTable_nrarfcn[j] + ") is not similar to the PCELL nrarfcn (value=" + cellReferenceTable_nrarfcn[i] + ")");

                                                            // ? Verify if the SCELL sectorID is similar to the PCELL sectorID
                                                            if(cellReferenceTable_sectorID[j] == cellReferenceTable_sectorID[i]) {
                                                                logger.info(" ************************************* INFO2 : GOOD ! The SCELL sectorID (value=" + cellReferenceTable_sectorID[j] + ") is similar to the PCELL sectorID (value=" + cellReferenceTable_sectorID[i] + ")");

                                                                // ? Verify if the SCELL celldeptype is compatible with the PCELL celldeptype
                                                                if( (cellReferenceTable_cellDepType[j] == SAplusNSA)                                        ||
                                                                    (cellReferenceTable_cellDepType[j] == NSA && cellReferenceTable_cellDepType[i] != SA)   ||
                                                                    (cellReferenceTable_cellDepType[j] == SA && cellReferenceTable_cellDepType[i] != NSA)   ){
                                                                        temp_pscellscell_cellTechnology_combination = cellReferenceTable_cellTechnology[i] + "_" + cellReferenceTable_cellTechnology[j];
                                                                        logger.info(" ************************************* INFO2 : GOOD ! The SCELL celldeptype (value=" + cellReferenceTable_cellDepType[j] + ") is compatible with the PCELL celldeptype (value=" + cellReferenceTable_cellDepType[i] + "). We'll now analyse the cellTechnology combination between the PCELL and SCELL, which is " + temp_pscellscell_cellTechnology_combination);

                                                                        // ! PCELL and SCELL cellTechnology constraints
                                                                        CELLTECHNOLOGY_CONSTRAINTS_ARE_VALID = false;
                                                                        if(temp_pscellscell_cellTechnology_combination == FDD_FDD) {
                                                                            // ? Verify if the FDD SCELL csiReportPeriodicity is similar to the FDD PCELL csiReportPeriodicity
                                                                            if(cellReferenceTable_csiReportPeriodicity[j] == cellReferenceTable_csiReportPeriodicity[i]) {
                                                                                logger.info(" *************************************************** INFO2 : PERFECT ! The FDD SCELL csiReportPeriodicity (value=" + cellReferenceTable_csiReportPeriodicity[j] + ") is similar to the FDD PCELL csiReportPeriodicity (value=" + cellReferenceTable_csiReportPeriodicity[i] + "). We'll add it to the list of scell !!");
                                                                                cellReferenceTable_numberoftime_FDDtoFDDcombo_pcell[i] = cellReferenceTable_numberoftime_FDDtoFDDcombo_pcell[i] + 1;
                                                                                cellReferenceTable_numberoftime_FDDtoFDDcombo_scell[j] = cellReferenceTable_numberoftime_FDDtoFDDcombo_scell[j] + 1;
                                                                                CELLTECHNOLOGY_CONSTRAINTS_ARE_VALID = true;
                                                                            } else {
                                                                                logger.info(" ************************************* INFO2 : TOOBAD ! The FDD SCELL csiReportPeriodicity (value=" + cellReferenceTable_csiReportPeriodicity[j] + ") is not similar to the FDD PCELL csiReportPeriodicity (value=" + cellReferenceTable_csiReportPeriodicity[i] + ").. so we won't add it to the list of scell");
                                                                            }
                                                                        }

                                                                        if(temp_pscellscell_cellTechnology_combination == FDD_TDD) {
                                                                            logger.info(" *************************************************** INFO2 : PERFECT ! The SCELL met all the conditions. We'll add it to the list of scell !!");
                                                                            cellReferenceTable_numberoftime_FDDtoTDDcombo_pcell[i] = cellReferenceTable_numberoftime_FDDtoTDDcombo_pcell[i] + 1;
                                                                            cellReferenceTable_numberoftime_FDDtoTDDcombo_scell[j] = cellReferenceTable_numberoftime_FDDtoTDDcombo_scell[j] + 1;
                                                                            CELLTECHNOLOGY_CONSTRAINTS_ARE_VALID = true;
                                                                        }

                                                                        if(temp_pscellscell_cellTechnology_combination == TDD_TDD) {
                                                                            // ? Verify if the TDD SCELL csirsTrackingPeriod is similar to the TDD PCELL csirsTrackingPeriod
                                                                            if(cellReferenceTable_csirsTrackingPeriod[j] == cellReferenceTable_csirsTrackingPeriod[i]) {
                                                                                logger.info(" ************************************* INFO2 : GOOD ! The TDD SCELL csirsTrackingPeriod (value=" + cellReferenceTable_csirsTrackingPeriod[j] + ") is similar to the TDD PCELL csirsTrackingPeriod (value=" + cellReferenceTable_csirsTrackingPeriod[i] + ")");

                                                                                // ? Verify if the TDD SCELL actBeamforming is similar to the TDD PCELL actBeamforming
                                                                                if(cellReferenceTable_actBeamforming[j] == cellReferenceTable_actBeamforming[i]) {
                                                                                    logger.info(" *************************************************** INFO2 : PERFECT ! The TDD SCELL actBeamforming (value=" + cellReferenceTable_actBeamforming[j] + ") is similar to the TDD PCELL actBeamforming (value=" + cellReferenceTable_actBeamforming[i] + "). We'll add it to the list of scell !!");
                                                                                    cellReferenceTable_numberoftime_TDDtoTDDcombo_pcell[i] = cellReferenceTable_numberoftime_TDDtoTDDcombo_pcell[i] + 1;
                                                                                    cellReferenceTable_numberoftime_TDDtoTDDcombo_scell[j] = cellReferenceTable_numberoftime_TDDtoTDDcombo_scell[j] + 1;
                                                                                    CELLTECHNOLOGY_CONSTRAINTS_ARE_VALID = true;
                                                                                } else {
                                                                                    logger.info(" ************************************* INFO2 : TOOBAD ! The TDD SCELL actBeamforming (value=" + cellReferenceTable_actBeamforming[j] + ") is not similar to the TDD PCELL actBeamforming (value=" + cellReferenceTable_actBeamforming[i] + ").. so we won't add it to the list of scell");
                                                                                }
                                                                            } else {
                                                                                logger.info(" ************************************* INFO2 : TOOBAD ! The TDD SCELL csirsTrackingPeriod (value=" + cellReferenceTable_csirsTrackingPeriod[j] + ") is not similar to the TDD PCELL csirsTrackingPeriod (value=" + cellReferenceTable_csirsTrackingPeriod[i] + ").. so we won't add it to the list of scell");
                                                                            }
                                                                        }

                                                                        if(temp_pscellscell_cellTechnology_combination == TDD_FDD) {
                                                                            logger.info(" *************************************************** INFO2 : PERFECT ! The SCELL met all the conditions. We'll add it to the list of scell !!");
                                                                            cellReferenceTable_numberoftime_TDDtoFDDcombo_pcell[i] = cellReferenceTable_numberoftime_TDDtoFDDcombo_pcell[i] + 1;
                                                                            cellReferenceTable_numberoftime_TDDtoFDDcombo_scell[j] = cellReferenceTable_numberoftime_TDDtoFDDcombo_scell[j] + 1;
                                                                            CELLTECHNOLOGY_CONSTRAINTS_ARE_VALID = true;
                                                                        }

                                                                        if(CELLTECHNOLOGY_CONSTRAINTS_ARE_VALID) {
                                                                            tempScellList.add(cellReferenceTable_gNBID_lcrId[j]);
                                                                            cellReferenceTable_listofpcell[j].add(cellReferenceTable_gNBID_lcrId[i]);
                                                                            CA_IS_NEEDED_ON_GNB = true;

                                                                            if(!cellReferenceTable_pscellscell_cellTechnology_uniquecombination.contains(temp_pscellscell_cellTechnology_combination)) {
                                                                                cellReferenceTable_pscellscell_cellTechnology_uniquecombination.add(temp_pscellscell_cellTechnology_combination);
                                                                            }

                                                                            if(cellReferenceTable_ssbFrequency[i] != null && cellReferenceTable_ssbFrequency[j] != null) {
                                                                                logger.info(" Nice ! Both PCELL and SCELL have a SSB_FREQ. We'll verify the NRCAIMP_NRCAIMS combo");
                                                                                pcell_scell_ssbFrequencyCombo = cellReferenceTable_ssbFrequency[i]+"_"+cellReferenceTable_ssbFrequency[j];

                                                                                if(!evaluated_pcell_scell_ssbFrequencyCombo.contains(pcell_scell_ssbFrequencyCombo)) {
                                                                                    evaluated_pcell_scell_ssbFrequencyCombo.add(pcell_scell_ssbFrequencyCombo);
                                                                                    logger.info(" NICE ! It's the first time we evaluate this NRCAIMP_NRCAIMS combo : " + pcell_scell_ssbFrequencyCombo);

                                                                                    // ? Looking at special condition for N25
                                                                                    // ? If flagN25NRCAIMPandNRCAIMS is false AND pcell is N25 OR scell is N25, we don't add N25 in needed NRCAIMP/NRCAIMS
                                                                                    if( !flagN25NRCAIMPandNRCAIMS && (cellReferenceTable_freqBandIndicatorNR[i] == "25" ||
                                                                                        cellReferenceTable_freqBandIndicatorNR[j] == "25")                              ){
                                                                                            logger.info(" WARNING : We don't create this NRCAIMP/NRCAIMS combo " + evaluated_pcell_scell_ssbFrequencyCombo + ", because it implies N25 and all N25 cells are locked" );
                                                                                    } else {
                                                                                        logger.info(" GOOD : We create this NRCAIMP/NRCAIMS combo " + evaluated_pcell_scell_ssbFrequencyCombo + ", because it doesn't imply N25 or there is at least one N25 cell which is unlock" );

                                                                                        // ? If it's the first time we see a CA COMBO with this PCELL ssbFreq
                                                                                        if(!neededNRCAIMPssbFreqList.contains(cellReferenceTable_ssbFrequency[i])) {
                                                                                            neededNRCAIMPssbFreqList.add(cellReferenceTable_ssbFrequency[i]);
                                                                                            tempList = [cellReferenceTable_ssbFrequency[j]];
                                                                                            neededNRCAIMSssbFreqList.add(tempList);
                                                                                        } else {
                                                                                            position = neededNRCAIMPssbFreqList.indexOf(cellReferenceTable_ssbFrequency[i]);
                                                                                            neededNRCAIMSssbFreqList[position].add(cellReferenceTable_ssbFrequency[j]);
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    logger.info(" WARNING : This NRCAIMP_NRCAIMS combo was already evaluated : " + pcell_scell_ssbFrequencyCombo);
                                                                                }
                                                                            } else {
                                                                                logger.info(" WARNING : One of the PCELL and/or SCELL doesn't have a SSB_FREQ.. we can't put it in the NRCAIMP_NRCAIMS combo");
                                                                            }
                                                                        }
                                                                } else {
                                                                    logger.info(" ************************************* INFO2 : TOOBAD ! The SCELL cellDepType (value=" + cellReferenceTable_cellDepType[j] + ") is not compatible with the PCELL cellDepType (value=" + cellReferenceTable_cellDepType[i] + ").. so we won't add it to the list of scell");
                                                                }
                                                            } else {
                                                                logger.info(" ************************************* INFO2 : TOOBAD ! The SCELL sectorID (value=" + cellReferenceTable_sectorID[j] + ") is not similar to the PCELL sectorID (value=" + cellReferenceTable_sectorID[i] + ").. so we won't add it to the list of scell");
                                                            }
                                                        } else {
                                                            logger.info(" ************************************* INFO2 : TOOBAD ! The SCELL nrarfcn (value=" + cellReferenceTable_nrarfcn[j] + ") is similar than the PCELL nrarfcn (value=" + cellReferenceTable_nrarfcn[i] + ").. so we won't add it to the list of scell");
                                                        }
                                                    } else {
                                                        logger.info(" ************************************* INFO2 : TOOBAD ! This band (value=" + cellReferenceTable_freqBandIndicatorNR[j] + ") is not an allowed scellBand (value=" + scell_bandList + ") or it is N25 cell that is OFF AIR.. so we won't add it to the list of scell");
                                                    }
                                                }
                                            } else {
                                                logger.info(" ***************************** INFO1 : TOOBAD ! This PCELL doesn't have LONGPUCCH (value=" + cellReferenceTable_pucchModeSelect[i] + ").. so we'll ignore it");
                                            }
                                        } else {
                                            logger.info(" ***************************** INFO1 : TOOBAD ! This band (value=" + cellReferenceTable_freqBandIndicatorNR[i] + ") is not an allowed pcellBand (value=" + pcell_bandList + ") or it is N25 cell that is OFF AIR.... so we'll ignore it");
                                        }
                                        cellReferenceTable_listofscell.add(tempScellList);
                                    }

                                    logger.info(" ************* cellReferenceTable_lcrId :                      " + cellReferenceTable_lcrId);
                                    logger.info(" ************* cellReferenceTable_freqBandIndicatorNR :        " + cellReferenceTable_freqBandIndicatorNR);
                                    logger.info(" ************* cellReferenceTable_bw :                         " + cellReferenceTable_bw);
                                    logger.info(" ************* cellReferenceTable_sectorID :                   " + cellReferenceTable_sectorID);
                                    logger.info(" ************* cellReferenceTable_listofscell :                " + cellReferenceTable_listofscell);
                                    logger.info(" ************* cellReferenceTable_listofpcell :                " + cellReferenceTable_listofpcell);
                                    logger.info(" ************* cellReferenceTable_pscellscell_cellTechnology_uniquecombination : " + cellReferenceTable_pscellscell_cellTechnology_uniquecombination);
                                    logger.info(" ************* cellReferenceTable_numberoftime_FDDtoFDDcombo_pcell : " + cellReferenceTable_numberoftime_FDDtoFDDcombo_pcell + " (MAXIMUM number allowed is : " + MAX_NUMBER_OF_FDDtoFDDcombo_HAVING_SAME_PCELL + ")");
                                    logger.info(" ************* cellReferenceTable_numberoftime_FDDtoFDDcombo_scell : " + cellReferenceTable_numberoftime_FDDtoFDDcombo_scell + " (MAXIMUM number allowed is : " + MAX_NUMBER_OF_FDDtoFDDcombo_HAVING_SAME_SCELL + ")");
                                    logger.info(" ************* cellReferenceTable_numberoftime_FDDtoTDDcombo_pcell : " + cellReferenceTable_numberoftime_FDDtoTDDcombo_pcell + " (MAXIMUM number allowed is : " + MAX_NUMBER_OF_FDDtoTDDcombo_HAVING_SAME_PCELL + ")");
                                    logger.info(" ************* cellReferenceTable_numberoftime_FDDtoTDDcombo_scell : " + cellReferenceTable_numberoftime_FDDtoTDDcombo_scell + " (MAXIMUM number allowed is : " + MAX_NUMBER_OF_FDDtoTDDcombo_HAVING_SAME_SCELL + ")");
                                    logger.info(" ************* cellReferenceTable_numberoftime_TDDtoTDDcombo_pcell : " + cellReferenceTable_numberoftime_TDDtoTDDcombo_pcell + " (MAXIMUM number allowed is : " + MAX_NUMBER_OF_TDDtoTDDcombo_HAVING_SAME_PCELL + ")");
                                    logger.info(" ************* cellReferenceTable_numberoftime_TDDtoTDDcombo_scell : " + cellReferenceTable_numberoftime_TDDtoTDDcombo_scell + " (MAXIMUM number allowed is : " + MAX_NUMBER_OF_TDDtoTDDcombo_HAVING_SAME_SCELL + ")");
                                    logger.info(" ************* cellReferenceTable_numberoftime_TDDtoFDDcombo_pcell : " + cellReferenceTable_numberoftime_TDDtoFDDcombo_pcell + " (MAXIMUM number allowed is : " + MAX_NUMBER_OF_TDDtoFDDcombo_HAVING_SAME_PCELL + ")");
                                    logger.info(" ************* cellReferenceTable_numberoftime_TDDtoFDDcombo_scell : " + cellReferenceTable_numberoftime_TDDtoFDDcombo_scell + " (MAXIMUM number allowed is : " + MAX_NUMBER_OF_TDDtoFDDcombo_HAVING_SAME_SCELL + ")");

                                    if(cellReferenceTable_gNBID_lcrId.size() == cellReferenceTable_listofscell.size()) {
                                        logger.info(" ************* The size of 'cellReferenceTable_listofscell' is OK");


                                        /*************************************************************************************************************/
                                        /************ STEP#2a Do the inconsistency check on NRBTS CA parameters and on NRDU CA parameters ************/
                                        /*************************************************************************************************************/
                                        logger.info(" ************* STEP#2a Do the inconsistency check on NRBTS CA parameters and on NRDU CA parameters");

                                        // ? If the GNB is CA
                                        if(CA_IS_NEEDED_ON_GNB) {
                                            // ! Update the parameter NRBTS.actMeasBasedSCellAddition if it's wrong
                                            logger.info(" ************************** The parameter NRBTS.actMeasBasedSCellAddition is " + nrbts.getParameter("actMeasBasedSCellAddition") + " and it should be " + TRUE);
                                            if(nrbts.getParameter("actMeasBasedSCellAddition") != TRUE) {
                                                addRecommendation("UPDATE", nrbts, "UPDATE : Update the parameter actMeasBasedSCellAddition to " + TRUE + " on the NRBTS " + nrbtsDistname, "actMeasBasedSCellAddition", TRUE);
                                                logger.info(" ************************** UPDATE : Update the parameter actMeasBasedSCellAddition to " + TRUE + " on the NRBTS " + nrbtsDistname);
                                            }

                                            // ! Update the parameter NRBTS.actMeasBasedSCellAdditionNsa if it's wrong
                                            if(NRCA_FOR_NSA_USERS_IS_ALLOWED) {
                                                logger.info(" ************************** The parameter NRBTS.actMeasBasedSCellAdditionNsa is " + nrbts.getParameter("actMeasBasedSCellAdditionNsa") + " and it should be " + TRUE);
                                                if(nrbts.getParameter("actMeasBasedSCellAdditionNsa") != TRUE) {
                                                    addRecommendation("UPDATE", nrbts, "UPDATE : Update the parameter actMeasBasedSCellAdditionNsa to " + TRUE + " on the NRBTS " + nrbtsDistname, "actMeasBasedSCellAdditionNsa", TRUE);
                                                    logger.info(" ************************** UPDATE : Update the parameter actMeasBasedSCellAdditionNsa to " + TRUE + " on the NRBTS " + nrbtsDistname);
                                                }
                                            } else {
                                                logger.info(" ************************** The parameter NRBTS.actMeasBasedSCellAdditionNsa is " + nrbts.getParameter("actMeasBasedSCellAdditionNsa") + " and it should be " + FALSE);
                                                if(nrbts.getParameter("actMeasBasedSCellAdditionNsa") != FALSE) {
                                                    addRecommendation("UPDATE", nrbts, "UPDATE : Update the parameter actMeasBasedSCellAdditionNsa to " + FALSE + " on the NRBTS " + nrbtsDistname, "actMeasBasedSCellAdditionNsa", FALSE);
                                                    logger.info(" ************************** UPDATE : Update the parameter actMeasBasedSCellAdditionNsa to " + FALSE + " on the NRBTS " + nrbtsDistname);
                                                }
                                            }

                                            // !Update the parameter NRBTS.maxNumScellsDl if it's wrong
                                            logger.info(" ************************** The parameter NRBTS.maxNumScellsDl is " + nrbts.getParameter("maxNumScellsDl") + " and it should be " + MAXNUMSCELLSDL);
                                            if(nrbts.getParameter("maxNumScellsDl") != MAXNUMSCELLSDL) {
                                                addRecommendation("UPDATE", nrbts, "UPDATE : Update the parameter maxNumScellsDl to " + MAXNUMSCELLSDL + " on the NRBTS " + nrbtsDistname, "maxNumScellsDl", MAXNUMSCELLSDL);
                                                logger.info(" ************************** UPDATE : Update the parameter maxNumScellsDl to " + MAXNUMSCELLSDL + " on the NRBTS " + nrbtsDistname);
                                            }

                                            nrduDistname = nrdu.getParameter("distName").substring(10);
                                            // ! Update the parameter NRDU.actInterNumerologyCa if it's wrong
                                            logger.info(" ************************** The parameter NRDU.actInterNumerologyCa is " + nrdu.getParameter("actInterNumerologyCa") + " and it should be " + TRUE);
                                            if(nrdu.getParameter("actInterNumerologyCa") != TRUE) {
                                                addRecommendation("UPDATE", nrdu, "UPDATE : Update the parameter actInterNumerologyCa to " + TRUE + " on the NRDU " + nrduDistname, "actInterNumerologyCa", TRUE);
                                                logger.info(" ************************** UPDATE : Update the parameter actInterNumerologyCa to " + TRUE + " on the NRDU " + nrduDistname);
                                            }

                                            // ! Update the parameter NRDU.actConnectedModeMobilityCA if it's wrong
                                            logger.info(" ************************** The parameter NRDU.actConnectedModeMobilityCA is " + nrdu.getParameter("actConnectedModeMobilityCA") + " and it should be " + TRUE);
                                            if(nrdu.getParameter("actConnectedModeMobilityCA") != TRUE) {
                                                addRecommendation("UPDATE", nrdu, "UPDATE : Update the parameter actConnectedModeMobilityCA to " + TRUE + " on the NRDU " + nrduDistname, "actConnectedModeMobilityCA", TRUE);
                                                logger.info(" ************************** UPDATE : Update the parameter actConnectedModeMobilityCA to " + TRUE + " on the NRDU " + nrduDistname);
                                            }

                                            // ! Update the parameter NRDU.flexibleCAMode if it's wrong
                                            logger.info(" ************************** The parameter NRDU.flexibleCAMode is " + nrdu.getParameter("flexibleCAMode") + " and it should be " + CONFIGURATIONINDEPENDENT);
                                            if(nrdu.getParameter("flexibleCAMode") != CONFIGURATIONINDEPENDENT) {
                                                addRecommendation("UPDATE", nrdu, "UPDATE : Update the parameter flexibleCAMode to " + CONFIGURATIONINDEPENDENT + " on the NRDU " + nrduDistname, "flexibleCAMode", CONFIGURATIONINDEPENDENT);
                                                logger.info(" ************************** UPDATE : Update the parameter flexibleCAMode to " + CONFIGURATIONINDEPENDENT + " on the NRDU " + nrduDistname);
                                            }

                                            // !Update the listenumeration NRDU.carrierAggregationSupportList if it's wrong
                                            neededcarrierAggregationSupportList = createListEnum();
                                            neededcarrierAggregationSupportListArray = []; // 4CC support
                                            if(cellReferenceTable_pscellscell_cellTechnology_uniquecombination.contains(FDD_FDD)) { // * 4CC support-3
                                                neededcarrierAggregationSupportList.addEntry(CA_FDD_FDD);
                                                neededcarrierAggregationSupportListArray.add(CA_FDD_FDD);
                                            }

                                            if(cellReferenceTable_pscellscell_cellTechnology_uniquecombination.contains(FDD_TDD)) { // * 4CC support -200
                                                neededcarrierAggregationSupportList.addEntry(CA_FDD_FR1TDD_SA);
                                                neededcarrierAggregationSupportListArray.add(CA_FDD_FR1TDD_SA);

                                                if(NRCA_FOR_NSA_USERS_IS_ALLOWED) {                                                 // * 4CC support - 201
                                                    neededcarrierAggregationSupportList.addEntry(CA_FDD_FR1TDD_NSA);
                                                    neededcarrierAggregationSupportListArray.add(CA_FDD_FR1TDD_NSA);
                                                }
                                            }

                                            if(cellReferenceTable_pscellscell_cellTechnology_uniquecombination.contains(TDD_TDD)) { // * 4CC support -400
                                                    neededcarrierAggregationSupportList.addEntry(CA_FR1TDD_SA);
                                                    neededcarrierAggregationSupportListArray.add(CA_FR1TDD_SA);

                                                    if(NRCA_FOR_NSA_USERS_IS_ALLOWED) {                                                         // * 4CC support - 401
                                                        neededcarrierAggregationSupportList.addEntry(CA_FR1TDD_NSA);
                                                        neededcarrierAggregationSupportListArray.add(CA_FR1TDD_NSA);
                                                    }
                                            }

                                            if(cellReferenceTable_pscellscell_cellTechnology_uniquecombination.contains(TDD_FDD) ){         // * 4CC support -500
                                                neededcarrierAggregationSupportList.addEntry(CA_FR1TDD_FDD_SA);
                                                neededcarrierAggregationSupportListArray.add(CA_FR1TDD_FDD_SA);

                                                //if(NRCA_FOR_NSA_USERS_IS_ALLOWED && versionInt >= 22 && versionString != "22R1" && versionString != "22R2") { //Starting with 22R3
                                                //    neededcarrierAggregationSupportList.addEntry(CA_FR1TDD_FDD_NSA);
                                                //}
                                            }

                                            if( cellReferenceTable_pscellscell_cellTechnology_uniquecombination.contains(FDD_FDD)       &&
                                                cellReferenceTable_pscellscell_cellTechnology_uniquecombination.contains(FDD_TDD)       ){  // * 4CC support  600
                                                    neededcarrierAggregationSupportList.addEntry(CA_FDD_ANY_SA);
                                                    neededcarrierAggregationSupportListArray.add(CA_FDD_ANY_SA);
                                            }

                                            if( cellReferenceTable_pscellscell_cellTechnology_uniquecombination.contains(TDD_FDD)       ||
                                                cellReferenceTable_pscellscell_cellTechnology_uniquecombination.contains(TDD_TDD)       ){  // * 4CC support 700
                                                    neededcarrierAggregationSupportList.addEntry(CA_FR1TDD_ANY_SA);
                                                    neededcarrierAggregationSupportListArray.add(CA_FR1TDD_ANY_SA);
                                                    if(!neededcarrierAggregationSupportListArray.contains(CA_FR1TDD_FDD_SA)){ // ! NEW
                                                        neededcarrierAggregationSupportList.addEntry(CA_FR1TDD_FDD_SA);
                                                        neededcarrierAggregationSupportListArray.add(CA_FR1TDD_FDD_SA);
                                                    }
                                                    if(!neededcarrierAggregationSupportListArray.contains(CA_FR1TDD_SA)){ // ! NEW
                                                        neededcarrierAggregationSupportList.addEntry(CA_FR1TDD_SA);
                                                        neededcarrierAggregationSupportListArray.add(CA_FR1TDD_SA);
                                                        if(NRCA_FOR_NSA_USERS_IS_ALLOWED) {                                                         // * 4CC support - 401
                                                            neededcarrierAggregationSupportList.addEntry(CA_FR1TDD_NSA);
                                                            neededcarrierAggregationSupportListArray.add(CA_FR1TDD_NSA);
                                                        }
                                                    }
                                            }

                                            currentcarrierAggregationSupportListArray = [];
                                            currentcarrierAggregationSupportList = nrdu.getListEnumerationParameter("carrierAggregationSupportList");
                                            for(i = 0; i < currentcarrierAggregationSupportList.size(); i++) {
                                                currentcarrierAggregationSupportListArray.add(currentcarrierAggregationSupportList[i]);
                                            }

                                            logger.info(" ************************** The current currentcarrierAggregationSupportList is " +  currentcarrierAggregationSupportList + " and the needed neededcarrierAggregationSupportList is " +  neededcarrierAggregationSupportList);
                                            if(currentcarrierAggregationSupportList.size() != neededcarrierAggregationSupportList.size()) {
                                                addRecommendation("UPDATE", nrdu, "UPDATE : Update carrierAggregationSupportList in NRDU " + nrduDistname + ", because the size is wrong", "carrierAggregationSupportList", neededcarrierAggregationSupportList);
                                                logger.info(" ************************** UPDATE : Update carrierAggregationSupportList in NRDU " + nrduDistname + ", because the size is wrong");
                                            } else {
                                                logger.info(" ************************** PERFECT ! Both lists have the same size. Now let's compare each parameters of each items..");
                                                for(def i = 0; i < neededcarrierAggregationSupportList.size(); i++) {
                                                    if(!currentcarrierAggregationSupportListArray.contains(neededcarrierAggregationSupportList[i])) {
                                                        addRecommendation("UPDATE", nrdu, "UPDATE : Update carrierAggregationSupportList in NRDU " + nrduDistname + ", because the value is wrong", "carrierAggregationSupportList", neededcarrierAggregationSupportList);
                                                        logger.info(" ****************************** UPDATE : Update carrierAggregationSupportList in NRDU " + nrduDistname + ", because the value is wrong");
                                                        break;
                                                    }
                                                }
                                            }
                                        } else {
                                            // ! HERE WE COULD DEACTIVATE ABIO GNB LEVEL CA PARAMETER
                                        }

                                        /********************************************************************************************************************************************************/
                                        /************ STEP#2b Loop through all the NRCELL, and do the inconsistency check on NRCELL CA parameters and on NRREL.sCellActivationMethod ************/
                                        /********************************************************************************************************************************************************/
                                        logger.info(" ************* STEP#2b Loop through all the NRCELL, and do the inconsistency check on NRCELL CA parameters and on NRREL.sCellActivationMethod");
                                        for(nrcell in nrcells) {
                                            nrcellDistname = nrcell.getParameter("distName").substring(10);
                                            position = cellReferenceTable_lcrId.indexOf(nrcell.getParameter("lcrId"));
                                            logger.info(" ****************** Evaluating the nrcell : " + nrcellDistname + " with lcrId:" + cellReferenceTable_lcrId[position] + " with gNBID_lcrId:" + cellReferenceTable_gNBID_lcrId[position] + " with cellTechnology:" + cellReferenceTable_cellTechnology[position] + " with status:" + cellReferenceTable_status[position] + " with freqBandIndicatorNR:" + cellReferenceTable_freqBandIndicatorNR[position] + " with bw:" + cellReferenceTable_bw[position] + " with cellTechnology_bw_scs:" + cellReferenceTable_cellTechnology_bw_scs[position] + " with max_reqMinNumPuschPrb_withCA:" + cellReferenceTable_max_reqMinNumPuschPrb_withCA[position] + " with sectorIDstr:" + cellReferenceTable_sectorID[position] + " with allowed scells:" + cellReferenceTable_listofscell[position]);

                                            // ? If the GNB is CA
                                            if(CA_IS_NEEDED_ON_GNB) {

                                                // ! Update the parameter NRCELL.reqMinNumPuschPrb if it's higher than the maximum value allowed
                                                logger.info(" ************************** The current value for reqMinNumPuschPrb is " +  nrcell.getParameterAsInteger("reqMinNumPuschPrb") + " and for 5G CA, the maximum allowed value is : " + cellReferenceTable_max_reqMinNumPuschPrb_withCA[position]);
                                                if(nrcell.getParameterAsInteger("reqMinNumPuschPrb") > cellReferenceTable_max_reqMinNumPuschPrb_withCA[position]) {
                                                    addRecommendation("UPDATE", nrcell, "UPDATE : To allow 5G CA activation, decrease the parameter reqMinNumPuschPrb to " + cellReferenceTable_max_reqMinNumPuschPrb_withCA[position] + " on the NRCELL " + nrcellDistname, "reqMinNumPuschPrb", cellReferenceTable_max_reqMinNumPuschPrb_withCA[position]);
                                                    logger.info(" **************************UPDATE : To allow 5G CA activation, decrease the parameter reqMinNumPuschPrb to " + cellReferenceTable_max_reqMinNumPuschPrb_withCA[position] + " on the NRCELL " + nrcellDistname);
                                                }

                                                if(cellReferenceTable_freqBandIndicatorNR[position] == "78-F2"){
                                                    if ( (cellReferenceTable_wttpind[position] == "YES") || (cellReferenceTable_wttpind[position] == "YES_FLEX")){
                                                        if(nrcell.getParameter("a1MeasSCellAddEnabled") != TRUE)
                                                        {
                                                            addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter a1MeasSCellAddEnabled to " + TRUE + " on the NRCELL " + nrcellDistname, "a1MeasSCellAddEnabled", TRUE);
                                                            logger.info(" ************************** UPDATE : Update the parameter a1MeasSCellAddEnabled to " + "TRUE" + " on the NRCELL " + nrcellDistname);
                                                        }
                                                    } else {
                                                        if(nrcell.getParameter("a1MeasSCellAddEnabled") != FALSE){
                                                            addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter a1MeasSCellAddEnabled to " + FALSE + " on the NRCELL " + nrcellDistname, "a1MeasSCellAddEnabled", FALSE);
                                                            logger.info(" ************************** UPDATE : Update the parameter a1MeasSCellAddEnabled to " + "FALSE" + " on the NRCELL " + nrcellDistname + " freq  " + cellReferenceTable_freqBandIndicatorNR[position]);
                                                        }
                                                    }
                                                } else {
                                                    if(nrcell.getParameter("a1MeasSCellAddEnabled") != TRUE){
                                                        addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter a1MeasSCellAddEnabled to " + TRUE + " on the NRCELL " + nrcellDistname, "a1MeasSCellAddEnabled", TRUE);
                                                        logger.info(" ************************** UPDATE : Update the parameter a1MeasSCellAddEnabled to " + "TRUE" + " on the NRCELL " + nrcellDistname);
                                                    }
                                                }

                                                // Update the parameter NRCELL.a1MeasSCellAddEnabled if it's wrong
                                                // logger.info(" ************************** The parameter NRCELL.a1MeasSCellAddEnabled is " + nrcell.getParameter("a1MeasSCellAddEnabled") + " and it should be " + TRUE);
                                                // if(nrcell.getParameter("a1MeasSCellAddEnabled") != TRUE) {
                                                    // if(cellReferenceTable_freqBandIndicatorNR[position] != "78-F2"){
                                                        // addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter a1MeasSCellAddEnabled to " + TRUE + " on the NRCELL " + nrcellDistname, "a1MeasSCellAddEnabled", TRUE);
                                                        // logger.info(" ************************** UPDATE : Update the parameter a1MeasSCellAddEnabled to " + "TRUE" + " on the NRCELL " + nrcellDistname);
                                                    // }
                                                // }
                                                // CHECK N_78_F2 TRUE THEN SET IT TO 0
                                                // else {
                                                    // if(cellReferenceTable_freqBandIndicatorNR[position] == "78-F2"){
                                                        // addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter a1MeasSCellAddEnabled to " + FALSE + " on the NRCELL " + nrcellDistname, "a1MeasSCellAddEnabled", FALSE);
                                                    // logger.info(" ************************** UPDATE : Update the parameter a1MeasSCellAddEnabled to " + "FALSE" + " on the NRCELL " + nrcellDistname + " freq  " + cellReferenceTable_freqBandIndicatorNR[position]);
                                                    // }
                                                // }

                                                // !Update the parameter NRCELL.a2MeasSCellRelEnabled if it's wrong
                                                logger.info(" ************************** The parameter NRCELL.a2MeasSCellRelEnabled is " + nrcell.getParameter("a2MeasSCellRelEnabled") + " and it should be " + TRUE);
                                                if(nrcell.getParameter("a2MeasSCellRelEnabled") != TRUE) {
                                                    addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter a2MeasSCellRelEnabled to " + TRUE + " on the NRCELL " + nrcellDistname, "a2MeasSCellRelEnabled", TRUE);
                                                    logger.info(" ************************** UPDATE : Update the parameter a2MeasSCellRelEnabled to " + TRUE + " on the NRCELL " + nrcellDistname);
                                                }

                                                // ! Update the parameter NRCELL.actK1OptForDPA if it's wrong
                                                logger.info(" ************************** The parameter NRCELL.actK1OptForDPA is " + nrcell.getParameter("actK1OptForDPA") +" Current NRBTS software version is : "+ versionInt + " If version is less than 23 then value should be " + TRUE);
                                                if(versionInt < 23){ //actK1OptForDPA removed from version 23R1 onwards
                                                    if(nrcell.getParameter("actK1OptForDPA") != TRUE) {
                                                        addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter actK1OptForDPA to " + TRUE + " on the NRCELL " + nrcellDistname, "actK1OptForDPA", TRUE);
                                                        logger.info(" ************************** UPDATE : Update the parameter actK1OptForDPA to " + TRUE + " on the NRCELL " + nrcellDistname);
                                                    }
                                                }

                                                // ! Update the parameter NRCELL.actK1Opt if it's wrong
                                                logger.info(" ************************** The parameter NRCELL.actK1Opt is " + nrcell.getParameter("actK1Opt") +" Current NRBTS software version is : "+ versionInt + " actK1Opt value should be intra-gNB " );
                                                if(versionInt >= 23){ //actK1Opt added in version 23R1 onwards
                                                    if(nrcell.getParameterAsInteger("actK1Opt") != 1) {
                                                        addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter actK1Opt to " + 1 + " on the NRCELL " + nrcellDistname, "actK1Opt", "1");
                                                        logger.info(" ************************** UPDATE : Update the parameter actK1Opt to " + intra-gNB + " on the NRCELL " + nrcellDistname);
                                                    }
                                                }

                                                // !Update the parameter NRCELL.maxNumScellsDlFr1 if it's wrong and condition is carrierAggregationSupportList(neededcarrierAggregationSupportListArray) contains - CA_FDD_ANY_SA = "600" AND CA_FR1TDD_ANY_SA = "700"
                                                logger.info(" ************************** The parameter NRCELL.maxNumScellsDlFr1 is " + nrbts.getParameter("maxNumScellsDlFr1") + " and it should be " + MAXNUMSCELLSDLFR1);
                                                if(neededcarrierAggregationSupportListArray.contains(CA_FDD_ANY_SA) && neededcarrierAggregationSupportListArray.contains(CA_FR1TDD_ANY_SA)){
                                                    if(nrcell.getParameter("maxNumScellsDlFr1") != MAXNUMSCELLSDLFR1) {
                                                        addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter maxNumScellsDlFr1 to " + MAXNUMSCELLSDLFR1 + " on the NRCELL " + nrcellDistname, "maxNumScellsDlFr1", MAXNUMSCELLSDLFR1);
                                                        logger.info(" ************************** UPDATE : Update the parameter maxNumScellsDlFr1 to " + MAXNUMSCELLSDLFR1 + " on the NRCELL " + nrcellDistname);
                                                    }
                                                } else{
                                                    addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter maxNumScellsDlFr1 to " + MAXNUMSCELLSDLFR1 + " on the NRCELL " + nrcellDistname, "maxNumScellsDlFr1", "");
                                                    logger.info(" ************************** The needed neededcarrierAggregationSupportListArray  doesn't have CA_FDD_ANY_SA = 600 OR CA_FR1TDD_ANY_SA = 700 =" +  neededcarrierAggregationSupportListArray);
                                                }
                                            } else {
                                                // ! HERE WE COULD DEACTIVATE ABIO CELL LEVEL CA PARAMETER
                                            }

                                            // ! Update the parameter NRCELL.idleMMSteeringAlgo if it's wrong
                                            if(neededNRCAIMPssbFreqList.size() != 0 && (cellReferenceTable_cellDepType[position] == SA || cellReferenceTable_cellDepType[position] == SAplusNSA)) {
                                                logger.info(" ************************** The parameter NRCELL.idleMMSteeringAlgo is " + nrcell.getParameter("idleMMSteeringAlgo") + " and it should be " + CAAWARE);
                                                if(nrcell.getParameter("idleMMSteeringAlgo") != CAAWARE) {
                                                    addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter idleMMSteeringAlgo to " + CAAWARE + " on the NRCELL " + nrcellDistname, "idleMMSteeringAlgo", CAAWARE);
                                                    logger.info(" ************************** UPDATE : Update the parameter idleMMSteeringAlgo to " + CAAWARE + " on the NRCELL " + nrcellDistname);
                                                }
                                            }

                                            nrrels = nrcell.getMos("NRREL");
                                            if(nrrels.size() != 0) {
                                                currentNumberOfNrrel = nrrels.size();
                                                nrrel_gNBID_lcrId_list = [];

                                                // ? Update sCellAdditionMethod for existing NRREL
                                                for(nrrel in nrrels) {
                                                    nrrelDistname = nrrel.getParameter("distName").substring(10);
                                                    nrrel_gNBID_lcrId = nrrel.getParameter("gNbId")+"_"+nrrel.getParameter("lcrId");
                                                    nrrel_gNBID_lcrId_list.add(nrrel_gNBID_lcrId);
                                                    logger.info(" ********************** Evaluating the NRREL : " + nrrelDistname + " with nrrel_gNBID_lcrId : " + nrrel_gNBID_lcrId + " with sCellAdditionMethod : " + nrrel.getParameter("sCellAdditionMethod") + " and with removeNotAllowed : " + nrrel.getParameter("removeNotAllowed"));

                                                    // ? If the existing NRREL is a scell that we need, make sure sCellAdditionMethod is configured and make sure removeNotAllowed is true
                                                    if(cellReferenceTable_listofscell[position].contains(nrrel_gNBID_lcrId)) {
                                                        logger.info(" ************************ This NRREL is a scell for our NRCELL " + cellReferenceTable_gNBID_lcrId[position] + "!! sCellAdditionMethod should be " + COVBASEDA1 + "sCellOperationalMode should be "+ " 0 - dlOnly "+" and removeNotAllowed should be " + TRUE);
                                                        if(nrrel.getParameter("sCellAdditionMethod") != COVBASEDA1) {
                                                            addRecommendation("UPDATE", nrrel, "UPDATE : On NRREL " + nrrelDistname + " with nrrel_gNBID_lcrId : " + nrrel_gNBID_lcrId + ", we'll update the parameter sCellAdditionMethod to " + COVBASEDA1 + " because this NRREL is a SCELL for this NRCELL " + cellReferenceTable_gNBID_lcrId[position], "sCellAdditionMethod", COVBASEDA1);
                                                            // ! Adding sCellAdditionMethod  1 (CovBasedA1) then sCellOperationalMode=0 (dlOnly)
                                                             addRecommendation("UPDATE", nrrel, "UPDATE : On NRREL " + nrrelDistname + " with nrrel_gNBID_lcrId : " + nrrel_gNBID_lcrId + ", we'll update the parameter sCellAdditionMethod to " + COVBASEDA1 + " then RAN version is greater than or equal to 23R2 - sCellOperationalMode=0 (dlOnly) " + " because this NRREL is a SCELL for this NRCELL " + cellReferenceTable_gNBID_lcrId[position], "sCellOperationalMode", "0");
                                                             logger.info(" ****************************************** UPDATE : On NRREL " + nrrelDistname + " with nrrel_gNBID_lcrId : " + nrrel_gNBID_lcrId + ", we'll update the parameter sCellAdditionMethod to " + COVBASEDA1 + " sCellOperationalMode to dlOnly "+ nrrel.getParameter("sCellOperationalMode") +" because this NRREL is a SCELL for this NRCELL " + cellReferenceTable_gNBID_lcrId[position]);
                                                        }

                                                        if(nrrel.getParameter("removeNotAllowed") != TRUE) {
                                                            addRecommendation("UPDATE", nrrel, "UPDATE : On NRREL " + nrrelDistname + " with nrrel_gNBID_lcrId : " + nrrel_gNBID_lcrId + ", we'll update the parameter removeNotAllowed to " + TRUE + " because this NRREL is a SCELL for this NRCELL " + cellReferenceTable_gNBID_lcrId[position], "removeNotAllowed", TRUE);
                                                            logger.info(" ****************************************** UPDATE : On NRREL " + nrrelDistname + " with nrrel_gNBID_lcrId : " + nrrel_gNBID_lcrId + ", we'll update the parameter removeNotAllowed to " + TRUE + " because this NRREL is a SCELL for this NRCELL " + cellReferenceTable_gNBID_lcrId[position]);
                                                        }
                                                    }
                                                    // ? If the existing NRREL is not a scell that we need, make sure sCellAdditionMethod is empty AND sCellOperationalMode= empty
                                                    else {
                                                        logger.info(" ************************ This NRREL is not a scell for our NRCELL " + cellReferenceTable_gNBID_lcrId[position] + ".. sCellAdditionMethod should be null"+ " and also sCellOperationalMode= empty" );
                                                        if(nrrel.getParameter("sCellAdditionMethod") != null) {
                                                            addRecommendation("UPDATE", nrrel, "UPDATE : On NRREL " + nrrelDistname + " with nrrel_gNBID_lcrId : " + nrrel_gNBID_lcrId + ", we'll update the parameter sCellAdditionMethod to null because this NRREL is not a SCELL for this NRCELL " + cellReferenceTable_gNBID_lcrId[position], "sCellAdditionMethod", "");

                                                            addRecommendation("UPDATE", nrrel, "UPDATE : On NRREL " + nrrelDistname + " with nrrel_gNBID_lcrId : " + nrrel_gNBID_lcrId + ", we'll update the parameter sCellAdditionMethod to " + COVBASEDA1 + " then RAN version is greater than or equal to 23R2 - sCellOperationalMode=0 (dlOnly) " + " because this NRREL is a SCELL for this NRCELL " + cellReferenceTable_gNBID_lcrId[position], "sCellOperationalMode", "");
                                                            logger.info(" ****************************************** UPDATE : On NRREL " + nrrelDistname + " with nrrel_gNBID_lcrId : " + nrrel_gNBID_lcrId + ", we'll update the parameter sCellAdditionMethod to null because this NRREL is not a SCELL for this NRCELL " + cellReferenceTable_gNBID_lcrId[position]);
                                                        }
                                                    }
                                                }

                                                // ! Create missing NRREL for NRCA
                                                for(i = 0; i < cellReferenceTable_listofscell[position].size(); i++) {
                                                    // ? If the needed scell doesn't exist in NRREL, create it
                                                    if(!nrrel_gNBID_lcrId_list.contains(cellReferenceTable_listofscell[position][i])) {
                                                        if(currentNumberOfNrrel < NRRELMAXSIZE) {
                                                            logger.info(" ********************** The NRREL with nrrel_gNBID_lcrId : " + cellReferenceTable_listofscell[position][i] + " is missing and it's needed for NRCA. We'll create it !");
                                                            def new_nrrel = buildMo("NRREL", nrcell, "1-"+(NRRELMAXSIZE-1));
                                                            new_nrrel.setParameterValue("defaults", "Default_5G");
                                                            new_nrrel.setParameterValue("gNbId", cellReferenceTable_listofscell[position][i].split("_")[0]);
                                                            new_nrrel.setParameterValue("lcrId", cellReferenceTable_listofscell[position][i].split("_")[1]);
                                                            new_nrrel.setParameterValue("removeNotAllowed", TRUE);
                                                            new_nrrel.setParameterValue("sCellAdditionMethod", COVBASEDA1);
                                                            new_nrrel.setParameterValue("sCellOperationalMode", 0);

                                                            // ? Adding sCellAdditionMethod  1 (CovBasedA1) then sCellOperationalMode=0 (dlOnly)
                                                            addRecommendation("CREATE", new_nrrel, "CREATE : For NRCA, create NRREL with nrrel_gNBID_lcrId : " + cellReferenceTable_listofscell[position][i] + " with removeNotAllowed : " + TRUE + " with sCellAdditionMethod : " + COVBASEDA1+ " And sCellOperationalMode = "+ "0");
                                                            logger.info(" ******************************************** CREATE : For NRCA, create NRREL with nrrel_gNBID_lcrId : " + cellReferenceTable_listofscell[position][i] + " with removeNotAllowed : " + TRUE + " with sCellAdditionMethod : " + COVBASEDA1);
                                                            currentNumberOfNrrel++;
                                                        } else {
                                                            listofcellwithtoomanyNRREL.add(nrcellDistname);
                                                            listofunconfiguredscelltoomanyNRREL.add(cellReferenceTable_listofscell[position][i]);
                                                        }
                                                    }
                                                }
                                            }

                                            else {
                                                logger.info(" ********************* WARNING : NRREL size is 0. Cannot evaluate NRREL.sCellAdditionMethod")
                                            }
                                        }




                                        /********************************************************************/
                                        /************ STEP#3a Manage the NRCAIMP/NRCAIMS objects ************/
                                        /********************************************************************/
                                        logger.info(" ************* STEP#3a Manage the NRCAIMP/NRCAIMS objects");
                                        logger.info(" ************* evaluated_pcell_scell_ssbFrequencyCombo : " + evaluated_pcell_scell_ssbFrequencyCombo);
                                        logger.info(" ************* neededNRCAIMPssbFreqList : " + neededNRCAIMPssbFreqList);
                                        logger.info(" ************* neededNRCAIMSssbFreqList : " + neededNRCAIMSssbFreqList);
                                        if(neededNRCAIMPssbFreqList.size() == neededNRCAIMSssbFreqList.size()) {
                                            nrcaimps = nrbts.getMos("NRCAIMP");
                                            logger.info(" ********* Build current NRCAIMP ssbFreq list");
                                            currentNRCAIMPssbFreqList = [];
                                            for(nrcaimp in nrcaimps) {
                                                currentpCellConfig = nrcaimp.getListParameter("pCellConfig");
                                                currentNRCAIMPssbFreqList.add(currentpCellConfig[0]["nrarfcnDl"]);
                                            }
                                            logger.info(" **************** The needed NRCAIMP ssbFreq are : " + neededNRCAIMPssbFreqList + " (size is " + neededNRCAIMPssbFreqList.size() + ")");
                                            logger.info(" **************** The current NRCAIMP ssbFreq are : " + currentNRCAIMPssbFreqList + " (size is " + currentNRCAIMPssbFreqList.size() + ")");

                                            // ? If there is something in neededNRCAIMPssbFreqList, we need to make sure all NRCAIMP/NRCAIMS are perfect
                                            if(neededNRCAIMPssbFreqList.size() > 0){
                                                // ?Walk through Needed List vs Current List
                                                for(def i = 0; i < neededNRCAIMPssbFreqList.size(); i++) {
                                                    logger.info(" ********* We need a NRCAIMP with this ssbFreq : " + neededNRCAIMPssbFreqList[i]);

                                                    // ! Create NRCAIMP if Missing
                                                    if(!currentNRCAIMPssbFreqList.contains(neededNRCAIMPssbFreqList[i])) {
                                                        neededpCellConfig = createList();
                                                        neededpCellConfig.addEntry(["nrarfcnDl":neededNRCAIMPssbFreqList[i]]);
                                                        def new_nrcaimp = buildMo("NRCAIMP", nrbts, "1-12");
                                                        new_nrcaimp.setParameterValue("defaults", "Default_5G");
                                                        new_nrcaimp.setParameterValue("pCellConfig", neededpCellConfig);
                                                        addRecommendation("CREATE", new_nrcaimp, "Create NRCAIMP with ssbFreq = " + neededNRCAIMPssbFreqList[i]);
                                                        logger.info(" ******************************************** Create NRCAIMP with ssbFreq = " + neededNRCAIMPssbFreqList[i]);
                                                        for(def j = 0; j < neededNRCAIMSssbFreqList[i].size(); j++) {
                                                            neededsCellConfig = createList();
                                                            neededsCellConfig.addEntry(["nrarfcnDl":neededNRCAIMSssbFreqList[i][j]]);
                                                            def new_nrcaims = buildMo("NRCAIMS", new_nrcaimp, "1-6");
                                                            new_nrcaims.setParameterValue("defaults", "Default_5G");
                                                            new_nrcaims.setParameterValue("sCellConfig", neededsCellConfig);
                                                            addRecommendation("CREATE", new_nrcaims, "Create NRCAIMS with ssbFreq : " + neededNRCAIMSssbFreqList[i][j] + " under the newly created NRCAIMP with ssbFreq : " + neededNRCAIMPssbFreqList[i]);
                                                            logger.info(" ********************************************** Create NRCAIMS with ssbFreq : " + neededNRCAIMSssbFreqList[i][j] + " under the newly created NRCAIMP with ssbFreq : " + neededNRCAIMPssbFreqList[i]);
                                                        }
                                                    }
                                                    // ? If NRCAIMP ssbFreq exist, Create/Delete NRCAIMS if needed
                                                    else {
                                                        for(nrcaimp in nrcaimps) {
                                                            currentpCellConfig = nrcaimp.getListParameter("pCellConfig");
                                                            if(currentpCellConfig[0]["nrarfcnDl"] == neededNRCAIMPssbFreqList[i]) {
                                                                logger.info(" ************ The NRCAIMP-" + nrcaimp.getParameter("originalId") + " has the needed ssbFreq : " + currentpCellConfig[0]["nrarfcnDl"]);
                                                                nrcaimss = nrcaimp.getMos("NRCAIMS");

                                                                // * Build a list of all the current NRCAIMS ssbFreq under the current NRCAIMP
                                                                currentNRCAIMSssbFreqList = [];
                                                                for(nrcaims in nrcaimss) {
                                                                    currentsCellConfig = nrcaims.getListParameter("sCellConfig");
                                                                    currentNRCAIMSssbFreqList.add(currentsCellConfig[0]["nrarfcnDl"]);
                                                                }

                                                                logger.info(" **************** For the NRCAIMP-" + nrcaimp.getParameter("originalId") + " with the needed ssbFreq : " + currentpCellConfig[0]["nrarfcnDl"] + ", the needed NRCAIMS ssbFreq are : " + neededNRCAIMSssbFreqList[i] + " (size is " + neededNRCAIMSssbFreqList[i].size() + ")");
                                                                logger.info(" **************** For the NRCAIMP-" + nrcaimp.getParameter("originalId") + " with the needed ssbFreq : " + currentpCellConfig[0]["nrarfcnDl"] + ", the current NRCAIMS ssbFreq are : " + currentNRCAIMSssbFreqList + " (size is " + currentNRCAIMSssbFreqList.size() + ")");
                                                                // ? Loop through all the needed ssbFreq
                                                                for(def j = 0; j < neededNRCAIMSssbFreqList[i].size(); j++) {
                                                                    // ! if it doesn't exist, create it
                                                                    if(!currentNRCAIMSssbFreqList.contains(neededNRCAIMSssbFreqList[i][j])) {
                                                                        neededsCellConfig = createList();
                                                                        neededsCellConfig.addEntry(["nrarfcnDl":neededNRCAIMSssbFreqList[i][j]]);
                                                                        def new_nrcaims = buildMo("NRCAIMS", nrcaimp, "1-6");
                                                                        new_nrcaims.setParameterValue("defaults", "Default_5G");
                                                                        new_nrcaims.setParameterValue("sCellConfig", neededsCellConfig);
                                                                        addRecommendation("CREATE", new_nrcaims, "Create NRCAIMS with ssbFreq : " + neededNRCAIMSssbFreqList[i][j] + " under the existing NRCAIMP-" + nrcaimp.getParameter("originalId") + " with ssbFreq : " + neededNRCAIMPssbFreqList[i]);
                                                                        logger.info(" ******************************************** Create NRCAIMS with ssbFreq : " + neededNRCAIMSssbFreqList[i][j] + " under the existing NRCAIMP-" + nrcaimp.getParameter("originalId") + " with ssbFreq : " + neededNRCAIMPssbFreqList[i]);
                                                                    }
                                                                }

                                                                // ! Delete NRCAIMS ssbFreq that are not in the neededNRCAIMSssbFreqList
                                                                // * Protection to avoid deleting every NRCAIMS if the CPR didn't find any needed NRCAIMS, since on a ABIO, it's highly unlikely that the number of needed NRCAIMS would be empty
                                                                if(neededNRCAIMSssbFreqList.size() != 0) {
                                                                    for(nrcaims in nrcaimss) {
                                                                        currentsCellConfig = nrcaims.getListParameter("sCellConfig");
                                                                        if(!neededNRCAIMSssbFreqList[i].contains(currentsCellConfig[0]["nrarfcnDl"])) {
                                                                            logger.info(" ******************************************** We'll delete the existing NRCAIMS index: " + nrcaims.getParameter("originalId") + " with ssbFreq: " + currentsCellConfig[0]["nrarfcnDl"] + " under the existing NRCAIMP-" + nrcaimp.getParameter("originalId") + " with ssbFreq : " + neededNRCAIMPssbFreqList[i] + " because it's not needed");
                                                                            // ? To protect n25 deletion below line to be commendted out
                                                                            addRecommendation("DELETE", nrcaims, "We'll delete the existing NRCAIMS index: " + nrcaims.getParameter("originalId") + " with ssbFreq: " + currentsCellConfig[0]["nrarfcnDl"] + " under the existing NRCAIMP-" + nrcaimp.getParameter("originalId") + " with ssbFreq : " + neededNRCAIMPssbFreqList[i] + " because it's not needed");
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // ! Delete NRCAIMP ssbFreq that are not in the neededNRCAIMPssbFreqList
                                            // ? Protection to avoid deleting every NRCAIMP if the CPR didn't find any needed NRCAIMP, since on a ABIO, it's highly unlikely that the number of needed NRCAIMP would be empty
                                            if(neededNRCAIMPssbFreqList.size() != 0) {
                                                for(nrcaimp in nrcaimps) {
                                                    currentpCellConfig = nrcaimp.getListParameter("pCellConfig");
                                                    if(!neededNRCAIMPssbFreqList.contains(currentpCellConfig[0]["nrarfcnDl"])){
                                                        logger.info(" ******************************************** We'll delete the existing NRCAIMP-" + nrcaimp.getParameter("originalId") + " with ssbFreq : " + currentpCellConfig[0]["nrarfcnDl"] + " because it's not needed");

                                                        //To protect n25 deletion below line to be commendted out
                                                        addRecommendation("DELETE", nrcaimp, "We'll delete the existing NRCAIMP-" + nrcaimp.getParameter("originalId") + " with ssbFreq : " + currentpCellConfig[0]["nrarfcnDl"] + " because it's not needed");
                                                    }
                                                }
                                            }
                                        } else {
                                            logger.info(" *************** ERROR : Something went wrong with the tables 'neededNRCAIMPssbFreqList' and 'neededNRCAIMSssbFreqList' as they don't have the same size");
                                        }
                                    } else {
                                        logger.info(" *************** ERROR : Something went wrong with the cell reference tables 'cellReferenceTable_listofscell' as it doesn't have the right size");
                                    }
                                } else {
                                    logger.info(" *************** ERROR : Something went wrong with the cell reference tables 'cellReferenceTable_sectorID' as it doesn't have the right size. If SECTORID_METHOD = 'CELLNAME', please verify the cellnames. If SECTORID_METHOD = 'TOPOLOGY_SECTORNAME', please verify NRM");
                                }
                            } else {
                                logger.info(" *************** ERROR : Something went wrong with the cell reference tables 'cellReferenceTable_max_reqMinNumPuschPrb_withCA' as it doesn't have the right size. Probably that one of the cell has an unknown cellTechnology_bw_scs (see the table called 'mapping_cellTechnology_bw_scs')");
                            }
                        } else {
                            logger.info("WARNING : One or more NRCELL had an unknown SSB FREQ, we'll skip the NR analysis.");
                        }
                    } else {
                        logger.info(" ************* WARNING : gNB hardware is not ABIO");
                        for(nrcell in nrcells) {
                            if(nrcell.getParameter("cellDepType") == SA || nrcell.getParameter("cellDepType") == SAplusNSA) {
                                logger.info(" ************************** The parameter NRCELL.idleMMSteeringAlgo is " + nrcell.getParameter("idleMMSteeringAlgo") + " and it should be " + NONCAAWARE);
                                if(nrcell.getParameter("idleMMSteeringAlgo") != NONCAAWARE) {
                                    addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter idleMMSteeringAlgo to " + NONCAAWARE + " on the NRCELL " + nrcellDistname, "idleMMSteeringAlgo", NONCAAWARE);
                                    logger.info(" ************************** UPDATE : Update the parameter idleMMSteeringAlgo to " + NONCAAWARE + " on the NRCELL " + nrcellDistname);
                                }
                            }
                        }
                    }
                } else {
                    logger.info(" ************* WARNING : gNB is NSA only, so we don't manage 5G CA on that older configuration");
                }
            } else {
                logger.info(" ************* WARNING : gNB software is older than 22R1, so we don't manage 5G CA on that older software");
            }
        } else {
            logger.info(" ************* ERROR : gNB doesn't have any NRCELL (size=" + nrcells.size() + ") or gNB doesn't have only one NRDU (size=" + nrdu.size() + ")");
        }
    }
} else {
    logger.info(" ** ERROR : one of these two tables doesn't have the right size : mapping_cellTechnology_bw_scs OR mapping_max_reqMinNumPuschPrb_withCA. We stop the CPR.");
}

logger.info(" **** Number of wrong cellName : " + wrongCellName.size());
for(def i = 0; i < wrongCellName.size(); i++) {
    logger.info(" ******* wrong cellname : " + wrongCellName[i]);
}
logger.info(" **** Number of scell that we were not able to be configure because the NRREL doesn't exist and we were not able to create it due to number of NRREL limit was reached : " + listofcellwithtoomanyNRREL.size());
for(def i = 0; i < listofcellwithtoomanyNRREL.size(); i++) {
    logger.info(" ******* On this NRCELL : " + listofcellwithtoomanyNRREL[i] + ", we were not able to create this scell relation : " + listofunconfiguredscelltoomanyNRREL[i] + " because the NRREL doesn't exist and we were not able to create it due to number of NRREL limit was reached.");
}
logger.info(" **** Number of nrcell with wrong SSB_FREQ: " + cellwithunknownSSBFreq.size());
for(def i = 0; i < cellwithunknownSSBFreq.size(); i++) {
    logger.info(" ******* nrcell with wrong SSB_FREQ : " + cellwithunknownSSBFreq[i]);
}
logger.info(" **** Total number of evaluated objects: NRBTS " + number_of_nrbts);
logger.info(" **** End of " + cpr_name);