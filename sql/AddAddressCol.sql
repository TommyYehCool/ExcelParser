ALTER TABLE `land`.`company` ADD COLUMN `VC_ADDRESS` VARCHAR(50) NOT NULL DEFAULT ' ' AFTER `VC_EXTENSION_UNIT_UNDERTAKER`;
ALTER TABLE `land`.`legal_person` ADD COLUMN `VC_ADDRESS` VARCHAR(50) NOT NULL DEFAULT ' ' AFTER `VC_EXTENSION_UNIT_UNDERTAKER`;
ALTER TABLE `land`.`others` ADD COLUMN `VC_ADDRESS` VARCHAR(50) NOT NULL DEFAULT ' ' AFTER `VC_EXTENSION_UNIT_UNDERTAKER`;
ALTER TABLE `land`.`personal` ADD COLUMN `VC_ADDRESS` VARCHAR(50) NOT NULL DEFAULT ' ' AFTER `VC_EXTENSION_UNIT_UNDERTAKER`;