/*
Navicat MySQL Data Transfer

Source Server         : 172.27.108.93
Source Server Version : 50722
Source Host           : 172.27.108.93:3306
Source Database       : filestorage

Target Server Type    : MYSQL
Target Server Version : 50722
File Encoding         : 65001

Date: 2018-09-06 11:16:29
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
`id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
`name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限描述' ,
`gmt_create`  datetime NULL DEFAULT NULL ,
`gmt_modify`  datetime NULL DEFAULT NULL ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
AUTO_INCREMENT=5

;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
BEGIN;
INSERT INTO `sys_permission` VALUES ('1', 'SEARCH', '2018-08-31 09:38:41', null), ('2', 'UPLOAD', '2018-08-31 09:38:53', null), ('3', 'DELETE', '2018-08-31 09:39:09', null), ('4', 'UPDATE_FILE', '2018-08-31 15:33:52', null);
COMMIT;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
`id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
`name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
`level`  tinyint(5) NULL DEFAULT NULL ,
`gmt_create`  datetime NULL DEFAULT NULL ,
`gmt_modify`  datetime NULL DEFAULT NULL ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
AUTO_INCREMENT=3

;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` VALUES ('1', 'ADMIN', '1', '2018-08-31 09:38:11', null), ('2', 'CUSTOM_USER', '2', '2018-08-31 14:10:17', null);
COMMIT;

-- ----------------------------
-- Table structure for sys_role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permissions`;
CREATE TABLE `sys_role_permissions` (
`id`  int(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
`rid`  int(10) UNSIGNED NOT NULL ,
`pid`  int(10) UNSIGNED NOT NULL ,
`gmt_create`  datetime NULL DEFAULT NULL ,
`gmt_modify`  datetime NULL DEFAULT NULL ,
PRIMARY KEY (`id`),
FOREIGN KEY (`pid`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
FOREIGN KEY (`rid`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
INDEX `fk_rid` (`rid`) USING BTREE ,
INDEX `fk_pid` (`pid`) USING BTREE 
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
AUTO_INCREMENT=7

;

-- ----------------------------
-- Records of sys_role_permissions
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_permissions` VALUES ('1', '1', '1', '2018-08-31 09:39:23', null), ('2', '1', '2', '2018-08-31 09:39:29', null), ('3', '1', '3', '2018-08-31 09:39:39', null), ('4', '2', '1', '2018-08-31 14:11:04', null), ('5', '2', '2', '2018-08-31 14:11:17', null), ('6', '2', '4', '2018-08-31 15:34:27', null);
COMMIT;

-- ----------------------------
-- Auto increment value for sys_permission
-- ----------------------------
ALTER TABLE `sys_permission` AUTO_INCREMENT=5;

-- ----------------------------
-- Auto increment value for sys_role
-- ----------------------------
ALTER TABLE `sys_role` AUTO_INCREMENT=3;

-- ----------------------------
-- Auto increment value for sys_role_permissions
-- ----------------------------
ALTER TABLE `sys_role_permissions` AUTO_INCREMENT=7;
