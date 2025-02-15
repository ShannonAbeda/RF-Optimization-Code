/*V1 (ORGINAL): Developed by Uzair Khan Ali
Date created: 2023

This feature introduces the dynamic beamforming schemes for
downlink, which means dynamic switching of SRS based
beamforming and codebook/SSB based beamforming.
 For the cell center UE, SRS based beamforming will be adopted by
using covariance matrix calculation.
 In case SRS covariance matrix cannot be available due to low SINR,
UE feedback based beam can be applied such as SSB
(5GC001856/5GC000605) or 8x4 codebook (5GC002200) based
beam.
 If SRS resource is exhausted, for other newly added UE, codebook
based BF will be configured with cell specific CSI-RS or UE specific
CSI-RS.
 UE will be configured with SRS antenna switching based on UE
capability.
 Codebook SRS will be used in case UE does not support SRS Tx
antenna switching.
 CB008247 reuses 5GC001086 SRS UL based beamforming feature
framework, however CB008247 and 5GC001086 can be activated
independently.


The feature is applied to TDD FR1 deployments and to 32 TRX (including 32TRx split mode) and 64 TRX RUs
(8 TRX RUs are out of feature scope).
 Supported RU types are same as 5GC001086 SRS based beamforming in UL (WebNEI)
? nrCellType=8DL4UL required for 64TRX RUs
? nrCellType=4DL4UL and 8DL4UL possible for 32TRX RUs
 CB008247 applies to eCPRI based RUs supporting beam weight download via M-Plane (introduced by
5GC001866).
 The feature is only based on NDM SRS*. For RUs not supporting NDM SRS, this feature shall be disabled, legacy
beam management scheme is applied then.
 Both SA/NSA architecture options are supported
 UE Rel. 15 are supported, although Transmit Antenna Switching (TAS) SRS is UE optional feature
 The feature is E2E feature, it also includes impact on RUs.



 The ABIO/ASOE platforms are supported. ABIL is not in the scope.
 ABIN support is covered in later release by CB007134 ABIN NR legacy features support TDD eCPRI
 The feature does not support vRAN2.0 (a separate feature CB008608 is planned to cover vRAN for CB008247).
 CB008247 can be activated and deactivated by new introduced O&M NIDD parameter.
 There is no dependency between DL and UL beam management. CB008247 and 5GC001086 can be activated
independently.
 CB008247 supports an interworking with:
? cDRX (gNB should consider the SRS report can be only achieved by gNB in DRX on mode).
? CA (the SRS based DL beam will not be used for Scell).
? EIRP and power saving (multiple BWPs).
 In addition, feature introduces CSI_RS multiplexing (activated by NRCELL. actCsirsMultiplexing parameter)

PARAMETER NAME              MO                      DESCRIPTION                                                                                                                                                                                                                                         Default Value   Recommended Value
actBeamControlImprovement1	NRBTS	                Activate beam control improvement 1 	         Activation flag for the TCI state indication while beam toggling. Optional feature(s): 5GC000533 Digital Beamforming for CPRI based RUs		                                                    0 - False  	        1 -True
actBeamControlImprovement2	NRBTS	                Activate beam control improvement 2	             Activation flag for the reduction of the UL beam re-probing rate, especially for the 4Rx receivers. Optional feature(s): 5GC000533 Digital Beamforming for CPRI based RUs	NA	                    0 - False	        1 -True
actUlSrsBm	                NRCELL	                Activate uplink SRS based beam management	        This parameter activates/deactivates uplink SRS based beam management.		                                                                                                                    0 - False	        1 -True
actDlSrsBm	                NRCELL	                Activate downlink SRS based beam management	        This parameter activates/deactivates downlink SRS based beam management.		                                                                                                                0 - False	        1- True
actCsirsBeamPowerBalance	NRCELL	                 Activate CSI-RS beam power balance	Activation flag for UE specific CSI-RS beam power balance. Optional feature(s): CB010602 SRS based Beamforming extension		                                                                                0 - False	        1- True
actCsirsMultiplexing	    NRCELL	                Activate multiple CSI-RS resources multiplexing in slot	        This parameter activates/deactivates multiple CSI-RS resources multiplexingin slot for both UE and cell specific CSI-RS.		                                                    0 - False	        1- True
actPdschBeamPowerBalance	NRCELL	                Activate PDSCH beam power balance	Activation flag for PDSCH beam power balance. Optional feature(s): CB010602 SRS based Beamforming extension	NA	                                                                                                0 - False	        1- True
actUeCsirsCoexistCellCsirs	NRCELL	                Activate UE and cell specific CSI-RS coexist	Activation flag for UE specific CSI-RS coexisted with cell specific CSI-RS in case NRCELL.actDlSrsBm is equal to TRUE and NRCELL.dlMimoMode is equalto '8x4 Closed Loop Spatial Multiplexing.		0 - False	        1- True
lowerEdgeAngle          	NRCELL.beamSet	        Lower cell edge angle	This parameter defines the angle limiting cell on lower side with respect to bore sight. The parameter is applicable and used only in analog beamforming.	                                                            	135	                   135
upperEdgeAngle	            NRCELL.beamSet	        Upper Cell Edge Angle	This parameter indicates the massive MIMO (mMIMO) antenna array mode on 64 pipes mMIMO MAA.	                                                                                                                            	80	                    80
srsUePeriodicity	        NRCELL.srsForBeamMgmt	SRS periodicity of UE transmission	UE periodicity for periodic SRS used by the UE to send SRS to the gNB for both codebook SRS and antenna switching SRS.	5-10-20-40-80-160-320-640-1280-2560	40 - slots                                          40	                    40
srsTypeAllocForDlBm	        NRCELL.srsForBeamMgmt	SRS type allocation for DL SRS-based beamforming	Type of SRS used for SRS based DL beamforming.	0-2-3	                                                                                                                                     2 - antenna switching or codebook	2
srsPowerThresholdForDlBm 	NRCELL.srsForBeamMgmt	SRS power threshold for DL SRS based beamforming	Power threshold of SRS covariance matrix DTX for DL SRS based beamforming	                                                                                                                    -1200	               -1200
cSRS	                    NRCELL.srsForBeamMgmt	SRS bandwidth configuration	This parameter indicates the SRS bandwidth configuration (corresponding to CSRS in table 6.4.1.4.3-1 of 3GPP TS38.211).	0-63	                                                                                            0	                12 -> ChBW <= 40
                                                                                                                                                                                                                                                                                                                                32 -> ChBW = 50
                                                                                                                                                                                                                                                                                                                                38 -> ChBW = 60 & 70
                                                                                                                                                                                                                                                                                                                                49 -> ChBW = 80
                                                                                                                                                                                                                                                                                                                                55 -> ChBW = 90
                                                                                                                                                                                                                                                                                                                                63 -> ChBW = 100

bSRS	                    NRCELL.srsForBeamMgmt	SRS subband configuration	This parameter indicates the SRS subband configuration (corresponding to BSRS in table 6.4.1.4.3-1 of 3GPP TS38.211).		                                                                                               0	                2 -> ChBW if not = 90
                                                                                                                                                                                                                                                                                                                                3 -> ChBW  = 90 
beamPowerOffsetThreshold	NRCELL	Threshold of beam power offset 	Threshold of the beam power offset. Optional feature(s): CB010602 SRS based Beamforming extension		                                                                                                                               6	                6


NRCELL.actDlSrsBm can be equal to TRUE only if all of the following conditions are fulfilled:
- NRCELL.actBeamforming is equal to TRUE
- NRCELL.cellTechnology is equal to 'TDD'
- NRCELL.nrarfcn <=800000 (FR1)




V2: Developed by Shannon Abeda
March 20, 2024
Changes made:
    -Added conditions to prevent changes to BeamSet Parameter in the Airport Exclusion Zone sites
        if preventParamModifTargetDN contains distname and has ""NRCELL-beamSet"", it will not update
    -Changes to acuUlSrsBm to prevent the following netact failures:
        Whenever
         NRCELL-41 having NRCELL-41.actUlSrsBm (value: true) equal to true, it is required
        that NRCELL-41.actBeamforming (value: false) equal to true and NRCELL-41.nrarfcn
        (value: 633334) less/equal to 800000"
     -Fixed srsBeamMgt Parameter that was missing a key parameter: srsPowerThresholdForDlBm and its value of -120
     -Formatted Indentation and added information about feature 

    -Fixed srsBeamMgt Missing Parameter:
        NRCELL.srsForBeamMgmt.comb can be equal to 'comb4' only if:
        NRCELL.actBeamforming is equal to TRUE
        NRCELL.cellTechnology is equal to 'TDD'
        NRCELL.nrarfcn <= 800000 (FR1)
        NRCELL.actDlSrsBm or NRCELL.actUlSrsBm is equal to TRUE.
        NRCELL.srsPowerThresholdForUlBm
        NRCELL.dftBeamDiffThresholdV
        NRCELL.dftBeamDiffThresholdH
    -Removed actDlSrsBm as requested by Gebran & Francis
*/





cpr_name = "N_5G_SRS_Downlink_Beamforming_Update";
logger.info(" **** Beginning of " + cpr_name);
OK_TO_START = true
def number_of_nrbts = 0;


diff_n78_F1 = []
diff_n78_F2 = []

if(OK_TO_START) {
	for(nrbts in _context.getMos("NRBTS")) {
		def mrbts = nrbts.getMos("MRBTS");
        def mnl = mrbts.getMos("MNL");
        def mnlent = mnl.getMos("MNLENT");
		def FeatCadm = mnlent.getMos("FEATCADM")
        def gNodeBID = mrbts.getParameterAsInteger("originalId");

        logger.info(" INFO: the gNodeBID is: ${gNodeBID}");
        def apeqm_r = nrbts.getMos("APEQM_R");
        def cabinet_r =apeqm_r.getMos("CABINET_R");
        def bbmod_r =cabinet_r.getMos("BBMOD_R");
		flag_condition = false;
        number_of_nrbts++;
     
		
		UNKNOWN_SSBFREQ = false;
		nrcells = nrbts.getMos("NRCELL");
		nrbtsDistname = nrbts.getParameter("distName").substring(10);	
		Integer versionInt = 0;	
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


        if(versionInt >= 23) {
            for(nrcell in nrcells) {
                
                flag_condition_cell = false;
                // BBU BBMOD_R
                def productName = bbmod_r.getParameter('productName')
                def distName = nrcell.getParameter("distName");


                logger.info(" INFO: The cell is ${distName} and the gNodeBID is: ${gNodeBID}");

             
            
                def freqBandIndicatorNR  = nrcell.getParameter("freqBandIndicatorNR")
                def cellTechnology = nrcell.getParameter("cellTechnology");
                def nrCellType = nrcell.getParameter("nrCellType");
                def pucchModeSelect = nrcell.getParameter("pucchModeSelect");
                def mMimoAntArrayMode = nrcell.getParameter("mMimoAntArrayMode");
                def dlMimoMode = nrcell.getParameter("dlMimoMode");
                
                if(nrcell.getParameterAsInteger("cellTechnology") == "0") {
                    nrarfcn_integer = nrcell.getMos("NRCELL_FDD").getParameter("nrarfcnDl");
                    bw_integer = nrcell.getMos("NRCELL_FDD").getParameterAsInteger("chBwDl");
                }
                else{
                    nrarfcn_integer = nrcell.getParameter("nrarfcn");
                    bw_integer = nrcell.getParameterAsInteger("chBw");
                }
                
                /*logger.info(" ************* Cell : " + nrbts.getParameter("originalId")+"_"+nrcell.getParameter("lcrId"));
                logger.info(" ************* apeqm_r : " + apeqm_r);
                logger.info(" ************* antmods_r : " + antmods_r);
                logger.info(" ************* rmods_r : " + rmods_r);
                logger.info(" ************* productName : " + productName);
                logger.info(" ************* freqBandIndicatorNR : " + freqBandIndicatorNR);	
                logger.info(" ************* bw_integer : " + bw_integer);	
                logger.info(" ************* cellTechnology : " + cellTechnology);	
                logger.info(" ************* nrCellType : " + nrCellType);
                logger.info(" ************* pucchModeSelect : " + pucchModeSelect);
                logger.info(" ************* mMimoAntArrayMode : " + mMimoAntArrayMode);	
                ogger.info(" ************* dlMimoMode : " + dlMimoMode); */
    
                def beamSetConditionFlag = false;
                def current_preventParamModifTargetDN   = FeatCadm.getListEnumerationParameter("preventParamModifTargetDN");
                def current_preventParamModifList = FeatCadm.getListEnumerationParameter("preventParamModifList");
    
                def exclusion_Parameter = "NRCELL-beamSet";
                      
                /*logger.info("**********************************************************************************************************************************************************************************************");
                logger.info("The exception parameter is: " + exclusion_Parameter);
                logger.info("The current_preventParamModifTargetDN is: " + current_preventParamModifTargetDN);
                */logger.info("The current_preventParamModifList is: " + current_preventParamModifList); 
                      
                //This is not working and skipping sites that do not have values
                if(current_preventParamModifTargetDN.size() != 0 && current_preventParamModifList.size() != 0) {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> INFO: The current_preventParamModifTargetDN and current_preventParamModifList on this cell exist and we will check if they contain the restrictions for beamset");
    
                    for(def i=0; i<current_preventParamModifTargetDN.size(); i++) {
                        if(current_preventParamModifTargetDN[i] == distName) {
                            //logger.info("**********************************************************************************************************************************************************************************************");
                            //logger.info("---> INFO: The distname is include on the preventParamModifTargetDn List and we will now check if the current_preventParaModifList contains the exclusion parameter: BeamSet");
                            for(def j=0; j<current_preventParamModifList.size(); j++) {
                                if(current_preventParamModifList[j] == exclusion_Parameter) {
                                    //logger.info("**********************************************************************************************************************************************************************************************");
                                    //logger.info("---> INFO: The current_preventParamModifList has the parameter: " + exclusion_Parameter + " and the BeamSetconditionFlag will be set to: " + false);
                                    beamSetConditionFlag = false;
                                }
                            }
                        }
                    }
                }
    
                else {
                    //logger.info("**********************************************************************************************************************************************************************************************");
                    //logger.info("---> This cell is not in the airport protection zone and therefore beamset is not restricted on this cell");
                    beamSetConditionFlag = true;
                }
                 
                if ( 
                    
                    ( productName == "ABIO AirScale Capacity" ) &&
                    ( cellTechnology == "1" ) &&
                    ( (nrCellType == "44") || (nrCellType == "84") ) &&
                    ( pucchModeSelect == "1" ) &&
                    ( (mMimoAntArrayMode == "0") || (mMimoAntArrayMode == "3") || (mMimoAntArrayMode == "4") || (mMimoAntArrayMode == "7") ) &&
                    ( dlMimoMode == "40")  && ((nrcell.getParameterAsInteger("actBeamforming") == 1 ) && nrcell.getParameterAsInteger("nrarfcn") <= 800000)
                )	
                    {
                        //logger.info("Condition True")	
                        flag_condition = true;
                        flag_condition_cell = true
                        currentActUlSrsBm = nrcell.getParameterAsInteger("actUlSrsBm");
                        currentactDlSrsBm = nrcell.getParameterAsInteger("actDlSrsBm");
                        neededActUlSrsBm = 1;
                        neededactDlSrsBm = 1;
                        def actUlSrsBmConditionFlag = false;
                        def actDlSrsBmConditionFlag = false;
    
                        existing_beamSet = nrcell.getListParameter('beamSet')
                        //logger.info( " ****** length of  beamSet " +  existing_beamSet.size())
                        existing_beamSet_lowerEdgeAngle = existing_beamSet[0]["lowerEdgeAngle"];
                        existing_beamSet_upperEdgeAngle = existing_beamSet[0]["upperEdgeAngle"];
         
                        if (flag_condition_cell){
                            if(currentActUlSrsBm != neededActUlSrsBm ){
                                //logger.info("INFO-> ******************** The current actUlSrsBm is: " + currentActUlSrsBm + " And the needed value is : " + neededActUlSrsBm);
                                addRecommendation("UPDATE", nrcell, "UPDATE : Update the parameter actUlSrsBm", "actUlSrsBm", neededActUlSrsBm);
                                actUlSrsBmConditionFlag = true;

                                if(versionString >= "24R2") {
                                    if(actUlSrsBmConditionFlag) {
                                        currentdftBeamDiffThresholdH = nrcell.getParameterAsInteger("dftBeamDiffThresholdH");
                                        currentdftBeamDiffThresholdV = nrcell.getParameterAsInteger("dftBeamDiffThresholdV");
        
                                        if(currentdftBeamDiffThresholdH != 15) {
                                            addRecommendation("UPDATE", nrcell, "UPDATE: Activating dftBeamDiffThresholdH", "dftBeamDiffThresholdH", 15)
                                        }
        
                                        if(currentdftBeamDiffThresholdV != 15) {
                                            addRecommendation("UPDATE", nrcell, "UPDATE: Activating dftBeamDiffThresholdV", "dftBeamDiffThresholdV", 15)
                                        }
                                    }
                                }
                            
                                if(beamSetConditionFlag) {
                                    if ( existing_beamSet.size() != null ){
                                        if ( (existing_beamSet_lowerEdgeAngle != 135) || (existing_beamSet_upperEdgeAngle != 80) ){
                                            //logger.info("**********************************************************************************************************************************************************************************************");
                                            //logger.info("--->INFO: DEBUG#1");
                                    
                                            if (nrcell.getParameter("lowerEdgeAngle") != "135" ){
                                                //logger.info("**********************************************************************************************************************************************************************************************");
                                                //logger.info("--->INFO: DEBUG#2");
                                                existing_beamSet[0]["lowerEdgeAngle"] = "135"
                                            }
                                            //logger.info("**********************************************************************************************************************************************************************************************");
                                            //logger.info("--->INFO: DEBUG#3");
                                        
                                            if (nrcell.getParameter("upperEdgeAngle") != "80" ){
                                            //logger.info("**********************************************************************************************************************************************************************************************");
                                            //logger.info("--->INFO: DEBUG#4");
                                            existing_beamSet[0]["upperEdgeAngle"] = "80"
                                            }
                                        addRecommendation("UPDATE", nrcell, "UPDATE : Update the beamSet list  to " + existing_beamSet, "beamSet", existing_beamSet);
                                        }
                                    }
                                
                                    else {
                                        //logger.info("**********************************************************************************************************************************************************************************************");
                                        //logger.info("--->INFO: DEBUG#4")
                                        neededsrsForbeamSet = createList()
                                        neededsrsForbeamSet.addEntry(["lowerEdgeAngle" : "135"])
                                        neededsrsForbeamSet.addEntry(["upperEdgeAngle" : "80"])
                                        addRecommendation("UPDATE", nrcell, "UPDATE : Update the beamSet list  to " + neededsrsForbeamSet, "beamSet", neededsrsForbeamSet);
                                    }
                                }
                                //logger.info("**********************************************************************************************************************************************************************************************");
                                //logger.info("--->INFO: DEBUG#5");
                                existing_srsForBeamMgmt = nrcell.getListParameter('srsForBeamMgmt')
                                //logger.info( " ****** length of  srsForBeamMgmt " +  existing_srsForBeamMgmt.size())
    
                                if (existing_srsForBeamMgmt.size() != 0){
                                    //logger.info("**********************************************************************************************************************************************************************************************");
                                    //logger.info("--->INFO: DEBUG#6");
    
                                    existing_srsForBeamMgmt_srsUePeriodicity = existing_srsForBeamMgmt[0]["srsUePeriodicity"]
                                    existing_srsForBeamMgmt_srsTypeAllocForDlBm = existing_srsForBeamMgmt[0]["srsTypeAllocForDlBm"]
                                    existing_srsForBeamMgmt_cSRS = existing_srsForBeamMgmt[0]["cSRS"]
                                    existing_srsForBeamMgmt_bSRS = existing_srsForBeamMgmt[0]["bSRS"]
                                    existing_srsForBeamMgmt_srsPowerThresholdForDlBm = existing_srsForBeamMgmt[0]["srsPowerThresholdForDlBm"];
                                    existing_srsForBeamMgmt_comb = existing_beamSet[0]["comb"];
                                    existing_srsForBeamMgmt_srsPowerThresholdForUlBm = existing_srsForBeamMgmt[0]["srsPowerThresholdForUlBm"];
    
                                    //logger.info("--->INFO: DEBUG#6.1");
    
                                   
                                    
                                    //logger.info("**********************************************************************************************************************************************************************************************");
                                    //logger.info("--->INFO: DEBUG#7")
                                    // srsUePeriodicity 
                                    if ( existing_srsForBeamMgmt_srsUePeriodicity != "40" ){
                                        
                                        existing_srsForBeamMgmt[0]["srsUePeriodicity"] = "40";
                                    }
    
                                    //logger.info("**********************************************************************************************************************************************************************************************");
                                    //logger.info("--->INFO: DEBUG#8")
                                    
                                    if(existing_srsForBeamMgmt_srsPowerThresholdForUlBm != "-1320") {
                                        existing_srsForBeamMgmt[0]["srsPowerThresholdForUlBm"] = "1320";
                                    }
                                    
                                    //logger.info("**********************************************************************************************************************************************************************************************");
                                    //logger.info("--->INFO: DEBUG#9")
                                    // srsTypeAllocForDlBm
                                    if ( existing_srsForBeamMgmt_srsTypeAllocForDlBm != "2" ){
                                        
                                        existing_srsForBeamMgmt[0]["srsTypeAllocForDlBm"] = "2"						
                                    }
                                    
                                    // cSRS
                                    //logger.info("**********************************************************************************************************************************************************************************************");
                                    //logger.info("--->INFO: DEBUG#10");
                                    if ( bw_integer <=40 ){
                                        
                                        if ( existing_srsForBeamMgmt_cSRS != "12" ){
                                            
                                            existing_srsForBeamMgmt[0]["cSRS"] = "12"	
                                        }
                                    }
                                    
                                    else if ( bw_integer == 50 ){
                                        
                                        if ( existing_srsForBeamMgmt_cSRS != "32" ){
                                            
                                            existing_srsForBeamMgmt[0]["cSRS"] = "32"
                                        }
                                        
                                        
                                    }
                                    
                                    else if ( bw_integer == 60 || bw_integer == 70 ){
                                        
                                        if ( existing_srsForBeamMgmt_cSRS != "38" ){
                                            
                                            existing_srsForBeamMgmt[0]["cSRS"] = "38"
                                        }
                                        
                                        
                                    }
                                    
                                    else if ( bw_integer == 80 ){
                                        
                                        if ( existing_srsForBeamMgmt_cSRS != "49" ){
                                            
                                            existing_srsForBeamMgmt[0]["cSRS"] = "49"
                                        }
                                        
                                        
                                    }
                                    
                                    else if ( bw_integer == 90 ){
                                        
                                        if ( existing_srsForBeamMgmt_cSRS != "55" ){
                                            
                                            existing_srsForBeamMgmt[0]["cSRS"] = "55"
                                        }
                                        
                                        
                                    }
                                    
                                    else if ( bw_integer == 100 ){
                                        
                                        if ( existing_srsForBeamMgmt_cSRS != "63" ){
                                            
                                            existing_srsForBeamMgmt[0]["cSRS"] = "63"
                                        }
                                        
                                        
                                    }
                                    
                                    // bSRS
                                    if ( bw_integer == 90 ){
                                        
                                        if ( existing_srsForBeamMgmt_bSRS != "3" ){
                                            
                                            existing_srsForBeamMgmt[0]["bSRS"] = "3"
                                        }
                                        
                                        
                                    }
                                    
                                    else{
                                        
                                        if ( existing_srsForBeamMgmt_bSRS != "2" ){
                                            
                                            existing_srsForBeamMgmt[0]["bSRS"] = "2"
                                        }
        
                                        if (existing_srsForBeamMgmt_srsPowerThresholdForDlBm != "-1200") {
                                            existing_srsForBeamMgmt[0]["srsPowerThresholdForDlBm"] = "-1200"
                                        }
                                        
                                    }
                                    
        
                                    if(existing_srsForBeamMgmt_comb != "4") {
                                        existing_srsForBeamMgmt[0]["comb"] = "4"
                                    }
        
        
                                    
                                    addRecommendation("UPDATE", nrcell, "UPDATE : Update the srsForBeamMgmt list  to " + existing_beamSet, "srsForBeamMgmt", existing_srsForBeamMgmt);
                                }
                                else {
                                    
                                    def cSRS_value = '';
                                    def bSRS_value = '';
                                    if ( bw_integer <=40 ){
                                        
                                        cSRS_value = "12"	
                                    }
                                    
                                    else if ( bw_integer == 50 ){
                                        
                                        cSRS_value = "32"													
                                    }
                                    
                                    else if ( bw_integer == 60 || bw_integer == 70 ){
                                        
                                        cSRS_value = "38"
                                        
                                    }
                                    
                                    else if ( bw_integer == 80 ){
                                        
                                        cSRS_value = "49"							
                                        
                                    }
                                    
                                    else if ( bw_integer == 90 ){
                                        
                                        cSRS_value = "55"						
                                        
                                    }
                                    
                                    else if ( bw_integer == 100 ){
                                        
                                        cSRS_value = "63"
                                        
                                    }
                                    
                                    // bSRS
                                    if ( bw_integer == 90 ){
                                        
                                        bSRS_value = "3"
                                        
                                    }
                                    
                                    else{
                                        
                                        bSRS_value = "2"
                                    }
                                    
                                    neededsrsForBeamMgmt = createList()
                                    neededsrsForBeamMgmt.addEntry([
                                    "srsUePeriodicity" : "40",
                                    "srsTypeAllocForDlBm" : "2",
                                    "cSRS" : cSRS_value,
                                    "bSRS" : bSRS_value,
                                    "srsPowerThresholdForDlBm" : "-1200",
                                    "srsPowerThresholdForUlBm": "-1320",
                                    "comb" : "2"
                                    ])
                                    
                                    addRecommendation("UPDATE", nrcell, "UPDATE : Update the srsForBeamMgmt list  to " + neededsrsForBeamMgmt, "srsForBeamMgmt", neededsrsForBeamMgmt);
                                }
                            }
                        }       
                    }
                }
            
                if (flag_condition){
                
                // actBeamControlImprovement1
                if ( nrbts.getParameter("actBeamControlImprovement1") != "1" ){
                                
                    addRecommendation("UPDATE", nrbts, "UPDATE : Update the parameter actBeamControlImprovement1", "actBeamControlImprovement1", 1);
                }
                
                // actBeamControlImprovement2
                if ( nrbts.getParameter("actBeamControlImprovement2") != "1" ){
                                
                    addRecommendation("UPDATE", nrbts, "UPDATE : Update the parameter actBeamControlImprovement2", "actBeamControlImprovement2", 1);
                }
            }						
        }
    }
}
logger.info(" ************** Total number of evaluated objects: NRBTS " + number_of_nrbts);
logger.info(" ************** End of " + cpr_name);