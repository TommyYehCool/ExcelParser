ALTER TABLE `land`.`company` ADD INDEX `index_owner`(`VC_OWNER`);                                    
ALTER TABLE `land`.`company` ADD INDEX `index_counties`(`VC_COUNTIES`);                              
ALTER TABLE `land`.`company` ADD INDEX `index_townships`(`VC_TOWNSHIPS`);                            
ALTER TABLE `land`.`company` ADD INDEX `index_area`(`N_AREA`);                                       
ALTER TABLE `land`.`company` ADD INDEX `index_land_use_partition`(`VC_LAND_USE_PARTITION`);          

ALTER TABLE `land`.`legal_person` ADD INDEX `index_owner`(`VC_OWNER`);                          
ALTER TABLE `land`.`legal_person` ADD INDEX `index_counties`(`VC_COUNTIES`);                    
ALTER TABLE `land`.`legal_person` ADD INDEX `index_townships`(`VC_TOWNSHIPS`);                  
ALTER TABLE `land`.`legal_person` ADD INDEX `index_area`(`N_AREA`);                             
ALTER TABLE `land`.`legal_person` ADD INDEX `index_land_use_partition`(`VC_LAND_USE_PARTITION`);

ALTER TABLE `land`.`others` ADD INDEX `index_owner`(`VC_OWNER`);                           
ALTER TABLE `land`.`others` ADD INDEX `index_counties`(`VC_COUNTIES`);                     
ALTER TABLE `land`.`others` ADD INDEX `index_townships`(`VC_TOWNSHIPS`);                   
ALTER TABLE `land`.`others` ADD INDEX `index_area`(`N_AREA`);                              
ALTER TABLE `land`.`others` ADD INDEX `index_land_use_partition`(`VC_LAND_USE_PARTITION`); 

ALTER TABLE `land`.`personal` ADD INDEX `index_owner`(`VC_OWNER`);
ALTER TABLE `land`.`personal` ADD INDEX `index_counties`(`VC_COUNTIES`);
ALTER TABLE `land`.`personal` ADD INDEX `index_townships`(`VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal` ADD INDEX `index_area`(`N_AREA`);
ALTER TABLE `land`.`personal` ADD INDEX `index_land_use_partition`(`VC_LAND_USE_PARTITION`);

ALTER TABLE `land`.`personal_taipei_1` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taipei_2` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taipei_3` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taipei_4` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);

ALTER TABLE `land`.`personal_newtaipei_1` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_newtaipei_2` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_newtaipei_3` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_newtaipei_4` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_newtaipei_5` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_newtaipei_6` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_newtaipei_7` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_newtaipei_8` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_newtaipei_9` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_newtaipei_10` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);

ALTER TABLE `land`.`personal_taoyuan_1` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taoyuan_2` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taoyuan_3` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taoyuan_4` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taoyuan_5` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);

ALTER TABLE `land`.`personal_taichung_1` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taichung_2` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taichung_3` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taichung_4` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_taichung_5` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);

ALTER TABLE `land`.`personal_kaohsung_1` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_kaohsung_2` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_kaohsung_3` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_kaohsung_4` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);
ALTER TABLE `land`.`personal_kaohsung_5` ADD INDEX `index_count_town`(`VC_COUNTIES`, `VC_TOWNSHIPS`);