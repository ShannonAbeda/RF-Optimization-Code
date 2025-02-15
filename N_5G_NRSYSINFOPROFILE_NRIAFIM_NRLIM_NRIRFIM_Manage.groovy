/*****************************************************************************************************************************************************************************************
 * * This CPR manages the 5G NRSYSINFO_PROFILE, NRIAFIM, NRLIM and NRIRFIM creation/update/deletion.
 * * These objects are needed when we have 5G SA cell.
 * * Only manage gNBs if software is >= 22R1.
 *
 * * VERSION#1 :
 * ?    - NRCELL
 * ?        - Update the parameter nrSysInfoProfileDN if it's wrong
 * ?    - NRSYSINFO_PROFILE
 * ?        - Create if it's missing
 * ?        - Update siWindowLength if it's wrong
 * ?    - NRIAFIM
 * ?        - Create if it's missing
 * ?    - NRLIM
 * ?        - Create if it's missing
 * ?        - Update allowedMeasBw if it's wrong
 * ?        - Update cellReselectionPriority if it's wrong
 *
 * * VERSION#2 :
 * *    - NRCELL
 * *        - Update the parameter cellReselectionPriority if it's wrong
 * *        - Update the parameter idleMMServCelResWeight if it's wrong
 * *    - NRIRFIM
 * *        - Logic to detect which NRIRFIM is needed
 * *            - The NRBAND is only considered "needed" if the NRBAND is in the table listof5Gband
 * *            - Only verify NRBAND from the NRCELL in the same gNB
 * *        - Logic for create/update:
 * *            - Create if it's missing
 * *            - Update ssbScs if it's wrong
 * *            - Update freqBandIndicatorNR if it's wrong
 * *            - Update cellReselectionPriority if it's wrong
 * *            - Update idleMMNrCelResWeight if it's wrong
 * *            - Update numberOfTransmittedSsBlocks if it's wrong
 * *        - Logic to delete NRIRFIM if it's not needed
 * *            - if the NRIRFIM is not in the list of "needed" NRIRFIM
 * *            - we only delete if there is at least 1 item in the "needed" list (avoid deleting all the NRIRFIM if something is wrong with the CPR)
 * *    - NRLIM
 * *        - Added logic to delete NRLIM if it's not needed
 * *            - we only delete if there is at least 1 item in the "needed" list (avoid deleting all the NRLIM if something is wrong with the CPR)
 *
 * * VERSION#3 :
 * ?    - Added wttp_indicator in the reference table, so we'll be able to take decision based on WTTP_IND
 *
 * * VERSION 4: Younes R.
 * ?    - Prevent any changes after version 24 since nrSysInfoProfileId parameter range has been updated

 * * VERSION 5: Shannon Abeda.
 * ?    - Adding VoNR update parameters
                NRSYSINFO_PROFILES:
                -sNonIntraSearchP
                -qHyst
                -threshServingLowP

                NRIAFIM:
                -qRxLevMin
                -sIntraSearchP
                -tReselectionNr
        -Adding NR Function to Determine NR FreqBand





 ****************************************************************************************************************************************************************************************/

//!     THESE TABLES MANAGE THE CPR

NEEDED_IDLEMMSTEERINGPERCENTAGEOFUES = "100";

def listof5Gband                            =   [   "66",   "78-F1",    "5",    "25",   "71",   "78-F2" ];  // * not a parameter
def listofcellpriority                      =   [   "6",    "7",        "4",    "6",    "4",    "7"     ];
def nrsysinfoprofile_indexLIST              =   [   0,      1,          2,      3,      4,      5,      ];
def nrsysinfoprofile_siWindowLengthLIST     =   [   "20",   "40",       "20",   "20",   "20",   "40"    ];
def nrsysinfoprofile_nrlimprofileIDLIST     =   [   0,      0,          1,      0,      1,      0       ];  // * not a parameter //Linked to the table nrlimprofileID
def listofcellweightmethod                  =   [   20,     100,        1,      30,     1,      1       ];  // * Use BW


// OLD LIST
// // listof5Gband =                             ["66",     "78-F1",    "5",    "71",    "78-F2"]; //not a parameter
// // listofcellpriority =                    ["6",    "7",        "4",    "4",    "7"];
// // nrsysinfoprofile_indexLIST =            [0,        1,            2,        4,        5];
// // nrsysinfoprofile_siWindowLengthLIST =    ["20",    "40",        "20",    "20",    "40"];
// // nrsysinfoprofile_nrlimprofileIDLIST =    [0,        0,            1,        1,        0];  //not a parameter //Linked to the table nrlimprofileID
// // listofcellweightmethod =                 [20,    100,        1,        1,        1];    //Use BW

def nrlimprofileID      =   [   0,                                  1                       ];
def nrlimListofLTEBand  =   [   ["B12","B17","B13","B2","B7"],      ["B12","B17","B13"]     ];


// ?                            EARFCN      LOW/HIGH,   BAND,      CELLPRIORITY
def listof4Gbandallinfo = [ [   5010,      5179,       "B12",      4            ],
                            [   5730,      5849,       "B17",      3            ],
                            [   5180,      5279,       "B13",      3            ],
                            [   2400,      2649,       "B5",       4            ],
                            [   9660,      9769,       "B29",      3            ],
                            [   600,       1199,       "B2",       5            ],
                        //  [   8040,      8689,       "B25",      5            ],
                            [   1950,      2399,       "B4",       5            ],
                            [   2750,      3449,       "B7",       5            ],
                            [   41590,     43589,      "B42",      5            ],
                            [   43590,     45589,      "B43",      5            ]];


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// * table used to do a conversion between LNCEL dlChBw and NRLIM allowedMeasBw
def bw_lncel    =   [   50,     100,    150,    200     ];
def bw_nrlim    =   [   25,     50,     75,     100     ];


def cpr_name = "N_5G_NRSYSINFOPROFILE_NRIAFIM_NRLIM_NRIRFIM_Manage";
logger.info(" ************** Beginning of " + cpr_name);


def FDD                         = "0";
def TDD                         = "1";
def NSA                         = "0";
def SA                          = "1";
def SAplusNSA                   = "10";
def FALSE                       = "0";
def TRUE                        = "1";
def NRLIMMAXSIZE                = 8;
def NRIRFIMMAXSIZE              = 8;
def number_of_nrbts             = 0;
def global_unknownband_gNB      = [];
def global_unknownband_band     = [];
def cellwithunknownSSBFreq      = [];


OK_TO_START = true;
if(listof5Gband.size() != listofcellpriority.size()) {
    OK_TO_START = false;
    logger.info(" ERROR : both of these lists doesn't have the same size : listof5Gband and listofcellpriority. We stop the CPR");
}
if(listof5Gband.size() != listofcellweightmethod.size()) {
    OK_TO_START = false;
    logger.info(" ERROR : both of these lists doesn't have the same size : listof5Gband and listofcellweightmethod. We stop the CPR");
}
if(listof5Gband.size() != nrsysinfoprofile_indexLIST.size()) {
    OK_TO_START = false;
    logger.info(" ERROR : both of these lists doesn't have the same size : listof5Gband and nrsysinfoprofile_indexLIST. We stop the CPR");
}
if(listof5Gband.size() != nrsysinfoprofile_siWindowLengthLIST.size()) {
    OK_TO_START = false;
    logger.info(" ERROR : both of these lists doesn't have the same size : listof5Gband and nrsysinfoprofile_siWindowLengthLIST. We stop the CPR");
}
if(listof5Gband.size() != nrsysinfoprofile_nrlimprofileIDLIST.size()) {
    OK_TO_START = false;
    logger.info(" ERROR : both of these lists doesn't have the same size : listof5Gband and nrsysinfoprofile_nrlimprofileIDLIST. We stop the CPR");
}
if(nrlimprofileID.size() != nrlimListofLTEBand.size()) {
    OK_TO_START = false;
    logger.info(" ERROR : both of these lists doesn't have the same size : nrlimprofileID and nrlimListofLTEBand. We stop the CPR");
}

if(OK_TO_START) {
    for(nrbts in _context.getMos("NRBTS")) {
        number_of_nrbts++;

        def nrcells             = nrbts.getMos("NRCELL");
        def nrcellgrps          = nrbts.getMos("NRCELLGRP");
        def nrsysinfoprofiles   = nrbts.getMos("NRSYSINFO_PROFILE");
        def lncels_samesite     = nrcells.getMos("LNCEL");          // *  get the LNCEL samesite

        def currentnrsysinfoprofileIndex         = [];
        def needednrsysinfoprofileIndex          = [];
        def needednrsysinfoprofileSiWindowLength = [];
        def needednrlimearfcn                    = [];
        def needednrlimBW                        = [];
        def needednrlimprio                      = [];
        def needednrirfimNrfreq                  = [];
        def needednrirfimSCS                     = [];
        def needednrirfimband                    = [];
        def needednrirfimprio                    = [];
        def needednrirfimweight                  = [];
        def needednrirfimnbrSSBlock              = [];
        def unknown_band                         = [];

        nrbtsDistname    = nrbts.getParameter("distName").substring(10);
        gNbCuType        = nrbts.getParameter("gNbCuType");
        Integer versionInt = 0;
        versionRealString = nrbts.getParameter("version");                              // * Here, versionRealString should look like NRBTSCL5G21B_2105_200 or NRBTSCL22R1_2109_102
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
        logger.info(" ******** We are evaluating this gNB " + nrbtsDistname + " with gNB CU type : " + gNbCuType + " on this 5G version : " + versionString);



        if(nrcells.size() != 0) {
            if(24 > versionInt && versionInt >= 22) {
                if(gNbCuType == SA || gNbCuType == SAplusNSA) {

                    /*******************************************************************************************************************************************************************/
                    /************ STEP#1a Loop through all the NRCELLs to build reference tables to help with IMLB decision ************************************************************/
                    /*******************************************************************************************************************************************************************/

                    logger.info(" ************* STEP#1a Loop through all the NRCELLs to build reference tables to help with IMLB decision");
                    def cellReferenceTable_cellName                     = [];
                    def cellReferenceTable_lcrId                        = [];
                    def cellReferenceTable_cellTechnology               = [];
                    def cellReferenceTable_cellDepType                  = [];
                    def cellReferenceTable_freqBandIndicatorNR          = [];
                    def cellReferenceTable_nrarfcn                      = [];
                    def cellReferenceTable_bw                           = [];
                    def cellReferenceTable_gscn                         = [];
                    def cellReferenceTable_ssbFrequency                 = [];
                    def cellReferenceTable_scs                          = [];
                    def cellReferenceTable_numberOfTransmittedSsBlocks  = [];
                    def cellReferenceTable_wttpind                      = [];
                    def cellReferenceTable_priority                     = [];
                    def cellReferenceTable_weight                       = [];
                    def cellReferenceTable_sysinfoprofile               = [];
                    def cellReferenceTable_siWindowLength               = [];
                    def cellReferenceTable_NRLIMallowed                 = [];
                    def cellReferenceTable_4G_earfcn                    = [];
                    def cellReferenceTable_4G_bw                        = [];
                    def cellReferenceTable_4G_prio                      = [];
                    def cellReferenceTable_4G_band                      = [];
                    def N78_ssbFreq_list                                = [];
                    def N78_bw_list                                     = [];
                    def UNKNOWN_SSBFREQ                                 = false;

                    for(nrcell in nrcells) {
                        if(nrcell.getParameter("cellTechnology") == FDD) {
                            nrarfcn_integer = nrcell.getMos("NRCELL_FDD").getParameterAsInteger("nrarfcnDl");
                            bw_integer = nrcell.getMos("NRCELL_FDD").getParameterAsInteger("chBwDl");
                        }
                        else {
                            nrarfcn_integer = nrcell.getParameterAsInteger("nrarfcn");
                            bw_integer = nrcell.getParameterAsInteger("chBw");
                        }
                        gscn_integer = nrcell.getParameterAsInteger("gscnOrSsPbchArfcn");
                        arfcnSsbPbch = nrcell.getParameter("arfcnSsbPbch");
                        logger.info("We are analysing the NRCELL : " + nrcell.getParameter("cellName") + " with the arfcnSsbPbch : " + arfcnSsbPbch + " with the gscn_integer : " + gscn_integer +  " with the nrarfcn_integer : " + nrarfcn_integer);
                        if((gscn_integer != null && gscn_integer > 2) || (arfcnSsbPbch != null && arfcnSsbPbch != "-1")) {
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
                            }
                            else {
                                ssbfreq_integer = Integer.parseInt(arfcnSsbPbch);
                                logger.info("SSBFREQ with parameter : ssbfreq_integer = " + arfcnSsbPbch);
                            }
                            cellReferenceTable_ssbFrequency.add(ssbfreq_integer.toString());
                            cellReferenceTable_cellName.add(nrcell.getParameter("cellName"))
                            cellReferenceTable_lcrId.add(nrcell.getParameter("lcrId"));
                            cellReferenceTable_cellTechnology.add(nrcell.getParameter("cellTechnology"));
                            cellReferenceTable_cellDepType.add(nrcell.getParameter("cellDepType"));
                            cellReferenceTable_freqBandIndicatorNR.add(nrcell.getParameter("freqBandIndicatorNR"));
                            cellReferenceTable_scs.add(nrcell.getParameter("ssbScs"));
                            cellReferenceTable_nrarfcn.add(nrarfcn_integer.toString());
                            cellReferenceTable_bw.add(bw_integer.toString());
                            cellReferenceTable_gscn.add(gscn_integer.toString());
                            cellReferenceTable_numberOfTransmittedSsBlocks.add([]);

                            if(nrcell.getPhysicalParameter("WTTP_IND") == null) {
                                cellReferenceTable_wttpind.add("NO");
                            } else {
                                cellReferenceTable_wttpind.add(nrcell.getPhysicalParameter("WTTP_IND"));
                            }

                            //? Build list of unique N78 ssbFreq
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
                        //? N78 differenciation : N78-F1 is the highest BW. If both N78 have similar BW, N78-F1 will be the lowest ssbFreq
                        if(N78_ssbFreq_list.size() != 0) {
                            if(N78_ssbFreq_list.size() == 1) {                      //? If only one N78 carrier
                                N78_F1_ssbFreq = N78_ssbFreq_list[0].toString();
                            } else {                                                //? More than 1 N78 carrier
                                if(N78_bw_list[0] > N78_bw_list[1]) {               //? If the first carrier has the highest BW
                                    N78_F1_ssbFreq = N78_ssbFreq_list[0].toString();
                                } else if(N78_bw_list[1] > N78_bw_list[0]) {        //? If the second carrier has the highest BW
                                    N78_F1_ssbFreq = N78_ssbFreq_list[1].toString();
                                } else {                                            //? If the both carriers have similar BW
                                    if(N78_ssbFreq_list[0] < N78_ssbFreq_list[1]) { //? If the first carrier has the lowest ssbFreq
                                        N78_F1_ssbFreq = N78_ssbFreq_list[0].toString();
                                    } else {                                        //? If the second carrier has the lowest ssbFreq
                                        N78_F1_ssbFreq = N78_ssbFreq_list[1].toString();
                                    }
                                }
                            }
                            logger.info(" *** N78_ssbFreq_list is : " + N78_ssbFreq_list);
                            logger.info(" *** N78_bw_list is : " + N78_bw_list);
                            logger.info(" *** N78_F1_ssbFreq is : " + N78_F1_ssbFreq);
                            for(i = 0 ; i < cellReferenceTable_lcrId .size(); i++) {
                                if(cellReferenceTable_freqBandIndicatorNR[i] == "78") {
                                    if(cellReferenceTable_ssbFrequency[i] == N78_F1_ssbFreq) {
                                        cellReferenceTable_freqBandIndicatorNR[i] = "78-F1";
                                    } else {
                                        cellReferenceTable_freqBandIndicatorNR[i] = "78-F2";
                                    }
                                }
                            }
                        }

                        //? Quick verification to see if all the band in cellReferenceTable_freqBandIndicatorNR are included in listof5Gband
                        for(i = 0 ; i < cellReferenceTable_freqBandIndicatorNR.size(); i++) {
                            if(!listof5Gband.contains(cellReferenceTable_freqBandIndicatorNR[i])) {
                                unknown_band.add(cellReferenceTable_freqBandIndicatorNR[i]);
                            }
                        }
                        //? If there is an unknown_band, we don't analyse the gNB
                        if(unknown_band.size() == 0) {
                            //? Complete the cellReferenceTable_numberOfTransmittedSsBlocks
                            for(nrcellgrp in nrcellgrps) {
                                nrcellgrpDistname = nrcellgrp.getParameter("distName").substring(10);
                                nrCellList = nrcellgrp.getListEnumerationParameter("nrCellList")[0];
                                logger.info(" Evaluating NRCELLGRP : " + nrcellgrpDistname + " with nrCellList : " + nrCellList);
                                if(cellReferenceTable_lcrId.contains(nrCellList)) {
                                    cellReferenceTable_numberOfTransmittedSsBlocks[cellReferenceTable_lcrId.indexOf(nrCellList)] = nrcellgrp.getParameter("numberOfTransmittedSsBlocks");
                                }
                            }

                            //? Complete the cellReferenceTable_priority, cellReferenceTable_weight, cellReferenceTable_sysinfoprofile, cellReferenceTable_siWindowLength, cellReferenceTable_NRLIMallowed
                            for(i = 0 ; i < cellReferenceTable_freqBandIndicatorNR.size(); i++) {
                                temp_position = listof5Gband.indexOf(cellReferenceTable_freqBandIndicatorNR[i])
                                cellReferenceTable_priority.add(listofcellpriority[temp_position]);
                                cellReferenceTable_sysinfoprofile.add(nrsysinfoprofile_indexLIST[temp_position])
                                cellReferenceTable_siWindowLength.add(nrsysinfoprofile_siWindowLengthLIST[temp_position]);
                                cellReferenceTable_NRLIMallowed.add(nrlimListofLTEBand[nrlimprofileID.indexOf(nrsysinfoprofile_nrlimprofileIDLIST[temp_position])]);
                                weight_method = listofcellweightmethod[temp_position];

                                if(weight_method == null) {
                                    cellReferenceTable_weight.add(weight_method);
                                    logger.info("cellReferenceTable_freqBandIndicatorNR[i] is : " + cellReferenceTable_freqBandIndicatorNR[i] + " and weight_method is : " + weight_method + ". The weight will be : " + weight_method);
                                } else if(weight_method == "BW") {
                                    cellReferenceTable_weight.add(cellReferenceTable_bw[i]);
                                    logger.info("cellReferenceTable_freqBandIndicatorNR[i] is : " + cellReferenceTable_freqBandIndicatorNR[i] + " and weight_method is : " + weight_method + ". The weight will be : " + cellReferenceTable_bw[i]);
                                } else if(weight_method > 0) {
                                    cellReferenceTable_weight.add(weight_method.toString());
                                    logger.info("cellReferenceTable_freqBandIndicatorNR[i] is : " + cellReferenceTable_freqBandIndicatorNR[i] + " and weight_method is : " + weight_method + ". The weight will be : " + weight_method.toString());
                                } else {
                                    cellReferenceTable_weight.add(null);
                                    logger.info("cellReferenceTable_freqBandIndicatorNR[i] is : " + cellReferenceTable_freqBandIndicatorNR[i] + " but the weight_method is unkwown because it's not null, 'BW' or an integer (weight_method: " + weight_method + "). The weight will be null");
                                }
                            }

                            //? Complete the cellReferenceTable_4G_earfcn, cellReferenceTable_4G_bw, cellReferenceTable_4G_prio
                            for(eachlncel in lncels_samesite) {
                                tempcellName = eachlncel.getParameter("cellName");
                                tempcellTechnology = eachlncel.getParameter("cellTechnology");

                                logger.info(" ********************************* We are evaluating this LTE cell " + tempcellName + " with cellTechnology " + tempcellTechnology);
                                if(tempcellTechnology == FDD || tempcellTechnology == TDD) {    //? NB-IOT protection
                                    if(tempcellTechnology == FDD) {
                                        tempEarfcn = eachlncel.getMos("LNCEL_FDD").getParameterAsInteger("earfcnDL");
                                        tempBW = eachlncel.getMos("LNCEL_FDD").getParameterAsInteger("dlChBw");
                                    } else {
                                        tempEarfcn = eachlncel.getMos("LNCEL_TDD").getParameterAsInteger("earfcn");
                                        tempBW = eachlncel.getMos("LNCEL_TDD").getParameterAsInteger("chBw");
                                    }

                                    logger.info(" *********************************** The tempEarfcn is " + tempEarfcn + " and the BW is " + tempBW);
                                    if(tempEarfcn != null && tempBW != null) {
                                        for(def i = 0; i < listof4Gbandallinfo.size(); i++) {
                                            //? If it's a known LTE BAND
                                            if(listof4Gbandallinfo[i][0] <= tempEarfcn && listof4Gbandallinfo[i][1] >= tempEarfcn) {
                                                tempBand = listof4Gbandallinfo[i][2];
                                                temp4Gprio = listof4Gbandallinfo[i][3];
                                                logger.info(" ************************************* The band of this cell is " + tempBand + " and the 4G cellpriority is " + temp4Gprio);

                                                if(!cellReferenceTable_4G_earfcn.contains(tempEarfcn)) {
                                                    cellReferenceTable_4G_earfcn.add(tempEarfcn);
                                                    cellReferenceTable_4G_bw.add(bw_nrlim[bw_lncel.indexOf(tempBW)]); //Conversion between LNCEL BW to NRLIM BW
                                                    cellReferenceTable_4G_prio.add(temp4Gprio);
                                                    cellReferenceTable_4G_band.add(tempBand);
                                                }
                                                break;
                                            }
                                        }
                                    } else {
                                        logger.info(" ERROR : This 4G cell has a null EARFCN or BW");
                                    }
                                } else {
                                    logger.info(" ERROR : This 4G cell is not FDD or TDD... maybe NB-IOT ?");
                                }
                            }

                            logger.info(" ************* cellReferenceTable_cellName : " + cellReferenceTable_cellName);
                            logger.info(" ************* cellReferenceTable_lcrId : " + cellReferenceTable_lcrId);
                            logger.info(" ************* cellReferenceTable_cellTechnology : " + cellReferenceTable_cellTechnology);
                            logger.info(" ************* cellReferenceTable_cellDepType : " + cellReferenceTable_cellDepType);
                            logger.info(" ************* cellReferenceTable_freqBandIndicatorNR : " + cellReferenceTable_freqBandIndicatorNR);
                            logger.info(" ************* cellReferenceTable_ssbFrequency : " + cellReferenceTable_ssbFrequency);
                            logger.info(" ************* cellReferenceTable_nrarfcn : " + cellReferenceTable_nrarfcn);
                            logger.info(" ************* cellReferenceTable_bw : " + cellReferenceTable_bw);
                            logger.info(" ************* cellReferenceTable_gscn : " + cellReferenceTable_gscn);
                            logger.info(" ************* cellReferenceTable_scs : " + cellReferenceTable_scs);
                            logger.info(" ************* cellReferenceTable_numberOfTransmittedSsBlocks : " + cellReferenceTable_numberOfTransmittedSsBlocks);
                            logger.info(" ************* cellReferenceTable_wttpind : " + cellReferenceTable_wttpind);
                            logger.info(" ************* cellReferenceTable_priority : " + cellReferenceTable_priority);
                            logger.info(" ************* cellReferenceTable_weight : " + cellReferenceTable_weight);
                            logger.info(" ************* cellReferenceTable_sysinfoprofile : " + cellReferenceTable_sysinfoprofile);
                            logger.info(" ************* cellReferenceTable_siWindowLength : " + cellReferenceTable_siWindowLength);
                            logger.info(" ************* cellReferenceTable_NRLIMallowed : " + cellReferenceTable_NRLIMallowed);
                            logger.info(" ************* cellReferenceTable_4G_earfcn : " + cellReferenceTable_4G_earfcn);
                            logger.info(" ************* cellReferenceTable_4G_bw : " + cellReferenceTable_4G_bw);
                            logger.info(" ************* cellReferenceTable_4G_prio : " + cellReferenceTable_4G_prio);
                            logger.info(" ************* cellReferenceTable_4G_band : " + cellReferenceTable_4G_band);


                            // * Build the list of needed NRSYSINFO_PROFILE, NRLIM, NRIRFIM
                            for(i = 0 ; i < cellReferenceTable_lcrId.size(); i++) {
                                if(!needednrsysinfoprofileIndex.contains(cellReferenceTable_sysinfoprofile[i])) {
                                    //! For NRLIM, get the 4G earfcn, BW, priority
                                    needed4GListOfEarfcn    = [];
                                    needed4GListOfBW        = [];
                                    needed4GListOfPrio      = [];
                                    for(j = 0 ; j < cellReferenceTable_4G_earfcn.size(); j++) {
                                        if(cellReferenceTable_NRLIMallowed[i].contains(cellReferenceTable_4G_band[j])) {
                                            if(!needed4GListOfEarfcn.contains(cellReferenceTable_4G_earfcn[j])) {
                                                needed4GListOfEarfcn.add(cellReferenceTable_4G_earfcn[j]);
                                                needed4GListOfBW.add(cellReferenceTable_4G_bw[j]);
                                                needed4GListOfPrio.add(cellReferenceTable_4G_prio[j]);
                                            }
                                        }
                                    }

                                    //! For NRIRFIM, get the 5G ssbFreq, SCS, priority, weight, band, nbrSSBlock
                                    needed5GListOfNrfreq        = [];
                                    needed5GListOfSCS           = [];
                                    needed5GListOfBand          = [];
                                    needed5GListOfPrio          = [];
                                    needed5GListOfWeight        = [];
                                    needed5GListOfNbrSSBlock    = [];
                                    for(j = 0 ; j < cellReferenceTable_lcrId.size(); j++) {
                                        if(cellReferenceTable_ssbFrequency[i] != cellReferenceTable_ssbFrequency[j]) {
                                            if((cellReferenceTable_cellDepType[j] == SA || cellReferenceTable_cellDepType[j] == SAplusNSA)) {
                                                if(!needed5GListOfNrfreq.contains(cellReferenceTable_ssbFrequency[j])) {
                                                    needed5GListOfNrfreq.add(cellReferenceTable_ssbFrequency[j]);
                                                    needed5GListOfSCS.add(cellReferenceTable_scs[j]);
                                                    needed5GListOfBand.add(cellReferenceTable_freqBandIndicatorNR[j].split("-")[0]);
                                                    needed5GListOfPrio.add(cellReferenceTable_priority[j]);
                                                    needed5GListOfWeight.add(cellReferenceTable_weight[j]);
                                                    needed5GListOfNbrSSBlock.add(cellReferenceTable_numberOfTransmittedSsBlocks[j]);
                                                }
                                            }
                                        }
                                    }

                                    //! Add information to the "needed" table
                                    needednrsysinfoprofileIndex.add(cellReferenceTable_sysinfoprofile[i]);
                                    needednrsysinfoprofileSiWindowLength.add(cellReferenceTable_siWindowLength[i]);
                                    needednrlimearfcn.add(needed4GListOfEarfcn);
                                    needednrlimBW.add(needed4GListOfBW);
                                    needednrlimprio.add(needed4GListOfPrio);
                                    needednrirfimNrfreq.add(needed5GListOfNrfreq);
                                    needednrirfimSCS.add(needed5GListOfSCS);
                                    needednrirfimband.add(needed5GListOfBand);
                                    needednrirfimprio.add(needed5GListOfPrio);
                                    needednrirfimweight.add(needed5GListOfWeight);
                                    needednrirfimnbrSSBlock.add(needed5GListOfNbrSSBlock);
                                }
                            }

                            // * Here we have the list of things that we need (NRSYSINFO_PROFILE, NRLIM, NRIRFIM)
                            logger.info(" *********** Here is the list of needed index for NRSYSINFO_PROFILE : " + needednrsysinfoprofileIndex);
                            logger.info(" *********** Here is the list of needed siWindowLength for NRSYSINFO_PROFILE : " + needednrsysinfoprofileSiWindowLength);
                            logger.info(" *********** Here is the list of needed EARFCN for NRLIM : " + needednrlimearfcn);
                            logger.info(" *********** Here is the list of needed BW for NRLIM : " + needednrlimBW);
                            logger.info(" *********** Here is the list of needed PRIO for NRLIM : " + needednrlimprio);
                            logger.info(" *********** Here is the list of needed NR Freq for NRIRFIM : " + needednrirfimNrfreq);
                            logger.info(" *********** Here is the list of needed SCS for NRIRFIM : " + needednrirfimSCS);
                            logger.info(" *********** Here is the list of needed BANDindicator for NRIRFIM : " + needednrirfimband);
                            logger.info(" *********** Here is the list of needed PRIO for NRIRFIM : " + needednrirfimprio);
                            logger.info(" *********** Here is the list of needed Weight for NRIRFIM : " + needednrirfimweight);
                            logger.info(" *********** Here is the list of needed NbrSSBlock for NRIRFIM : " + needednrirfimnbrSSBlock);


                            // * Build the list of current NRSYSINFO_PROFILE
                            for(nrsysinfoprofile in nrsysinfoprofiles) {
                                currentnrsysinfoprofileIndex.add(nrsysinfoprofile.getParameterAsInteger("originalId"));
                                logger.info(" ********************** We have a NRSYSINFO_PROFILE-" + nrsysinfoprofile.getParameterAsInteger("originalId"));
                            }
                            logger.info(" *********** Here is the list of current index for NRSYSINFO_PROFILE : " + currentnrsysinfoprofileIndex);


                            //! This huge section below the 'for loop' will compare all the needed vs current NRSYSINFO_PROFILE, and it will create/update/delete everything related to NRSYSINFO_PROFILE, NRLIM, NRIRFIM accordingly
                            for(def i = 0; i < needednrsysinfoprofileIndex.size(); i++) {
                                neededIndex = needednrsysinfoprofileIndex[i];
                                // Create NRSYSINFO_PROFILE/NRLIM if Missing
                                if(!currentnrsysinfoprofileIndex.contains(neededIndex)) {
                                    def newnrsysinfoprofile = buildMo("NRSYSINFO_PROFILE", nrbts, neededIndex);
                                    newnrsysinfoprofile.setParameterValue("defaults", "Default_5G");
                                    newnrsysinfoprofile.setParameterValue("siWindowLength", needednrsysinfoprofileSiWindowLength[i]);
                                    addRecommendation("CREATE", newnrsysinfoprofile, "CREATE : Create NRSYSINFO_PROFILE with index : " + neededIndex + " and siWindowLength : " + needednrsysinfoprofileSiWindowLength[i]);
                                    logger.info(" ********************** CREATE : Create NRSYSINFO_PROFILE with index : " + neededIndex + " and siWindowLength : " + needednrsysinfoprofileSiWindowLength[i]);
                                    def newnriafim = buildMo("NRIAFIM", newnrsysinfoprofile, 1);
                                    newnriafim.setParameterValue("defaults", "Default_5G");
                                    addRecommendation("CREATE", newnriafim, "CREATE : Create NRIAFIM-1 under the newly created NRSYSINFO_PROFILE-" + neededIndex);
                                    logger.info(" ********************** CREATE : Create NRIAFIM-1 under the newly created NRSYSINFO_PROFILE-" + neededIndex);
                                    for(def j = 0; j < needednrlimearfcn[i].size(); j++) {
                                        def newnrlim = buildMo("NRLIM", newnrsysinfoprofile, "1-"+(NRLIMMAXSIZE));
                                        newnrlim.setParameterValue("defaults", "Default_5G");
                                        newnrlim.setParameterValue("carrierFreq", needednrlimearfcn[i][j]);
                                        newnrlim.setParameterValue("allowedMeasBw", needednrlimBW[i][j]);
                                        newnrlim.setParameterValue("cellReselectionPriority", needednrlimprio[i][j]);
                                        addRecommendation("CREATE", newnrlim, "CREATE : Create NRLIM (under the newly created NRSYSINFO_PROFILE-" + neededIndex + ") with carrierFreq : " +  needednrlimearfcn[i][j] + " and allowedMeasBw : " + needednrlimBW[i][j] + " and cellReselectionPriority : " + needednrlimprio[i][j]);
                                        logger.info(" ********************** CREATE : Create NRLIM (under the newly created NRSYSINFO_PROFILE-" + neededIndex + ") with carrierFreq : " +  needednrlimearfcn[i][j] + " and allowedMeasBw : " + needednrlimBW[i][j] + " and cellReselectionPriority : " + needednrlimprio[i][j]);
                                    }
                                    for(def j = 0; j < needednrirfimNrfreq[i].size(); j++) {
                                        def newnrirfim = buildMo("NRIRFIM", newnrsysinfoprofile, "1-"+(NRIRFIMMAXSIZE));
                                        newnrirfim.setParameterValue("defaults", "Default_5G");
                                        newnrirfim.setParameterValue("dlCarrierFreq", needednrirfimNrfreq[i][j]);
                                        newnrirfim.setParameterValue("ssbScs", needednrirfimSCS[i][j]);
                                        newnrirfim.setParameterValue("freqBandIndicatorNR", needednrirfimband[i][j]);
                                        newnrirfim.setParameterValue("cellReselectionPriority", needednrirfimprio[i][j]);
                                        if(needednrirfimweight[i][j] != null) {
                                            newnrirfim.setParameterValue("idleMMNrCelResWeight", needednrirfimweight[i][j]);
                                        }
                                        newnrirfim.setParameterValue("numberOfTransmittedSsBlocks", needednrirfimnbrSSBlock[i][j]);
                                        addRecommendation("CREATE", newnrirfim, "CREATE : Create NRIRFIM (under the newly created NRSYSINFO_PROFILE-" + neededIndex + ") with dlCarrierFreq : " +  needednrirfimNrfreq[i][j] + " and ssbScs : " + needednrirfimSCS[i][j] + " and freqBandIndicatorNR : " + needednrirfimband[i][j] + " and cellReselectionPriority : " + needednrirfimprio[i][j] + " and idleMMNrCelResWeight : " + needednrirfimweight[i][j] + " and numberOfTransmittedSsBlocks : " + needednrirfimnbrSSBlock[i][j]);
                                        logger.info(" ********************** CREATE : Create NRIRFIM (under the newly created NRSYSINFO_PROFILE-" + neededIndex + ") with dlCarrierFreq : " +  needednrirfimNrfreq[i][j] + " and ssbScs : " + needednrirfimSCS[i][j] + " and freqBandIndicatorNR : " + needednrirfimband[i][j] + " and cellReselectionPriority : " + needednrirfimprio[i][j] + " and idleMMNrCelResWeight : " + needednrirfimweight[i][j] + " and numberOfTransmittedSsBlocks : " + needednrirfimnbrSSBlock[i][j]);
                                    }
                                }
                                else {
                                    logger.info(" ********************** The needed NRSYSINFO_PROFILE-" + neededIndex + " exists ! We'll verify if other things are correctly configured.");
                                    for(nrsysinfoprofile in nrsysinfoprofiles) {
                                        if(nrsysinfoprofile.getParameterAsInteger("originalId") == neededIndex) {

                                            //Update the parameter siWindowLength if it's wrong
                                            logger.info(" ************************* The parameter siWindowLength is " + nrsysinfoprofile.getParameter("siWindowLength") + " and it should be " + needednrsysinfoprofileSiWindowLength[i]);
                                            if(nrsysinfoprofile.getParameter("siWindowLength") != needednrsysinfoprofileSiWindowLength[i]) {
                                                addRecommendation("UPDATE", nrsysinfoprofile, "Update the parameter siWindowLength to " + needednrsysinfoprofileSiWindowLength[i] + " on the existing NRSYSINFO_PROFILE-" + neededIndex, "siWindowLength", needednrsysinfoprofileSiWindowLength[i]);
                                                logger.info(" ****************************** UPDATE : Update the parameter siWindowLength to " + needednrsysinfoprofileSiWindowLength[i] + " on the existing NRSYSINFO_PROFILE-" + neededIndex);
                                            }

                                            //Verify and create NRIAFIM if needed
                                            nriafim = nrsysinfoprofile.getMos("NRIAFIM");  //Only one NRIAFIM MO possible
                                            logger.info(" ************************* Here is the number of current NRIAFIM MO : " + nriafim.size());
                                            if(nriafim.size() == 0) {
                                                def newnriafim = buildMo("NRIAFIM", nrsysinfoprofile, 1);
                                                newnriafim.setParameterValue("defaults", "Default_5G");
                                                addRecommendation("CREATE", newnriafim, "CREATE : Create NRIAFIM-1 under the existing NRSYSINFO_PROFILE-" + neededIndex);
                                                logger.info(" ****************************** CREATE : Create NRIAFIM-1 under the existing NRSYSINFO_PROFILE-" + neededIndex);
                                            }


                                            //Verify and create/update/delete NRLIM if needed
                                            nrlims = nrsysinfoprofile.getMos("NRLIM");
                                            currentnrlimearfcn = [];
                                            for(nrlim in nrlims) {
                                                currentnrlimearfcn.add(nrlim.getParameterAsInteger("carrierFreq"));
                                                logger.info(" ************************* We have a NRLIM with this earfcn : " + nrlim.getParameterAsInteger("carrierFreq"));
                                            }
                                            logger.info(" ************************* Here is the list of current earfcn for NRLIM : " + currentnrlimearfcn);
                                            //CREATE/UPDATE
                                            for(def j = 0; j < needednrlimearfcn[i].size(); j++) {
                                                neededEarfcn = needednrlimearfcn[i][j];
                                                //CREATE NRLIM if Missing
                                                if(!currentnrlimearfcn.contains(neededEarfcn)) {
                                                    def newnrlim = buildMo("NRLIM", nrsysinfoprofile, "1-"+(NRLIMMAXSIZE));
                                                    newnrlim.setParameterValue("defaults", "Default_5G");
                                                    newnrlim.setParameterValue("carrierFreq", needednrlimearfcn[i][j]);
                                                    newnrlim.setParameterValue("allowedMeasBw", needednrlimBW[i][j]);
                                                    newnrlim.setParameterValue("cellReselectionPriority", needednrlimprio[i][j]);
                                                    addRecommendation("CREATE", newnrlim, "CREATE : Create NRLIM (under the existing NRSYSINFO_PROFILE-" + neededIndex + ") with carrierFreq : " +  needednrlimearfcn[i][j] + " and carrierFreq : " + needednrlimBW[i][j] + " and cellReselectionPriority : " + needednrlimprio[i][j]);
                                                    logger.info(" ****************************** CREATE : Create NRLIM (under the existing NRSYSINFO_PROFILE-" + neededIndex + ") with carrierFreq : " +  needednrlimearfcn[i][j] + " and carrierFreq : " + needednrlimBW[i][j] + " and cellReselectionPriority : " + needednrlimprio[i][j]);
                                                }
                                                //UPDATE
                                                else {
                                                    logger.info(" ****************************** The needed NRLIM with earfcn " + neededEarfcn + " exists ! We'll verify if other things are correctly configured.");
                                                    for(nrlim in nrlims) {
                                                        if(nrlim.getParameterAsInteger("carrierFreq") == neededEarfcn) {

                                                            nrlimDISTNAME = nrlim.getParameter("distName").substring(10);

                                                            //Update the parameter allowedMeasBw if it's wrong
                                                            logger.info(" ******************************** The parameter allowedMeasBw is " + nrlim.getParameterAsInteger("allowedMeasBw") + " and it should be " + needednrlimBW[i][j]);
                                                            if(nrlim.getParameterAsInteger("allowedMeasBw") != needednrlimBW[i][j]) {
                                                                addRecommendation("UPDATE", nrlim, "Update the parameter allowedMeasBw to " + needednrlimBW[i][j] + " on the NRLIM " + nrlimDISTNAME, "allowedMeasBw", needednrlimBW[i][j]);
                                                                logger.info(" ********************************** UPDATE : Update the parameter allowedMeasBw to " + needednrlimBW[i][j] + " on the NRLIM " + nrlimDISTNAME);
                                                            }

                                                            //Update the parameter cellReselectionPriority if it's wrong
                                                            logger.info(" ******************************** The parameter cellReselectionPriority is " + nrlim.getParameterAsInteger("cellReselectionPriority") + " and it should be " + needednrlimprio[i][j]);
                                                            if(nrlim.getParameterAsInteger("cellReselectionPriority") != needednrlimprio[i][j]) {
                                                                addRecommendation("UPDATE", nrlim, "Update the parameter cellReselectionPriority to " + needednrlimprio[i][j] + " on the NRLIM " + nrlimDISTNAME, "cellReselectionPriority", needednrlimprio[i][j]);
                                                                logger.info(" ********************************** UPDATE : Update the parameter cellReselectionPriority to " + needednrlimprio[i][j] + " on the NRLIM " + nrlimDISTNAME);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            //DELETE
                                            //IMPORTANT : Protection to avoid deleting all the NRLIM if there is no needed NRLIM because we should always have at least one LTE BAND available (maybe it's caused by a wrong lncels_samesite query)
                                            if(needednrlimearfcn[i].size() != 0) {
                                                for(nrlim in nrlims) {
                                                    currentEarfcn = nrlim.getParameterAsInteger("carrierFreq");
                                                    nrlimDISTNAME = nrlim.getParameter("distName").substring(10);
                                                    if(!needednrlimearfcn[i].contains(currentEarfcn)) {
                                                        addRecommendation("DELETE", nrlim, "Delete " + nrlimDISTNAME + " with EARFCN: " + currentEarfcn + " that is not in the super needednrlimearfcn in the subarray #" + i);
                                                        logger.info(" ******************************************** Delete " + nrlimDISTNAME + " with EARFCN: " + currentEarfcn + " that is not in the super needednrlimearfcn in the subarray #" + i);
                                                    }
                                                }
                                            }






                                            //Verify and create/update/delete NRIRFIM if needed
                                            nrirfims = nrsysinfoprofile.getMos("NRIRFIM");
                                            currentnrirfimNrfreq = [];
                                            for(nrirfim in nrirfims) {
                                                currentnrirfimNrfreq.add(nrirfim.getParameter("dlCarrierFreq"));
                                                logger.info(" ************************* We have a NRIRFIM with this ssbFreq : " + nrirfim.getParameter("dlCarrierFreq"));
                                            }
                                            logger.info(" ************************* Here is the list of current ssbFreq for NRIRFIM : " + currentnrirfimNrfreq);
                                            //CREATE/UPDATE
                                            for(def j = 0; j < needednrirfimNrfreq[i].size(); j++) {
                                                neededNRfreq = needednrirfimNrfreq[i][j];
                                                //CREATE NRIRFIM if Missing
                                                if(!currentnrirfimNrfreq.contains(neededNRfreq)) {
                                                    def newnrirfim = buildMo("NRIRFIM", nrsysinfoprofile, "1-"+(NRIRFIMMAXSIZE));
                                                    newnrirfim.setParameterValue("defaults", "Default_5G");
                                                    newnrirfim.setParameterValue("dlCarrierFreq", needednrirfimNrfreq[i][j]);
                                                    newnrirfim.setParameterValue("ssbScs", needednrirfimSCS[i][j]);
                                                    newnrirfim.setParameterValue("freqBandIndicatorNR", needednrirfimband[i][j]);
                                                    newnrirfim.setParameterValue("cellReselectionPriority", needednrirfimprio[i][j]);
                                                    if(needednrirfimweight[i][j] != null) {
                                                        newnrirfim.setParameterValue("idleMMNrCelResWeight", needednrirfimweight[i][j]);
                                                    }
                                                    newnrirfim.setParameterValue("numberOfTransmittedSsBlocks", needednrirfimnbrSSBlock[i][j]);
                                                    addRecommendation("CREATE", newnrirfim, "CREATE : Create NRIRFIM (under the existing NRSYSINFO_PROFILE-" + neededIndex + ") with dlCarrierFreq : " +  needednrirfimNrfreq[i][j] + " and ssbScs : " + needednrirfimSCS[i][j] + " and freqBandIndicatorNR : " + needednrirfimband[i][j] + " and cellReselectionPriority : " + needednrirfimprio[i][j] + " and idleMMNrCelResWeight : " + needednrirfimweight[i][j] + " and numberOfTransmittedSsBlocks : " + needednrirfimnbrSSBlock[i][j]);
                                                    logger.info(" ****************************** CREATE : Create NRIRFIM (under the existing NRSYSINFO_PROFILE-" + neededIndex + ") with dlCarrierFreq : " +  needednrirfimNrfreq[i][j] + " and ssbScs : " + needednrirfimSCS[i][j] + " and freqBandIndicatorNR : " + needednrirfimband[i][j] + " and cellReselectionPriority : " + needednrirfimprio[i][j] + " and idleMMNrCelResWeight : " + needednrirfimweight[i][j] + " and numberOfTransmittedSsBlocks : " + needednrirfimnbrSSBlock[i][j]);
                                                }
                                                //UPDATE
                                                else {
                                                    logger.info(" ****************************** The needed NRIRFIM with ssbFreq " + neededNRfreq + " exists ! We'll verify if other things are correctly configured.");
                                                    for(nrirfim in nrirfims) {
                                                        if(nrirfim.getParameter("dlCarrierFreq") == neededNRfreq) {

                                                            nrirfimDISTNAME = nrirfim.getParameter("distName").substring(10);

                                                            //Update the parameter ssbScs if it's wrong
                                                            logger.info(" ******************************** The parameter ssbScs is " + nrirfim.getParameter("ssbScs") + " and it should be " + needednrirfimSCS[i][j]);
                                                            if(nrirfim.getParameter("ssbScs") != needednrirfimSCS[i][j]) {
                                                                addRecommendation("UPDATE", nrirfim, "Update the parameter ssbScs to " + needednrirfimSCS[i][j] + " on the NRIRFIM " + nrirfimDISTNAME, "ssbScs", needednrirfimSCS[i][j]);
                                                                logger.info(" ********************************** UPDATE : Update the parameter ssbScs to " + needednrirfimSCS[i][j] + " on the NRIRFIM " + nrirfimDISTNAME);
                                                            }

                                                            //Update the parameter freqBandIndicatorNR if it's wrong
                                                            logger.info(" ******************************** The parameter freqBandIndicatorNR is " + nrirfim.getParameter("freqBandIndicatorNR") + " and it should be " + needednrirfimband[i][j]);
                                                            if(nrirfim.getParameter("freqBandIndicatorNR") != needednrirfimband[i][j]) {
                                                                addRecommendation("UPDATE", nrirfim, "Update the parameter freqBandIndicatorNR to " + needednrirfimband[i][j] + " on the NRIRFIM " + nrirfimDISTNAME, "freqBandIndicatorNR", needednrirfimband[i][j]);
                                                                logger.info(" ********************************** UPDATE : Update the parameter freqBandIndicatorNR to " + needednrirfimband[i][j] + " on the NRIRFIM " + nrirfimDISTNAME);
                                                            }

                                                            //Update the parameter cellReselectionPriority if it's wrong
                                                            logger.info(" ******************************** The parameter cellReselectionPriority is " + nrirfim.getParameter("cellReselectionPriority") + " and it should be " + needednrirfimprio[i][j]);
                                                            if(nrirfim.getParameter("cellReselectionPriority") != needednrirfimprio[i][j]) {
                                                                addRecommendation("UPDATE", nrirfim, "Update the parameter cellReselectionPriority to " + needednrirfimprio[i][j] + " on the NRIRFIM " + nrirfimDISTNAME, "cellReselectionPriority", needednrirfimprio[i][j]);
                                                                logger.info(" ********************************** UPDATE : Update the parameter cellReselectionPriority to " + needednrirfimprio[i][j] + " on the NRIRFIM " + nrirfimDISTNAME);
                                                            }

                                                            //Update the parameter idleMMNrCelResWeight if it's wrong
                                                            logger.info(" ******************************** The parameter idleMMNrCelResWeight is " + nrirfim.getParameter("idleMMNrCelResWeight") + " and it should be " + needednrirfimweight[i][j]);
                                                            if(nrirfim.getParameter("idleMMNrCelResWeight") != needednrirfimweight[i][j]) {
                                                                if(needednrirfimweight[i][j] == null) {
                                                                    addRecommendation("UPDATE", nrirfim, "Update the parameter idleMMNrCelResWeight to empty on the NRIRFIM " + nrirfimDISTNAME, "idleMMNrCelResWeight", "");
                                                                    logger.info(" ********************************** UPDATE : Update the parameter idleMMNrCelResWeight to empty on the NRIRFIM " + nrirfimDISTNAME);
                                                                }
                                                                else {
                                                                    addRecommendation("UPDATE", nrirfim, "Update the parameter idleMMNrCelResWeight to " + needednrirfimweight[i][j] + " on the NRIRFIM " + nrirfimDISTNAME, "idleMMNrCelResWeight", needednrirfimweight[i][j]);
                                                                    logger.info(" ********************************** UPDATE : Update the parameter idleMMNrCelResWeight to " + needednrirfimweight[i][j] + " on the NRIRFIM " + nrirfimDISTNAME);
                                                                }
                                                            }

                                                            //Update the parameter numberOfTransmittedSsBlocks if it's wrong
                                                            logger.info(" ******************************** The parameter numberOfTransmittedSsBlocks is " + nrirfim.getParameter("numberOfTransmittedSsBlocks") + " and it should be " + needednrirfimnbrSSBlock[i][j]);
                                                            if(nrirfim.getParameter("numberOfTransmittedSsBlocks") != needednrirfimnbrSSBlock[i][j]) {
                                                                addRecommendation("UPDATE", nrirfim, "Update the parameter numberOfTransmittedSsBlocks to " + needednrirfimnbrSSBlock[i][j] + " on the NRIRFIM " + nrirfimDISTNAME, "numberOfTransmittedSsBlocks", needednrirfimnbrSSBlock[i][j]);
                                                                logger.info(" ********************************** UPDATE : Update the parameter numberOfTransmittedSsBlocks to " + needednrirfimnbrSSBlock[i][j] + " on the NRIRFIM " + nrirfimDISTNAME);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            //DELETE
                                            //IMPORTANT : Protection to avoid deleting all the NRIRFIM if there is no needed NRIRFIM
                                            if(needednrirfimNrfreq[i].size() != 0) {
                                                for(nrirfim in nrirfims) {
                                                    currentNRfreq = nrirfim.getParameter("dlCarrierFreq");
                                                    nrirfimDISTNAME = nrirfim.getParameter("distName").substring(10);
                                                    if(!needednrirfimNrfreq[i].contains(currentNRfreq)) {
                                                        addRecommendation("DELETE", nrirfim, "Delete " + nrirfimDISTNAME + " with NR ARFCN: " + currentNRfreq + " that is not in the super needednrirfimNrfreq in the subarray #" + i);
                                                        logger.info(" ******************************************** Delete " + nrirfimDISTNAME + " with NR ARFCN: " + currentNRfreq + " that is not in the super needednrirfimNrfreq in the subarray #" + i);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }




                            //NRBTS/NRCELL section
                            //Update the parameter actIdleMMSteering if it's wrong
                            if(cellReferenceTable_cellDepType.contains(SA) || cellReferenceTable_cellDepType.contains(SAplusNSA)) {
                                logger.info(" ************************** The parameter actIdleMMSteering is " + nrbts.getParameter("actIdleMMSteering") + " and it should be " + TRUE);
                                if(nrbts.getParameter("actIdleMMSteering") != TRUE) {
                                    addRecommendation("UPDATE", nrbts, "Update the parameter actIdleMMSteering to " + TRUE + " on the NRBTS : " + nrbtsDistname, "actIdleMMSteering", TRUE);
                                    logger.info(" ***************************** UPDATE : Update the parameter actIdleMMSteering to " + TRUE + " on the NRBTS : " + nrbtsDistname);
                                }
                            }
                            else {
                                logger.info(" ************************** The parameter actIdleMMSteering is " + nrbts.getParameter("actIdleMMSteering") + " and it should be " + FALSE);
                                if(nrbts.getParameter("actIdleMMSteering") != FALSE) {
                                    addRecommendation("UPDATE", nrbts, "Update the parameter actIdleMMSteering to " + FALSE + " on the NRBTS : " + nrbtsDistname, "actIdleMMSteering", FALSE);
                                    logger.info(" ***************************** UPDATE : Update the parameter actIdleMMSteering to " + FALSE + " on the NRBTS : " + nrbtsDistname);
                                }
                            }
                            for(nrcell in nrcells) {
                                nrcellDistname = nrcell.getParameter("distName").substring(10);
                                pos_cellref = cellReferenceTable_lcrId.indexOf(nrcell.getParameter("lcrId"));

                                // ! Update the parameter cellReselectionPriority if it's wrong
                                logger.info(" ************************** The parameter cellReselectionPriority is " + nrcell.getParameter("cellReselectionPriority") + " and it should be " + cellReferenceTable_priority[pos_cellref]);
                                if(nrcell.getParameter("cellReselectionPriority") != cellReferenceTable_priority[pos_cellref]) {
                                    addRecommendation("UPDATE", nrcell, "Update the parameter cellReselectionPriority to " + cellReferenceTable_priority[pos_cellref] + " on the NRCELL " + nrcellDistname, "cellReselectionPriority", cellReferenceTable_priority[pos_cellref]);
                                    logger.info(" ***************************** UPDATE : Update the parameter cellReselectionPriority to " + cellReferenceTable_priority[pos_cellref] + " on the NRCELL " + nrcellDistname);
                                }
                                if((cellReferenceTable_cellDepType[pos_cellref] == SA || cellReferenceTable_cellDepType[pos_cellref] == SAplusNSA)) {
                                    // ! Update the parameter nrSysInfoProfileDN if it's wrong
                                    needednrSysInfoProfileDN = nrbtsDistname+"/NRSYSINFO_PROFILE-"+cellReferenceTable_sysinfoprofile[pos_cellref].toString();
                                    logger.info(" ************************** The parameter nrSysInfoProfileDN is " + nrcell.getParameter("nrSysInfoProfileDN") + " and it should be " + needednrSysInfoProfileDN);
                                    if(nrcell.getParameter("nrSysInfoProfileDN") != needednrSysInfoProfileDN) {
                                        addRecommendation("UPDATE", nrcell, "Update the parameter nrSysInfoProfileDN to " + needednrSysInfoProfileDN + " on the NRCELL : " + nrcellDistname, "nrSysInfoProfileDN", needednrSysInfoProfileDN);
                                        logger.info(" ***************************** UPDATE : Update the parameter nrSysInfoProfileDN to " + needednrSysInfoProfileDN + " on the NRCELL : " + nrcellDistname);
                                    }

                                    // ! Update the parameter idleMMServCelResWeight if it's wrong
                                    logger.info(" ************************** The parameter idleMMServCelResWeight is " + nrcell.getParameter("idleMMServCelResWeight") + " and it should be " + cellReferenceTable_weight[pos_cellref]);
                                    if(nrcell.getParameter("idleMMServCelResWeight") != cellReferenceTable_weight[pos_cellref]) {
                                        if(cellReferenceTable_weight[pos_cellref] == null) {
                                            addRecommendation("UPDATE", nrcell, "Update the parameter idleMMServCelResWeight to empty on the NRCELL " + nrcellDistname, "idleMMServCelResWeight", "");
                                            logger.info(" ***************************** UPDATE : Update the parameter idleMMServCelResWeight to empty on the NRCELL " + nrcellDistname);
                                        }
                                        else {
                                            addRecommendation("UPDATE", nrcell, "Update the parameter idleMMServCelResWeight to " + cellReferenceTable_weight[pos_cellref] + " on the NRCELL " + nrcellDistname, "idleMMServCelResWeight", cellReferenceTable_weight[pos_cellref]);
                                            logger.info(" ***************************** UPDATE : Update the parameter idleMMServCelResWeight to " + cellReferenceTable_weight[pos_cellref] + " on the NRCELL " + nrcellDistname);
                                        }
                                    }

                                    //Update the parameter idleMMSteeringPercentageOfUes if it's wrong
                                    logger.info(" ************************** The parameter idleMMSteeringPercentageOfUes is " + nrcell.getParameter("idleMMSteeringPercentageOfUes") + " and it should be " + NEEDED_IDLEMMSTEERINGPERCENTAGEOFUES);
                                    if(nrcell.getParameter("idleMMSteeringPercentageOfUes") != NEEDED_IDLEMMSTEERINGPERCENTAGEOFUES) {
                                        addRecommendation("UPDATE", nrcell, "Update the parameter idleMMSteeringPercentageOfUes to " + NEEDED_IDLEMMSTEERINGPERCENTAGEOFUES + " on the NRCELL : " + nrcellDistname, "idleMMSteeringPercentageOfUes", NEEDED_IDLEMMSTEERINGPERCENTAGEOFUES);
                                        logger.info(" ***************************** UPDATE : Update the parameter idleMMSteeringPercentageOfUes to " + NEEDED_IDLEMMSTEERINGPERCENTAGEOFUES + " on the NRCELL : " + nrcellDistname);
                                    }
                                    //Update the parameter idleMMSteeringWRREnabled if it's wrong
                                    logger.info(" ************************** The parameter idleMMSteeringWRREnabled is " + nrcell.getParameter("idleMMSteeringWRREnabled") + " and it should be " + TRUE);
                                    if(nrcell.getParameter("idleMMSteeringWRREnabled") != TRUE) {
                                        addRecommendation("UPDATE", nrcell, "Update the parameter idleMMSteeringWRREnabled to " + TRUE + " on the NRCELL : " + nrcellDistname, "idleMMSteeringWRREnabled", TRUE);
                                        logger.info(" ***************************** UPDATE : Update the parameter idleMMSteeringWRREnabled to " + TRUE + " on the NRCELL : " + nrcellDistname);
                                    }
                                }
                            }
                        }
                        else {
                            logger.info("ERROR : On gNB " + nrbtsDistname + ", these bands are unknown, we'll stop the CPR : " + unknown_band);
                            global_unknownband_gNB.add(nrbtsDistname);
                            global_unknownband_band.add(unknown_band);
                        }
                    }
                    else {
                        logger.info("WARNING : One or more NRCELL had an unknown SSB FREQ, we'll skip the NR analysis.");
                    }
                }
                else {
                    logger.info(" ************* WARNING : gNB is NSA only, so we don't manage IMLB on that older configuration");
                }
            }
            else {
                logger.info(" ************* WARNING : gNB software is older than 22R1, so we don't manage IMLB on that older software");
            }
        }
        else {
            logger.info(" ************* ERROR : On gNB " + nrbtsDistname + ", nrcells size is 0");
        }
    }
}

logger.info(" **** Number of gNB with unknown band : " + global_unknownband_gNB.size());
for(def i = 0; i < global_unknownband_gNB.size(); i++) {
    logger.info(" ******* gNB with unknown band : " + global_unknownband_gNB[i] + " and the unknown_band are : " + global_unknownband_band[i]);
}
logger.info(" **** Number of nrcell with wrong SSB_FREQ: " + cellwithunknownSSBFreq.size());
for(def i = 0; i < cellwithunknownSSBFreq.size(); i++) {
    logger.info(" ******* nrcell with wrong SSB_FREQ : " + cellwithunknownSSBFreq[i]);
}

logger.info(" ************** Total number of evaluated objects: NRBTS " + number_of_nrbts);
logger.info(" ************** End of " + cpr_name);