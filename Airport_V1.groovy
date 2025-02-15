/***************************************************************************************************************************************
 * * CPR: N_5G_Airport_Protection_Unlock,
 * !    Description: This CPR is to unlock the FEATCADM protection perameter lists whenever we find an inconsistency and perform the updates
 * !      If a cell is found to have inconsistencies, it will unlock the cell from the FEATCADM preventParamModlist and do the update
 *!
 * * Version 1:  Shannon Abeda -  July 2024
 * ?    
 *
 ***************************************************************************************************************************************/
def cpr_name = "N_5G_Airport_Protection_Unlock"
logger.info("Beginning - of CPR: " + cpr_name);
def number_of_NRBTS = 0

//Static Table Values for NRCELL
def nrcellExclusionList = ['PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-221': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-222': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-223': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-41': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-42': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-43': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-221': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-222': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-223': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-41': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-42': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-43': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-221': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-222': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-223': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-41': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-42': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-43': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-221': ['PMAX': '302', 'SSPBCHBLOCKPOWER': '11'],
                        'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-222': ['PMAX': '302', 'SSPBCHBLOCKPOWER': '11'],
                        'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-223': ['PMAX': '302', 'SSPBCHBLOCKPOWER': '11'],
                        'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-41': ['PMAX': '349', 'SSPBCHBLOCKPOWER': '11'],
                        'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-42': ['PMAX': '349', 'SSPBCHBLOCKPOWER': '11'],
                        'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-43': ['PMAX': '349', 'SSPBCHBLOCKPOWER': '11'],
                        'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-221': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-222': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-223': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-41': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-42': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-43': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549726/NRBTS-1549726/NRCELL-221': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549726/NRBTS-1549726/NRCELL-222': ['PMAX': '259', 'SSPBCHBLOCKPOWER': '10'],
                        'PLMN-PLMN/MRBTS-1549726/NRBTS-1549726/NRCELL-41': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549726/NRBTS-1549726/NRCELL-42': ['PMAX': '307', 'SSPBCHBLOCKPOWER': '10'],
                        'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-221': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-222': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-223': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-41': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-42': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-43': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1550856/NRBTS-1550856/NRCELL-221': ['PMAX': '207', 'SSPBCHBLOCKPOWER': '5'],
                        'PLMN-PLMN/MRBTS-1550856/NRBTS-1550856/NRCELL-222': ['PMAX': '207', 'SSPBCHBLOCKPOWER': '5'],
                        'PLMN-PLMN/MRBTS-1550856/NRBTS-1550856/NRCELL-41': ['PMAX': '255', 'SSPBCHBLOCKPOWER': '5'],
                        'PLMN-PLMN/MRBTS-1550856/NRBTS-1550856/NRCELL-42': ['PMAX': '255', 'SSPBCHBLOCKPOWER': '5'],
                        'PLMN-PLMN/MRBTS-1551084/NRBTS-1551084/NRCELL-41': ['PMAX': '448', 'SSPBCHBLOCKPOWER': '9'],
                        'PLMN-PLMN/MRBTS-1551084/NRBTS-1551084/NRCELL-42': ['PMAX': '448', 'SSPBCHBLOCKPOWER': '9'],
                        'PLMN-PLMN/MRBTS-1551084/NRBTS-1551084/NRCELL-43': ['PMAX': '448', 'SSPBCHBLOCKPOWER': '9'],
                        'PLMN-PLMN/MRBTS-1551084/NRBTS-1551084/NRCELL-221': ['PMAX': '400', 'SSPBCHBLOCKPOWER': '9'],
                        'PLMN-PLMN/MRBTS-1551084/NRBTS-1551084/NRCELL-222': ['PMAX': '400', 'SSPBCHBLOCKPOWER': '9'],
                        'PLMN-PLMN/MRBTS-1551084/NRBTS-1551084/NRCELL-223': ['PMAX': '400', 'SSPBCHBLOCKPOWER': '9'],
                        'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-223': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-41': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-42': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-43': ['PMAX': '337', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-221': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-222': ['PMAX': '289', 'SSPBCHBLOCKPOWER': '13'],
                        'PLMN-PLMN/MRBTS-1560283/NRBTS-1560283/NRCELL-221': ['PMAX': '400', 'SSPBCHBLOCKPOWER': '9'],
                        'PLMN-PLMN/MRBTS-1560283/NRBTS-1560283/NRCELL-222': ['PMAX': '400', 'SSPBCHBLOCKPOWER': '9'],
                        'PLMN-PLMN/MRBTS-1560283/NRBTS-1560283/NRCELL-41': ['PMAX': '448', 'SSPBCHBLOCKPOWER': '9'],
                        'PLMN-PLMN/MRBTS-1560283/NRBTS-1560283/NRCELL-42': ['PMAX': '448', 'SSPBCHBLOCKPOWER': '9']]


def beamSetList = ['PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-221':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-222':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-223':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-41':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-42':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549234/NRBTS-1549234/NRCELL-43':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-221':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-222':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-223':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-41':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-42':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549247/NRBTS-1549247/NRCELL-43':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-221':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-222':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-223':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-41':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-42':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549579/NRBTS-1549579/NRCELL-43':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-221':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-222':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-223':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-41':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-42':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549768/NRBTS-1549768/NRCELL-43':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-221':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-222':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-223':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-42':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549769/NRBTS-1549769/NRCELL-43':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-221':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-222':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-223':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-41':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-42':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549808/NRBTS-1549808/NRCELL-43':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549726/NRBTS-1549726/NRCELL-221':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549726/NRBTS-1549726/NRCELL-222':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549726/NRBTS-1549726/NRCELL-41':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1549726/NRBTS-1549726/NRCELL-42':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1550856/NRBTS-1550856/NRCELL-221':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1550856/NRBTS-1550856/NRCELL-222':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1550856/NRBTS-1550856/NRCELL-41':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1550856/NRBTS-1550856/NRCELL-42':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-221':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-222':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-223':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-41':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-42':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80'],
    'PLMN-PLMN/MRBTS-1553203/NRBTS-1553203/NRCELL-43':['LEFTEDGEANGLE':'45','RIGHTEDGEANGLE':'45','TILTOFFSET':'0','BASICBEAMSET':'16','BEAMSET':'1','BEAMSETINUSE':'beamSet_6', 'NRBTSBEAMREFINEMENT': '0', 'LOWEREDGEangle': '135', 'UPPEREDGEangle': '80']]

def NR_RETU_exceptionList = [
        'PLMN-PLMN/MRBTS-1560283/EQM-1/APEQM-1/ALD-10/RETU-1' : ['ANGLE': '60', 'ANTBEARING': '1400', 'ANTMODEL': 'RV4S4-65A-R6', 'BASESTATIONID': 'X1801', 'SECTORID': '92A/92B/92C/92D/92E/92F/92G/92H'],
        'PLMN-PLMN/MRBTS-1560283/EQM-1/APEQM-1/ALD-5/RETU-1' : ['ANGLE': '60', 'ANTBEARING': '200', 'ANTMODEL': 'RV4S4-65A-R6', 'BASESTATIONID': 'X1801', 'SECTORID': '91A/91B/91C/91D/91E/91F/91G/91H']
]

def LTE_RETU_exceptionList = [
    'PLMN-PLMN/MRBTS-551084/EQM-1/APEQM-1/ALD-16/RETU-1' : ['ANGLE': '60', 'ANTBEARING': '2000', 'ANTMODEL': 'RV4S4-65A-R6', 'BASESTATIONID': 'W1289', 'SECTORID': '92A/92B/92C/92D/92E/92F/92G/92H'],
    'PLMN-PLMN/MRBTS-551084/EQM-1/APEQM-1/ALD-28/RETU-1' : ['ANGLE': '60', 'ANTBEARING': '3200', 'ANTMODEL': 'RV4S4-65A-R6', 'BASESTATIONID': 'W1289', 'SECTORID': '93A/93B/93C/93D/93E/93F/93G/93H'],
    'PLMN-PLMN/MRBTS-551084/EQM-1/APEQM-1/ALD-4/RETU-1' : ['ANGLE': '60', 'ANTBEARING': '700', 'ANTMODEL': 'RV4S4-65A-R6', 'BASESTATIONID': 'W1289', 'SECTORID': '91A/91B/91C/91D/91E/91F/91G/91H']
]


//? This function is used to check if there are inconsistencies in a specified NRCELL.PMAX
//*     -> if the provided NRCELL is not part of the Airport protection Zone then we return FALSE
//*     -> if PMAX is NOT correct we return TRUE
//*     -> if PMAX is correct we return FALSE
def isNRCELL_PMAX_incorrect = { NRCELL ->
    def NRCELL_distName = NRCELL.getParameter("distName");
    def PMAX_recommendedParams = nrcellExclusionList[NRCELL_distName];
    if(PMAX_recommendedParams) {
        logger.info("INFO 1B: This cell is contained within the static table for PMax updates");
        def required_pmax     = PMAX_recommendedParams["PMAX"];
        def NRCELL_pmax       =  NRCELL.getParameter("pMax");
        logger.info("INFO 1C: Current PMax  is ${NRCELL_pmax}                   And required value is ${required_pmax}");
        if(NRCELL_pmax != required_pmax ){
            logger.info(" INFO 1D: PMax is not correct");
            addRecommendation("UPDATE", NRCELL, "Updating PMax value from current: " + NRCELL_pmax + " to: " + required_pmax, "pMax", required_pmax);
            logger.info("INFO: PMAX Update complete");
            return true;
        }
        
        else {
            logger.info("INFO 1E: PMax is correct and no updates are needed");
            return false;
        }
    } 
    logger.info("INFO 1F: This Cell is not contained within the static table for PMax updates");
    return false;
}

//? This function is used to check if there are inconsistencies in a specified NRCELL.SSPBCHBLOCKPOWER
//*     -> if the provided NRCELL is not part of the Airport protection Zone then we return FALSE
//*     -> if SSPBCHBLOCKPOWER is NOT correct we return TRUE
//*     -> if SSPBCHBLOCKPOWER is correct we return FALSE
def isNRCELL_SSPBCHBLOCKPOWER_incorrect = { NRCELL ->
    def NRCELL_distName = NRCELL.getParameter("distName");
    def PMAX_recommendedParams = nrcellExclusionList[NRCELL_distName];
    if(PMAX_recommendedParams) {
        logger.info("INFO 1B: This cell is contained within the static table for PMax updates");
        def required_pmax     = PMAX_recommendedParams["PMAX"];
        def NRCELL_pmax       =  NRCELL.getParameter("pMax");
        logger.info("INFO 1C: Current PMax  is ${NRCELL_pmax}                   And required value is ${required_pmax}");
        if(NRCELL_pmax != required_pmax ){
            logger.info(" INFO 1D: PMax is not correct");
            addRecommendation("UPDATE", NRCELL, "Updating PMax value from current: " + NRCELL_pmax + " to: " + required_pmax, "pMax", required_pmax);
            logger.info("INFO: PMAX Update complete");
            return true;
        }
        
        else {
            logger.info("INFO 1E: PMax is correct and no updates are needed");
            return false;
        }
    } 
    logger.info("INFO 1F: This Cell is not contained within the static table for PMax updates");
    return false;
}




//? This function is used to check if there are inconsistencies in a specified NRCELL
//*     -> if the provided NRCELL is not part of the Airport protection Zone then we return FALSE
//*     -> if beamSet is NOT correct we return TRUE
//*     -> if beamSet is correct we return FALSE
def isNRCELL_BEAMSET_incorrect = { NRCELL ->
    def NRCELL_distName = NRCELL.getParameter("distName")
    def BEAMSET_recommendedParams = beamSetList[NRCELL_distName];
    // if the NRCELL distname is not in the exception list keys then the BTSis not part of the airport protection zone
    if(!BEAMSET_recommendedParams){
        logger.info(" INFO 3A: The CELL is not part of the airport Protection zone for beamSet Updates");
        return false
    } 
    else {
        def required_leftEdgeAngle              = BEAMSET_recommendedParams["LEFTEDGEANGLE"]
        def required_rightEdgeAngle             = BEAMSET_recommendedParams["RIGHTEDGEANGLE"]
        def required_tiltOffset                 = BEAMSET_recommendedParams["TILTOFFSET"]
        def required_basicBeamSet               = BEAMSET_recommendedParams["BASICBEAMSET"]
        def required_beamSetInUse               = BEAMSET_recommendedParams["BEAMSETINUSE"]
        def required_nrBtsBeamRefinementP2      = BEAMSET_recommendedParams["NRBTSBEAMREFINEMENT"]
        def required_lowerEdgeAngle             = BEAMSET_recommendedParams["LOWEREDGEangle"]
        def required_upperEdgeAngle             = BEAMSET_recommendedParams["UPPEREDGEangle"]

        def NRCELL_beamSet = NRCELL.getListParameter('beamSet')
        def NRCELL_beamSet_leftEdgeAngle         = NRCELL_beamSet[0]["leftEdgeAngle"]
        def NRCELL_beamSet_rightEdgeAngle        = NRCELL_beamSet[0]["rightEdgeAngle"]
        def NRCELL_beamSet_tiltOffset            = NRCELL_beamSet[0]["tiltOffset"]
        def NRCELL_beamSet_basicBeamSet          = NRCELL_beamSet[0]["basicBeamSet"]
        def NRCELL_beamSet_beamSetInUse          = NRCELL_beamSet[0]["beamSetInUse"]
        def NRCELL_beamSet_nrBtsBeamRefinementP2 = NRCELL_beamSet[0]["nrBtsBeamRefinementP2"]
        def NRCELL_beamSet_lowerEdgeAngle        = NRCELL_beamSet[0]["lowerEdgeAngle"]
        def NRCELL_beamSet_upperEdgeAngle        = NRCELL_beamSet[0]["upperEdgeAngle"]

        logger.info("Current leftEdgeAngle  is ${NRCELL_beamSet_leftEdgeAngle}                 And required value is ${required_leftEdgeAngle}")
        logger.info("Current rightEdgeAngle is ${NRCELL_beamSet_rightEdgeAngle}                And required value is ${required_rightEdgeAngle}")
        logger.info("Current tiltOffset is ${NRCELL_beamSet_tiltOffset}                        And required value is ${required_tiltOffset}")
        logger.info("Current basicBeamSet   is ${NRCELL_beamSet_basicBeamSet}                  And required value is ${required_basicBeamSet}")
        logger.info("Current beamSetInUse   is ${NRCELL_beamSet_beamSetInUse}                  And required value is ${required_beamSetInUse}")
        logger.info("Current nrBtsBeamRefinementP2  is ${NRCELL_beamSet_nrBtsBeamRefinementP2} And required value is ${required_nrBtsBeamRefinementP2}")
        logger.info("Current lowerEdgeAngle is ${NRCELL_beamSet_lowerEdgeAngle}                And required value is ${required_lowerEdgeAngle}")
        logger.info("Current upperEdgeAngle is ${NRCELL_beamSet_upperEdgeAngle}                And required value is ${required_upperEdgeAngle}")



        if( required_leftEdgeAngle           != NRCELL_beamSet_leftEdgeAngle           ||
            required_rightEdgeAngle          != NRCELL_beamSet_rightEdgeAngle          ||
            required_tiltOffset              != NRCELL_beamSet_tiltOffset              ||
            required_basicBeamSet            != NRCELL_beamSet_basicBeamSet            ||
            required_beamSetInUse            != NRCELL_beamSet_beamSetInUse            ||
            required_nrBtsBeamRefinementP2   != NRCELL_beamSet_nrBtsBeamRefinementP2   ||
            required_lowerEdgeAngle          != NRCELL_beamSet_lowerEdgeAngle          ||
            required_upperEdgeAngle          != NRCELL_beamSet_upperEdgeAngle          ){
            logger.info(" INFO 3B: One of the protected parameter is not correct");

            newBeamSetList = createList();
            newBeamSetList.addEntry([
                "leftEdgeAngle" : required_leftEdgeAngle,
                "rightEdgeAngle" : required_rightEdgeAngle,
                "tiltOffset" : required_tiltOffset,
                "basicBeamSet" : required_basicBeamSet,
                "beamSetInUse" : required_beamSetInUse,
                "nrBtsBeamRefinementP2" : required_nrBtsBeamRefinementP2,
                "lowerEdgeAngle" : required_lowerEdgeAngle,
                "upperEdgeAngle" : required_upperEdgeAngle
            ])
            addRecommendation("UPDATE", NRCELL, "Updating the BeamSet", "beamSet", newBeamSetList);
            logger.info("INFO: BEAMSET Update complete");
            return true
        }

        else {
            logger.info("INFO: BEAMSET parameters are aligned, no updates are needed");
            return false;
        }
    }
}

//? This function is used to check if there are inconsistencies the 4G collocated (values harcoded in LTE_RETU_exceptionList table)
// *     -> if there is NO 4Gcollocated we return FALSE
// *     -> if there is a 4G collocatd:
// *        -> if the LNBTS/EQM/APEQM/ALD/RETU is NOT in the LTE_RETU_exceptionList table we return FLASE
// *        -> if the LNBTS/EQM/APEQM/ALD/RETU configuration is NOT correct we return TRUE
// *        -> if the LNBTS/EQM/APEQM/ALD/RETU configuration is correct we return FLASE
def isCollocated_4G_RETU_incorrect = { NRCELL ->
    def LNCEL = NRCELL.getMos("LNCEL");
    def MRBTS = LNCEL.getMos("MRBTS");
    if(MRBTS.size() == 0){
        logger.info("INFO 4A: This cell is NOT collocated with 4G");
        return false
    } else {
        for(RETU in MRBTS.getMos("RETU")){
            logger.info("INFO 4B: This cell is collocated with 4G");
            def RETU_distName = RETU.getParameter("distName")
            logger.info(" INFO 4C: Evaluating ${RETU_distName}")
            if (!LTE_RETU_exceptionList.keySet().contains(RETU_distName)){
                logger.info("INFO 4D: This cell is NOT contained within the update list");
                return false
            } else {
                logger.info("INFO 4E: This cell is contained within the update list");
                def recommendedRetuParams = LTE_RETU_exceptionList[RETU_distName]
                def required_angle          = recommendedRetuParams["ANGLE"]
                def required_antBearing     = recommendedRetuParams["ANTBEARING"]
                def required_antModel       = recommendedRetuParams["ANTMODEL"]
                def required_baseStationID  = recommendedRetuParams["BASESTATIONID"]
                def required_sectorID       = recommendedRetuParams["SECTORID"]

                def RETU_angle         = retu.getParameter("angle");
                def RETU_antBearing    = retu.getParameter("antBearing");
                def RETU_antModel      = retu.getParameter("antModel");
                def RETU_baseStationID = retu.getParameter("baseStationID");
                def RETU_sectorID      = retu.getParameter("sectorID");

                logger.info(" INFO: current angle = ${RETU_angle}                  AND required value is ${required_angle}")
                logger.info(" INFO: current antBearing = ${RETU_antBearing}        AND required value is ${required_antBearing}")
                logger.info(" INFO: current antModel = ${RETU_antModel}            AND required value is ${required_antModel}")
                logger.info(" INFO: current baseStationID = ${RETU_baseStationID}  AND required value is ${required_baseStationID}")
                logger.info(" INFO: current sectorID = ${RETU_sectorID}            AND required value is ${required_sectorID}")

                if (RETU_angle!= required_angle) {          
                    addRecommendation("UPDATE", RETU, "UPDATING ANGLE PARAMETER DUE TO DISCREPENCY FROM: " + RETU_angle + " TO: " + required_angle, "angle", required_angle);
                    logger.info("INFO 4F: Angle updated");
                }

                if(RETU_antBearing != required_antBearing) {
                    addRecommendation("UPDATE", RETU, "UPDATING ANGLE PARAMETER DUE TO DISCREPENCY FROM: " + RETU_antBearing + " TO: " + required_antBearing, "antBearing", required_antBearing);
                    logger.info("INFO 4G: antBearing updated");
                }

                if(RETU_antModel != required_antModel){
                    addRecommendation("UPDATE", RETU, "UPDATING ANGLE PARAMETER DUE TO DISCREPENCY FROM: " + RETU_antModel + " TO: " + required_antModel, "antModel", required_antModel);
                    logger.info("INFO 4H: antModel updated");
                }

                if(RETU_baseStationID != required_baseStationID) {
                    addRecommendation("UPDATE", RETU, "UPDATING ANGLE PARAMETER DUE TO DISCREPENCY FROM: " + RETU_baseStationID + " TO: " + required_baseStationID, "baseStationID", required_baseStationID);
                    logger.info("INFO 4I: baseStationID updated");
                }

                if(RETU_sectorID != required_sectorID) {
                    addRecommendation("UPDATE", RETU, "UPDATING ANGLE PARAMETER DUE TO DISCREPENCY FROM: " + RETU_sectorID + " TO: " + required_sectorID, "sectorID", required_sectorID);
                    logger.info("INFO 4J: sectorID updated");
                }
            }
        }
    }
}

//? This function is used to check if the NRBTS/EQM/APEQM/ALD/RETU hasinconsistencies (values harcoded in NR_RETU_exceptionList table)
// *    -> if the NRBTS/EQM/APEQM/ALD/RETU is NOT in the NR_RETU_exceptionList table we return FLASE
// *    -> if the NRBTS/EQM/APEQM/ALD/RETU configuration is NOT correct we return TRUE
// *    -> if the NRBTS/EQM/APEQM/ALD/RETU configuration is correct we return FLASE
def 5G_RETU_incorrect = { NRBTS ->
    for(RETU in NRBTS.getMos("RETU")){
        def RETU_distName = RETU.getParameter("distName")
        logger.info(" INFO 5A: Evaluating ${RETU_distName}")
        if (!NR_RETU_exceptionList.keySet().contains(RETU_distName)){
            logger.info(" INFO 5B: This cell is not contained within the updates for RETU 5G");
            return false
        } 
        else {
            def recommendedRetuParams   = NR_RETU_exceptionList[RETU_distName]
            def required_angle          = recommendedRetuParams["ANGLE"]
            def required_antBearing     = recommendedRetuParams["ANTBEARING"]
            def required_antModel       = recommendedRetuParams["ANTMODEL"]
            def required_baseStationID  = recommendedRetuParams["BASESTATIONID"]
            def required_sectorID       = recommendedRetuParams["SECTORID"]

            def RETU_angle         = retu.getParameter("angle");
            def RETU_antBearing    = retu.getParameter("antBearing");
            def RETU_antModel      = retu.getParameter("antModel");
            def RETU_baseStationID = retu.getParameter("baseStationID");
            def RETU_sectorID      = retu.getParameter("sectorID");

            def counter;

            logger.info(" INFO: current angle = ${RETU_angle}                  AND required value is ${required_angle}")
            logger.info(" INFO: current antBearing = ${RETU_antBearing}        AND required value is ${required_antBearing}")
            logger.info(" INFO: current antModel = ${RETU_antModel}            AND required value is ${required_antModel}")
            logger.info(" INFO: current baseStationID = ${RETU_baseStationID}  AND required value is ${required_baseStationID}")
            logger.info(" INFO: current sectorID = ${RETU_sectorID}            AND required value is ${required_sectorID}")

            if (RETU_angle!= required_angle){      
                addRecommendation("UPDATE", RETU, "UPDATING ANGLE PARAMETER DUE TO DISCREPENCY FROM: " + RETU_angle + " TO: " + required_angle, "angle", required_angle);
                logger.info("INFO 5C: Angle updated");
            }


            if(RETU_antBearing != required_antBearing) {
                addRecommendation("UPDATE", RETU, "UPDATING ANGLE PARAMETER DUE TO DISCREPENCY FROM: " + RETU_antBearing + " TO: " + required_antBearing, "antBearing", required_antBearing);
                logger.info("INFO 5D: antBearing updated");
            }

            if(RETU_antModel != required_antModel){
                addRecommendation("UPDATE", RETU, "UPDATING ANGLE PARAMETER DUE TO DISCREPENCY FROM: " + RETU_antModel + " TO: " + required_antModel, "antModel", required_antModel);
                logger.info("INFO 5E: antModel updated");
            }

            if(RETU_baseStationID != required_baseStationID) {
                addRecommendation("UPDATE", RETU, "UPDATING ANGLE PARAMETER DUE TO DISCREPENCY FROM: " + RETU_baseStationID + " TO: " + required_baseStationID, "baseStationID", required_baseStationID);
                logger.info("INFO 5F: baseStationID updated");
            }

            if(RETU_sectorID != required_sectorID) {
                addRecommendation("UPDATE", RETU, "UPDATING ANGLE PARAMETER DUE TO DISCREPENCY FROM: " + RETU_sectorID + " TO: " + required_sectorID, "sectorID", required_sectorID);
                logger.info("INFO 5G: sectorID updated");
            }

        }
        logger.info("INFO: No discrepency found for NR RETU PARAMETERS");
    }
}


for (NRBTS in _context.getMos("NRBTS")){
    number_of_NRBTS ++
    def NRCELLS = NRBTS.getMos("NRCELL");
    def FEATCADM    = MRBTS.getMos("FEATCADM");
    def current_prevParamModTargetDN    = FEATCADM.getListEnumerationParameter("preventParamModifTargetDN");
    def required_prevParamModTargetDN = [];
    freqBandIndicatorN78List = [];
    def needed_prevParamModList = [ "NRCELL-pMax","NRCELL-beamSet","NRCELL-ssPbchBlockPower"];
    def current_prevParamModList = FEATCADM.getListEnumerationParameter("preventParamModifList");
    def isUpdated_preventParamModifTargetDN     = false;
    def isUpdated_preventParamModifList         = false;


    logger.info("INFO 16:  N78 Restricted current NRcells from preventParamModifTargetDN size is: " + current_prevParamModTargetDN.size() + " current_prevParamModTargetDN: " + current_prevParamModTargetDN );

    // * At the beginning the distName list is not changed, we intitial to null

    for (NRCELL in NRCELLS){
        def NRCELL_distName     = NRCELL.getParameter("distName")
        NR_DistName_format       = NRCELL_distName.substring(10);
        freqBandIndicatorNR      = NRCELL.getParameterAsInteger("freqBandIndicatorNR");
        logger.info(" INFO 17: Evaluating ${NRCELL_distName} for discrepancies");
        


        //This section will check for each section of the code
        //This is an addition as the condition below was only running through one section and getting triggered
        logger.info(" INFO: Checking for PMax for Inconsistency");
        def pMax_Inconsistency = isNRCELL_PMAX_incorrect(NRCELL) 

        logger.info(" INFO: Checking for sspbchBlockPower for Inconsistency");
        def sspbchBlockPower_Inconsistency = isNRCELL_SSPBCHBLOCKPOWER_incorrect(NRCELL)
        
        logger.info(" INFO: Checking for beamSet for Inconsistency");
        def beamSet_Inconsistency = isNRCELL_BEAMSET_incorrect(NRCELL)
        
        logger.info(" INFO: Checking for 5G Retu for Inconsistency");
        5G_RETU_incorrect(NRBTS)
        
        //MRBTS is added instead due to MO MAPPING issues with NRBTS
        logger.info(" INFO: Checking for 4G Retu for Inconsistency");
        isCollocated_4G_RETU_incorrect(NRCELL)

        logger.info(" INFO: The PMax flag on this cell is set to: ${pMax_Inconsistency}");
        logger.info(" INFO: The sspbchBlockPower flag on this cell is set to: ${spbchBlockPower_Inconsistency}");
        logger.info(" INFO: The beamSet flag on this cell is set to: ${beamSet_Inconsistency}");


        if(pMax_Inconsistency                    || // * Checking if there is a discrepency, if no discrepency is found it is not added to the list
        sspbchBlockPower_Inconsistency           ||
        beamSet_Inconsistency                    ){
            logger.info("INFO 18: Discrenpancies found on ${NR_DistName_format}!"); 
            logger.info("INFO 19: This cell: ${NR_DistName_format} will not be added to the locked list");
        }

        else {                                                                      // * Adding all other remaining cells to the preventParamModifTargetDn that are N78 cells
            logger.info("INFO 20: No disrepencies are found we will check for other cells that are not locked and add them to the list to be locked");
            if(freqBandIndicatorNR == 78) {
                logger.info("INFO 21: This cell: {NR_DistName_format} is N78 NOT locked & will be added to the list");
                required_prevParamModTargetDN.add(NR_DistName_format);
                freqBandIndicatorN78List.add(freqBandIndicatorNR);
            }

            else {
                logger.info("INFO 21: This cell is NOT N78 and will NOT be added to the list");
            }
        }
    } // end of nrcell

    // This section is previously written by Bineesh V1 CPR
    if(freqBandIndicatorN78List.size() > 0) { //Filtering out N78 sites only
        //* Comparing the lists to see if they match with the required lists
        logger.info("---> INFO 22: The current prevParamModList size is: " + current_prevParamModList.size());
        logger.info("---> INFO 23: The needed_prevParamModList size is: " + needed_prevParamModList.size());
        logger.info("---> INFO 24: The current_prevParamModTargetDN size is:  " + current_prevParamModTargetDN.size());
        logger.info("---> INFO 25: The required_FEATCADM_prevParamModTargetDN size is:  " + required_prevParamModTargetDN.size());
        if(current_prevParamModTargetDN.size()!=required_prevParamModTargetDN.size()){                            // ? Current and Needed Target lists sizes are differents
            logger.info("INFO 26 :UPDATE  featcadm.prevParamModTargetDN, current and needed lists have different sizes" );
            isUpdated_preventParamModifTargetDN = true
            def new_prevParamModTargetDN = createListEnum();
            for(i = 0; i < required_prevParamModTargetDN.size(); i++){
                new_prevParamModTargetDN.addEntry(required_prevParamModTargetDN[i])
            }
            logger.info("INFO: The new updated list is ")
            addRecommendation("UPDATE", FEATCADM, "Update featcadm.prevParamModTargetDN from  " + current_prevParamModTargetDN +" to " + new_prevParamModTargetDN ,"preventParamModifTargetDN",new_prevParamModTargetDN);
    
        } else {                                                                                             // ? Current and Needed Target lists sizes are the same
            for (i = 0; i < required_prevParamModTargetDN.size(); i++) {
                if ( ! required_prevParamModTargetDN.contains(current_prevParamModTargetDN.getItem(i))) {         // ? Current and Needed Target lists are differents
                    logger.info("INFO 27: UPDATE  featcadm.prevParamModTargetDN, current and needed lists are different" );
                    isUpdated_preventParamModifTargetDN = true
                    def new_prevParamModTargetDN = createListEnum();
                    for(i = 0; i < required_prevParamModTargetDN.size(); i++){
                        new_prevParamModTargetDN.addEntry(required_prevParamModTargetDN[i])
                    }
                    addRecommendation("UPDATE", FEATCADM, "Update featcadm.prevParamModTargetDN from  " + current_prevParamModTargetDN +" to " + new_prevParamModTargetDN ,"preventParamModifTargetDN",new_prevParamModTargetDN);
                }
            }
        }
    
        if(current_prevParamModList.size()!= needed_prevParamModList.size()){                                   // ? Current and Needed parameter lists sizes are differents
            logger.info("INFO 28: UPDATE  featcadm.preventParamModifList, current and needed lists have different sizes" );
            isUpdated_preventParamModifList = true
            def new_prevParamModList = createListEnum();
            for(i = 0; i < needed_prevParamModList.size(); i++){
                new_prevParamModList.addEntry(needed_prevParamModList[i])
            }
            addRecommendation("UPDATE", FEATCADM, "Update featcadm.preventParamModifList from  " + current_prevParamModList + " to " + new_prevParamModList ,"preventParamModifList",new_prevParamModList);
    
        } else {                                                                                                // ? Current and Needed parameter lists sizes are the same
            for (i = 0; i < needed_prevParamModList.size(); i++){
                if ( ! needed_prevParamModList.contains(current_prevParamModList.getItem(i))) {                 // ? Current and Needed parameter lists lists are differents
                    logger.info("INFO 29: UPDATE  featcadm.preventParamModifList, current and needed lists are different" );
                    isUpdated_preventParamModifList = true
                    def new_prevParamModList = createListEnum();
                    for(i = 0; i < needed_prevParamModList.size(); i++){
                        new_prevParamModList.addEntry(needed_prevParamModList[i])
                    }
                    addRecommendation("UPDATE", FEATCADM, "Update featcadm.preventParamModifList from  " + current_prevParamModList + " to " + new_prevParamModList ,"preventParamModifList",new_prevParamModList);
    
                }
            }
        }
    }
   
    if (!isUpdated_preventParamModifTargetDN){
        logger.info("INFO 30: preventParamModifTargetDN is correct, no update needed.");
    }

    if (!isUpdated_preventParamModifList){
        logger.info("INFO 31: preventParamModifList is correct, no update needed.");
    }

    
    
} // END CPR

logger.info("Total number of evaluated objects: NRBTS " + number_of_nrbts);
logger.info("End of the CPR: " + cpr_name);