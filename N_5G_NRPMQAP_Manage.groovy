/***************************************************************************************************************************************
 * * CPR: N_5G_NRPMQAP_Manage,
 * To obtain per-PLMN volume and throughput calculations for SA and NSA traffic, Nokia recommends using NRPMQAP (Network Radio Performance Measurement Quality Assurance Profiles). 
 NRPMQAP profiles enable the collection and analysis of detailed performance metrics for specific PLMNs.
 *
 * * Version 1:  Younes -  June 24th 2024
 * ?    Update the MO as per the recommendation in the NRDRB QoS Settings Tab in the 5G tracker
 * ?    This version is for NRBTS.actThpDist = False, in case this parameter becomes true in the Network, NRPMQAP.thpHistScale and NRBTS.thpHistScale will need to be configured
 *
 ***************************************************************************************************************************************/


//Static table as per request for CURRENT NRPMQAP needed
def recommended_NRMQAP = [:]                   // ?      1           3           4           5           6           8           9           12          13          14          15          16          18          19
recommended_NRMQAP["originalId"]                 = [     1,          3,          4,          5,          6,          8,          9,          12,         13,         14,         15,         16,         18,         19,     ]
recommended_NRMQAP["cfg5qiRange"]                = [     8,          8,          9,          5,          1,          null,       null,       8,          8,          9,          5,          1,          null,       null,   ]
recommended_NRMQAP["cfgArpRange"]                = [     null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,   ]
recommended_NRMQAP["thpHistDownlinkMaxRange"]    = [     null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,   ]
recommended_NRMQAP["thpHistDownlinkMinRange"]    = [     null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,   ]
recommended_NRMQAP["cfgPlmnId"]      =   [
                                "mcc"           : [     "302",      "302",      "302",      "302",      "302",      "302",      "302",      "302",      "302",      "302",      "302",      "302",      "302",      "302",  ],
                                "mnc"           : [     "610",      "610",      "610",      "610",      "610",      "610",      "610",      "220",      "220",      "220",      "220",      "220",      "220",      "220",  ],
                                "mncLength"     : [     "3",        "3",        "3",        "3",        "3",        "3",        "3",        "3",        "3",        "3",        "3",        "3",        "3",        "3",    ],]

recommended_NRMQAP["cfgProfType"]                = [     "1",       "1",        "1",        "1",        "1",        "0",        "0",        "1",        "1",        "1",        "1",        "1",        "0",        "0",      ]
recommended_NRMQAP["cfgQciRange"]                = [     null,       null,       null,       null,       null,       8,          9,          null,       null,       null,       null,       null,       8,          9,      ]
recommended_NRMQAP["cfgSliceId"]     =   [
                                "exists"        : [     true,       false,      false,      false,      false,      false,      false,      true,       false,      false,      false,      false,      false,      false,  ],
                                "sd"            : [     "10940",    null,       null,       null,       null,       null,       null,       "2",        null,       null,       null,       null,       null,       null,   ],
                                "sst"           : [     "1",        null,       null,       null,       null,       null,       null,       "1",        null,       null,       null,       null,       null,       null,   ],]


recommended_NRMQAP["cfgSpidRange"]               = [     null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,   ]
recommended_NRMQAP["thpHistScale"]               = [     null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,   ]
recommended_NRMQAP["thpHistUplinkMaxRange"]      = [     null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,   ]
recommended_NRMQAP["thpHistUplinkMinRange"]      = [     null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,       null,   ]


def cpr_name = "N_5G_NRPMQAP_Manage";
logger.info("Beginning - of CPR: "+ cpr_name );


for (NRBTS in _context.getMos("NRBTS")){
    def list_of_existing_NRPMQAP = []

    // logger.info(" INFO: Evaluating ${NRBTS.getParameter("distName")}")

    def NRPMRNL = NRBTS.getMos("NRPMRNL")
    for (NRPMQAP in NRBTS.getMos("NRPMQAP") ){

        def NRPMQAP_distName    = NRPMQAP.getParameter("distName")
        // logger.info("       || INFO: Evaluating ${NRPMQAP_distName}")


        def NRPMQAP_originalId    = NRPMQAP.getParameterAsInteger("originalId")
        list_of_existing_NRPMQAP.add(NRPMQAP_originalId)

        if (!recommended_NRMQAP["originalId"].contains(NRPMQAP_originalId)){
            addRecommendation("DELETE", NRPMQAP, "Deleting ${NRPMQAP_distName} because it is not needed")
        } else {
            int index = 0
            // logger.info(" INFO: ${recommended_NRMQAP["originalId"]}")
            for( index = 0; index < recommended_NRMQAP["originalId"].size(); index ++){
                if(recommended_NRMQAP["originalId"][index] == NRPMQAP_originalId ){
                    // logger.info(" INFO: ${index} - ${recommended_NRMQAP["originalId"][index]}")
                    break
                }
            }


            // ! UPDATE originalId
            def NRPMQAQP_cfg5qiRange = NRPMQAP.getParameterAsInteger("cfg5qiRange")
            // logger.info(" INFO: NRPMQAQP_cfg5qiRange = ${NRPMQAQP_cfg5qiRange}  - recommended is: ${recommended_NRMQAP["cfg5qiRange"][index]}")
            if(NRPMQAQP_cfg5qiRange != recommended_NRMQAP["cfg5qiRange"][index]){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "cfg5qiRange", recommended_NRMQAP["cfg5qiRange"][index] ?: "")
            }

            // ! UPDATE cfgArpRange
            def NRPMQAQP_cfgArpRange = NRPMQAP.getParameterAsInteger("cfgArpRange")
            // logger.info(" INFO: NRPMQAQP_cfgArpRange = ${NRPMQAQP_cfgArpRange}  - recommended is: ${recommended_NRMQAP["cfgArpRange"][index]}")
            if(NRPMQAQP_cfgArpRange != recommended_NRMQAP["cfgArpRange"][index]){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "cfgArpRange", recommended_NRMQAP["cfgArpRange"][index] ?: "")
            }

            // ! UPDATE thpHistDownlinkMaxRange
            def NRPMQAQP_thpHistDownlinkMaxRange = NRPMQAP.getParameterAsInteger("thpHistDownlinkMaxRange")
            // logger.info(" INFO: NRPMQAQP_thpHistDownlinkMaxRange = ${NRPMQAQP_thpHistDownlinkMaxRange}  - recommended is: ${recommended_NRMQAP["thpHistDownlinkMaxRange"][index]}")
            if(NRPMQAQP_thpHistDownlinkMaxRange != recommended_NRMQAP["thpHistDownlinkMaxRange"][index]){
                    addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "thpHistDownlinkMaxRange", recommended_NRMQAP["thpHistDownlinkMaxRange"][index])
            }

            // ! UPDATE thpHistDownlinkMinRange
            def NRPMQAQP_thpHistDownlinkMinRange = NRPMQAP.getParameterAsInteger("thpHistDownlinkMinRange")
            // logger.info(" INFO: NRPMQAQP_thpHistDownlinkMinRange = ${NRPMQAQP_thpHistDownlinkMinRange}  - recommended is: ${recommended_NRMQAP["thpHistDownlinkMinRange"][index]}")
            if(NRPMQAQP_thpHistDownlinkMinRange != recommended_NRMQAP["thpHistDownlinkMinRange"][index]){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "thpHistDownlinkMinRange", recommended_NRMQAP["thpHistDownlinkMinRange"][index] ?: "")
            }

            // ! UPDATE cfgPlmnId
            def NRPMQAQP_cfgPlmnId = NRPMQAP.getListParameter("cfgPlmnId")
            // logger.info(" INFO: NRPMQAQP_cfgPlmnId = ${NRPMQAQP_cfgPlmnId} ")
            def needed_cfgPlmnId = createList()
            needed_cfgPlmnId.addEntry([
                        "mcc"           : recommended_NRMQAP["cfgPlmnId"]["mcc"][index],
                        "mnc"           : recommended_NRMQAP["cfgPlmnId"]["mnc"][index],
                        "mncLength"     : recommended_NRMQAP["cfgPlmnId"]["mncLength"][index],  ])
            // logger.info(" INFO:         NEEDED is  = ${needed_cfgPlmnId}")
            if(NRPMQAQP_cfgPlmnId != needed_cfgPlmnId){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "cfgPlmnId", needed_cfgPlmnId)
            }

            // ! UPDATE cfgProfType
            def NRPMQAQP_cfgProfType = NRPMQAP.getParameter("cfgProfType")
            // logger.info(" INFO: NRPMQAQP_cfgProfType = ${NRPMQAQP_cfgProfType} - recommended is: ${recommended_NRMQAP["cfgProfType"][index]}")
            if(NRPMQAQP_cfgProfType != recommended_NRMQAP["cfgProfType"][index]){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "cfgProfType", recommended_NRMQAP["cfgProfType"][index])
            }

            // ! UPDATE cfgQciRange
            def NRPMQAQP_cfgQciRange = NRPMQAP.getParameterAsInteger("cfgQciRange")
            // logger.info(" INFO: NRPMQAQP_cfgQciRange = ${NRPMQAQP_cfgQciRange} - recommended is: ${recommended_NRMQAP["cfgQciRange"][index]}")
            if(NRPMQAQP_cfgQciRange != recommended_NRMQAP["cfgQciRange"][index]){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "cfgQciRange", recommended_NRMQAP["cfgQciRange"][index] ?: "")
            }

            // ! UPDATE cfgSliceId
            def NRPMQAQP_cfgSliceId = NRPMQAP.getListParameter("cfgSliceId")
            // logger.info(" INFO: NRPMQAQP_cfgSliceId = ${NRPMQAQP_cfgSliceId}")
            def needed_cfgSliceId = createList()
            if (recommended_NRMQAP["cfgSliceId"]["exists"][index]){
                needed_cfgSliceId.addEntry([
                            "sd"    : recommended_NRMQAP["cfgSliceId"]["sd"][index],
                            "sst"   : recommended_NRMQAP["cfgSliceId"]["sst"][index],  ])
            }
            // logger.info(" INFO:         NEEDED is  = ${needed_cfgSliceId}")
            if(NRPMQAQP_cfgSliceId != needed_cfgSliceId){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "cfgSliceId", needed_cfgSliceId)
            }

            // ! UPDATE cfgSpidRange
            def NRPMQAQP_cfgSpidRange = NRPMQAP.getParameterAsInteger("cfgSpidRange")
            // logger.info(" INFO: NRPMQAQP_cfgSpidRange = ${NRPMQAQP_cfgSpidRange} - recommended is: ${recommended_NRMQAP["cfgSpidRange"][index]}")
            if(NRPMQAQP_cfgSpidRange != recommended_NRMQAP["cfgSpidRange"][index]){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "cfgSpidRange", recommended_NRMQAP["cfgSpidRange"][index] ?: "")
            }

            // ! UPDATE thpHistScale
            def NRPMQAQP_thpHistScale = NRPMQAP.getParameterAsInteger("thpHistScale")
            // logger.info(" INFO: NRPMQAQP_thpHistScale = ${NRPMQAQP_thpHistScale} - recommended is: ${recommended_NRMQAP["thpHistScale"][index]}")
            if(NRPMQAQP_thpHistScale != recommended_NRMQAP["thpHistScale"][index]){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "thpHistScale", recommended_NRMQAP["thpHistScale"][index] ?: "")
            }

            // ! UPDATE thpHistUplinkMaxRange
            def NRPMQAQP_thpHistUplinkMaxRange = NRPMQAP.getParameterAsInteger("thpHistUplinkMaxRange")
            // logger.info(" INFO: NRPMQAQP_thpHistUplinkMaxRange = ${NRPMQAQP_thpHistUplinkMaxRange} - recommended is: ${recommended_NRMQAP["thpHistUplinkMaxRange"][index]}")
            if(NRPMQAQP_thpHistUplinkMaxRange != recommended_NRMQAP["thpHistUplinkMaxRange"][index]){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "thpHistUplinkMaxRange", recommended_NRMQAP["thpHistUplinkMaxRange"][index] ?: "")
            }

            // ! UPDATE thpHistUplinkMinRange
            def NRPMQAQP_thpHistUplinkMinRange = NRPMQAP.getParameterAsInteger("thpHistUplinkMinRange")
            // logger.info(" INFO: NRPMQAQP_thpHistUplinkMinRange = ${NRPMQAQP_thpHistUplinkMinRange} - recommended is: ${recommended_NRMQAP["thpHistUplinkMinRange"][index]}")
            if(NRPMQAQP_thpHistUplinkMinRange != recommended_NRMQAP["thpHistUplinkMinRange"][index]){
                addRecommendation("UPDATE", NRPMQAP, "UPDATE parameter to correct value", "thpHistUplinkMinRange", recommended_NRMQAP["thpHistUplinkMinRange"][index] ?: "")
            }
        }
    }

    // logger.info(" INFO: List of existsing NRPMQAP indexes is    : ${list_of_existing_NRPMQAP}")
    // logger.info(" INFO: List of required NRPMQAP indexes is     : ${recommended_NRMQAP["originalId"]}")

    for (id in recommended_NRMQAP["originalId"]){
        if(!list_of_existing_NRPMQAP.contains(id)){
            // logger.info(" INFO: Missing NRPMQAP-${id}")

            def index = recommended_NRMQAP["originalId"].indexOf(id)
            NRPMQAP = buildMo("NRPMQAP", NRPMRNL, id)
            NRPMQAP.setParameterValue("defaults", "Default_5G");

            if(recommended_NRMQAP["cfg5qiRange"][id] != null){
                NRPMQAP.setParameterValue("cfg5qiRange", recommended_NRMQAP["cfg5qiRange"][id]);
            }
            if(recommended_NRMQAP["cfgProfType"][id] != null){
                NRPMQAP.setParameterValue("cfgProfType", recommended_NRMQAP["cfgProfType"][id]);
            }
            if(recommended_NRMQAP["cfgQciRange"][id] != null){
                NRPMQAP.setParameterValue("cfgQciRange", recommended_NRMQAP["cfgQciRange"][id]);
            }

            def needed_cfgPlmnId = createList()
            needed_cfgPlmnId.addEntry([
                        "mcc"           : recommended_NRMQAP["cfgPlmnId"]["mcc"][index],
                        "mnc"           : recommended_NRMQAP["cfgPlmnId"]["mnc"][index],
                        "mncLength"     : recommended_NRMQAP["cfgPlmnId"]["mncLength"][index],  ])
            NRPMQAP.setParameterValue("cfgPlmnId", needed_cfgPlmnId);

            def needed_cfgSliceId = createList()
            if (recommended_NRMQAP["cfgSliceId"]["exists"][index]){
                needed_cfgSliceId.addEntry([
                            "sd"    : recommended_NRMQAP["cfgSliceId"]["sd"][index],
                            "sst"   : recommended_NRMQAP["cfgSliceId"]["sst"][index],  ])
            }
            NRPMQAP.setParameterValue("cfgSliceId", needed_cfgSliceId);
        }
    }
}

logger.info("End of the CPR: cpr_name")